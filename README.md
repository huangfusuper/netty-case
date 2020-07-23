我们不妨对BIO的线程模型进行分析，来探讨他第一个性能瓶颈！根据我们刚刚看到的代码，我们可以大致理解为当前的BIO的线程模型大概为这样！

![image-20200720151735388](http://images.huangfusuper.cn/typora/20200720伪异步BIO服务端模型.png)

现在线程问题解决了，因为线程池是有界的，所以即使在超高并发的情况下，线程池慢了，我们的系统也能够从容的依据线程池的拒绝策略去解决大部分的问题，但是性能瓶颈仅仅只有线程不，不妨点开bio锁依赖的BIO输入流（InputStremm）的源码,我们看一下在读取时会发生什么？

```java
	/**
	 * ........
     * This method blocks until input data is available, end of file is detected, or an exception is thrown.
     * 翻译过来就是：该方法将阻塞，直到输入数据可用，检测到文件结尾或引发异常为止。
     */
    public int read(byte b[]) throws IOException {
        return read(b, 0, b.length);
    }
```

注意一下这个api的注释文档！那么我们就可以认为，当对Socket输入流进行读取的时候，只有三种情况下会停止阻塞：

1. 有数据可读
2. 可用的数据已经读取完毕
3. 发生异常

那么这意味着什么？当对方发送请求或者应答消息比较缓慢的时候，或者网路传输较慢的时候，读取输入流的一方的通信线程会被长时间的阻塞，如果对方需要60秒才能发送完数据的话，读取的一方I/O线程也会被阻塞60秒，在此期间，其他的接入消息只能排队。

那么writeAPI也依旧如此，他取消阻塞的方法有两种：

1. 数据写完
2. 发生异常

当消费方的消费速度赶不上生产方的生产速度是，会产生数据积压，此时TCP的 windows size 会缩减为0，这是发送发无法在向TCP缓冲区写入或任何的数据，此时就会处于无期限的阻塞，除非windows size大于0或者发生异常

![<u>image-20200720170731313</u>](http://images.huangfusuper.cn/typora/20200720BIO线程阻塞.png)

1. 线程资源受限：线程是操作系统中非常宝贵的资源，同一时刻有大量的线程处于阻塞状态是非常严重的资源浪费，操作系统耗不起
2. 线程切换效率低下：单机 CPU 核数固定，线程爆炸之后操作系统频繁进行线程切换，应用性能急剧下降。
3. 除了以上两个问题，IO 编程中，我们看到数据读写是以字节流为单位。

## 二、NIO是什么？

nio有些人叫他`New I/O`,原因是它相对于之前的I/O是新增的类库，但是jdk官方的叫法是`Non-block I/O`,简称NIO！

 **NIO支持面向缓冲区(Buffer)的、基于通道(Channel)的IO操作。NIO将以更加高效的方式进行文件的读写操作。**术语方面我就不说了，如果没有研究过NIO的话，术语听着无疑是很难受的！我们用一张简单的图来对比BIO与NIO的流程方法！

### 1.BIO的处理模型

![image-20200720173613691](http://images.huangfusuper.cn/typora/20200720BIO的线程处理模型哦吼.png)

### 2.NIO的处理模型

![image-20200720174128437](http://images.huangfusuper.cn/typora/NIO的处理模型20200720呕吼.png)

根据上面的代码，我们可以很容易的理解上面的两幅图，BIO 每一个线程都对应着一个死循环，每一个死循环都不断的监控着属于自己的读写流是否有数据，从而进行读取，但是大多数情况下，1w个连接，有数据可读的往往是少数，所以那些没有数据可读的线程都被浪费掉了！

NIO则不同，所有的线程都被注册在了一个叫做`selector`的选择器上，选择器只需要不断的扫描注册在上的连接，就可以批量监测出有数据可读的连接，进而读取数据！这样线程的数量少了，一是对于内存的占用少了，第二个因为线程数量少，CPU对于线程的切换效率也大大增强！

| BIO                     | NIO                         |
| ----------------------- | --------------------------- |
| 面向流(Stream oriented) | 面向缓冲区(Buffer oriented) |
| 阻塞式(Blocking IO)     | 非阻塞式(Non blocking IO)   |
| 无                      | 选择器(Selectors)           |

#### 面向流与面向缓冲

 JavaIO面向流意味着每次从流中读一个或多个字节，直至读取所有字节，它们没有被缓存在任何地方。此外，它不能前后移动流中的数据。如果需要前后移动从流中读取的数据，需要先将它缓存到一个缓冲区。Java NIO的缓冲导向方法略有不同。数据读取到一个它稍后处理的缓冲区，需要时可在缓冲区中前后移动。这就增加了处理过程中的灵活性。但是，还需要检查是否该缓冲区中包含所有您需要处理的数据。而且，需确保当更多的数据读入缓冲区时，不要覆盖缓冲区里尚未处理的数据。

#### 阻塞与非阻塞IO

Java IO的各种流是阻塞的。这意味着，当一个线程调用read() 或 write()时，该线程被阻塞，直到有一些数据被读取，或数据完全写入。该线程在此期间不能再干任何事情了。 Java NIO的非阻塞模式，使一个线程从某通道发送请求读取数据，但是它仅能得到目前可用的数据，如果目前没有数据可用时，就什么都不会获取。而不是保持线程阻塞，所以直至数据变的可以读取之前，该线程可以继续做其他的事情。非阻塞写也是如此。一个线程请求写入一些数据到某通道，但不需要等待它完全写入，这个线程同时可以去做别的事情。线程通常将非阻塞IO的空闲时间用于在其它通道上执行IO操作，所以一个单独的线程现在可以管理多个输入和输出通道（channel）。

#### 选择器（Selectors）

Java NIO的选择器允许一个单独的线程来监视多个输入通道，你可以注册多个通道使用一个选择器，然后使用一个单独的线程来“选择”通道：这些通道里已经有可以处理的输入，或者选择已准备写入的通道。这种选择机制，使得一个单独的线程很容易来管理多个通道。

相比来说NIO比BIO的编码复杂度，复杂了不止好几倍，但是NIO的应用却是越来越广泛为什么呢？

- 客户端发起的连接操作是异步的，可以通过多路复用器注册`OP_CONNECT`等待后续结果，不需要向之前那样被同步阻塞。
- SocketChannel的读写操作时异步的，如果没有可读写的数据，他不会同步等待，直接返回，这样IO线程就可以处理其他链路，不需要同步等待这个链路的可用。
- 线程模型的优化：由于JDK的Selector在Linux等主流操作系统上是通过epoll实现，他没有连接句柄的限制（只受限于操作系统的最大句柄数或者单个进程的最大句柄数），这意味着一个selector线程可以处理成千上万个客户端连接，且性能不会随着客户端的增加而线程下降，因此他适合做高性能、高负载的网络服务器。

### 为何要用Reactor

常见的网络服务中，如果每一个客户端都维持一个与登陆服务器的连接。那么服务器将维护多个和客户端的连接以出来和客户端的contnect 、read、write ，特别是对于长链接的服务，有多少个c端，就需要在s端维护同等的IO连接。这对服务器来说是一个很大的开销。

很明显，为了避免资源耗尽，我们采用线程池的方式来处理读写服务。但是这么做依然有很明显的弊端：

> 1. 同步阻塞IO，读写阻塞，线程等待时间过长
> 2. 在制定线程策略的时候，只能根据CPU的数目来限定可用线程资源，不能根据连接并发数目来制定，也就是连接有限制。否则很难保证对客户端请求的高效和公平。
> 3. 多线程之间的上下文切换，造成线程使用效率并不高，并且不易扩展
> 4. 状态数据以及其他需要保持一致的数据，需要采用并发同步控制

NIO

> 非阻塞的IO读写
>
> 基于IO事件进行分发任务，同时支持对多个fd的监听

*我们可以看到上述的NIO例子已经差不多拥有reactor的影子了*

1. 基于事件驱动-> selector（支持对多个socketChannel的监听）
2. 统一的事件分派中心-> dispatch
3. 事件处理服务-> read & write

事实上NIO已经解决了上述BIO暴露的1&2问题了，服务器的并发客户端有了量的提升，不再受限于一个客户端一个线程来处理，而是一个线程可以维护多个客户端（selector 支持对多个socketChannel 监听）。

### NIO的内核优化

https://www.cnblogs.com/fatmanhappycode/p/12362423.html

https://my.oschina.net/editorial-story/blog/3052308#comments

传统select的接收流程

> select 的实现思路很直接，假如程序同时监视如下图的 sock1、sock2 和 sock3 三个 socket，那么在调用 select 之后，操作系统把进程 A 分别加入这三个 socket 的等待队列中。

![<u>image-20200723102231455</u>](http://images.huangfusuper.cn/typora/0723select内核是监控socket实现.png)

> 操作系统把进程 A 分别加入这三个 socket 的等待队列中,当任何一个 socket 收到数据后，中断程序将唤起进程。下图展示了 sock2 接收到了数据的处理流程：

![image-20200723102446914](http://images.huangfusuper.cn/typora/0723socketCPU触发中断请求.png)
>*sock2 接收到了数据，中断程序唤起进程 A*,所谓唤起进程，就是将进程从所有的等待队列中移除，加入到工作队列里面，如下图所示：

![image-20200723102553261](http://images.huangfusuper.cn/typora/0720中断指令触发进程添加到工作队列1.png)

**那么这种处理的缺陷也显而易见**:

- 每次调用 select 都需要将进程加入到所有监视 socket 的等待队列，每次唤醒都需要从每个队列中移除。这里涉及了两次遍历，而且每次都要将整个 fds 列表传递给内核，有一定的开销。正是因为遍历操作开销大，出于效率的考量，才会规定 select 的最大监视数量，默认只能监视 1024 个 socket。
- 进程被唤醒后，程序并不知道哪些 socket 收到数据，还需要遍历一次。

那么，**有没有减少遍历的方法？有没有保存就绪 socket 的方法？这两个问题便是 epoll 技术要解决的**。

### epoll的设计思路

select 低效的另一个原因在于程序不知道哪些 socket 收到数据，只能一个个遍历。如果内核维护一个“就绪列表”，引用收到数据的 socket，就能避免遍历。

如下图所示，计算机共有三个 socket，收到数据的 sock2 和 sock3 被就绪列表 rdlist 所引用。当进程被唤醒后，只要获取 rdlist 的内容，就能够知道哪些 socket 收到数据。

![image-20200723105327826](http://images.huangfusuper.cn/typora/epoll维护就绪列表.png)

步骤实现

1. epoll_create() 创建好请求socket的fd; 准备空间, 在高速cache中生成Map(就那个红黑树) 和 1个List(就那个事件链表)

2. epoll_ctl(epollfd, my_events) 只把有效fd事件(比如:读写等)注册进红黑树, 以便支持快速插入、删除、修改等操作而不用再变遍历 ,然后通过回调函数, 写进事件链表里.
3. epoll_wait(epollfd, ready_events) 检查事件链表中有数据就处理, 没有数据就睡眠.

epoll优点:

1. 相比select和poll, epoll获取有效fd的时间复杂度是O(1), epoll每次只扫描事件链表的数据, 直接取符合条件的fd, 所以复杂度O(1) 
2. epoll提供高速cache, 共享用户态和内核态, 不需要内核状态之前的切换.



### 3. 不选择Java原生NIO编程的原因

- nio的类库和API繁杂，使用麻烦，需要掌握Selector、ServerSocketChannel、SocketChannel、ByteBuffer等
- 需要具备其他额外的技能做铺垫，例如熟悉JAVA多线程编程。这是因为NIO编程涉及到Reactor模式你必须对多线程和网络编程非常熟悉才能编写高质量的NIO程序。
- 可靠性能力补齐，工作量和难度都特别大，例如客户端面临断链重连、网络闪断、半包读写、失败缓存、网络拥塞和异常码的处理问题，NIO编程的特点是功能开发容易，但是可靠性能力补全的工作量和难度极大！
- JDK NIO的BUG，臭名昭著的epoll bug 会导致Selector空轮训，最终导致CPU 100%，虽然官方1.6的时候宣称已经解决，但是到现在也没有根本解决，只是发生的概率降低了！
- TCP编程中最容易出现的拆包粘包问题不好解决

## 三、Netty

### 1.为什么选择Netty

- 业界最流行的NIO框架
- API简单使用，开发门槛低
- 定制功能强悍，通过ChannlHandler对通信矿建灵活扩展
- 成熟、稳定，目前Netty已经修复了迄今为止一经发现的所由的jdk的BUG，业务人员不需要在为NIO的BUG烦恼！
- 社区活跃、版本迭代周期短、发现BIUG可以及时修复！
- 健壮性、功能、性能、可定制性、可扩展性首屈一指！
- 经过业内诸如 zk、dubbo、es、tomcat、hadoop等主流框架的实战考验！

### 2.Netty解码器

- FixedLengthFrameDecoder  固定长度的解码器
- LineBasedFrameDecoder  行解码器
- DelimiterBasedFrameDecoder  自定义，分隔符
- LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,0,4)   不定长长度域   最大数值    长度位偏移量    长度标识长度
- LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,1,4)   不定长长度域   最大数值    长度位偏移量    长度标识长度

- new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,1,4,1,6)  不定长长度域   最大数值    长度位偏移量    长度标识长度 长度域后到Body的偏移量    切断数据前置数据偏移量。````