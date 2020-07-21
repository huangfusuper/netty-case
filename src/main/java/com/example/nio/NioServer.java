package com.example.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Set;

/**
 * @author huangfu
 */
public class NioServer {

    public static void main(String[] args) throws IOException {
        //打开服务管道
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        //打开选择器
        Selector selector = Selector.open();
        //设置为非阻塞
        serverSocketChannel.configureBlocking(false);
        //绑定端口
        serverSocketChannel.bind(new InetSocketAddress(8080));
        //注册选择器  注册为可连接的
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            //至少有一个通道被选择或者到达超时时间时返回
            selector.select(1000);
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                //获取当前要处理的键
                SelectionKey selectionKey = iterator.next();
                //删除当前这个key
                iterator.remove();
                //验证当前key的有效性
                if(!selectionKey.isValid()) {
                    continue;
                }
                //当key的状态为准备好连接时，将key注册为可读
                if (selectionKey.isAcceptable()){
                    //获取连接
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    //将该链接置为非阻塞
                    socketChannel.configureBlocking(false);
                    //将该链接注册为可读
                    socketChannel.register(selector,SelectionKey.OP_READ);
                }else if(selectionKey.isReadable()){
                    //当发现该通道可读时
                    SocketChannel channel = (SocketChannel) selectionKey.channel();
                    //创建缓冲区
                    ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                    String clientData = handlerInput(channel, byteBuffer);
                    if("end".equals(clientData)){
                        byteBuffer.clear();
                        selectionKey.cancel();
                        channel.close();
                    }else {
                        byteBuffer.clear();
                        System.out.println("【系统消息】服务端收到客户端["+channel.getRemoteAddress()+"]的消息："+clientData);
                        String thisDateTime = getThisDateTime();
                        byteBuffer.put(thisDateTime.getBytes(StandardCharsets.UTF_8));
                        byteBuffer.flip();
                        //开始准备回复消息
                        doWrite(channel, byteBuffer);
                    }
                }
            }
        }
    }

    /**
     * 开始准备写数据
     * @param channel 管道流信息
     * @param byteBuffer 缓冲区
     */
    private static void doWrite(SocketChannel channel, ByteBuffer byteBuffer) throws IOException {
        channel.write(byteBuffer);
        byteBuffer.clear();
    }

    /**
     * 获取当前时间
     * @return 返回当前时间
     */
    private static String getThisDateTime() {
        LocalDateTime localDateTime = LocalDateTime.now();
        return localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * 处理读取客户端的数据
     * @param channel 管道
     * @return 返回值
     * @throws IOException 异常信息
     */
    private static String handlerInput(SocketChannel channel, ByteBuffer byteBuffer) throws IOException {
        channel.read(byteBuffer);
        byteBuffer.flip();
        if(byteBuffer.hasRemaining() && byteBuffer.get(0) == 4) {
            return "end";
        }else{
            byte[] array = new byte[byteBuffer.remaining()];
            byteBuffer.get(array);
            return new String(array, StandardCharsets.UTF_8);
        }
    }

}
