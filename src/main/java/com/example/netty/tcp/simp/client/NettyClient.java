package com.example.netty.tcp.simp.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.concurrent.TimeUnit;

/**
 * netty客户端
 * @author huangfu
 */
public class NettyClient {

    public static void main(String[] args) {
        NioEventLoopGroup workGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new FirstClientHandler());
                    }
                });
        connect(bootstrap,"127.0.0.1",8080,3,3);

    }

    private static void connect(Bootstrap bootstrap, String host, Integer port,int max, int retry){
        bootstrap.connect(host, port).addListener(future -> {
            if(future.isSuccess()){
                System.out.println("连接成功");
            }else if(retry == 0){
                System.out.println("连接失败，重试次数为0");
            }else{
                System.out.println("连接失败");
                int order = (max - retry) + 1;
                int delay = 1 << order;
                System.out.println(System.currentTimeMillis() + ":连接失败，第" + order + "次重连....");
                bootstrap.config().group().schedule(() -> connect(bootstrap,host,port,max,retry-1),delay, TimeUnit.SECONDS);
            }
        });
    }
}
