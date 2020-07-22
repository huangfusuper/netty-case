package com.example.netty.tcp.simp.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * Netty服务器
 * @author huangfu
 */
public class NettyServer {
    private int port;

    public NettyServer(int port) {
        this.port = port;
    }

    public  void start() {
        //监听端口  accept新连接的线程组
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        //处理数据读写的线程组
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            //引导我们进行服务端的启动工作
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup,workerGroup)
                    //选择通道类型    BIO=OioServerSocketChannel.class
                    .channel(NioServerSocketChannel.class)
                    //定义后续每条连接的数据读写，业务处理逻辑
                    //NioSocketChannel 泛型
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                            nioSocketChannel.pipeline().addLast(new FirstServerHandler());
                        }
                    });
            bind(serverBootstrap, port);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 绑定端口
     * @param serverBootstrap 服务启动引导
     * @param port 端口
     */
    private void bind(ServerBootstrap serverBootstrap, int port){
        serverBootstrap.bind(port).addListener(future -> {
            if(future.isSuccess()){
                System.out.println(port+"端口绑定成功");
            }else{
                System.out.println(port + "端口绑定失败");
                bind(serverBootstrap,port+1);
            }
        });
    }

    public static void main(String[] args) {
        NettyServer nettyServer = new NettyServer(8080);
        nettyServer.start();
    }

}

