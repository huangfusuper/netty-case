package com.example.netty.tcp.time.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class NettyTimeClient {
    final static Scanner scanner = new Scanner(System.in);
    public static void main(String[] args) {
        EventLoopGroup workGroup = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class)
                .group(workGroup)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        nioSocketChannel.pipeline().addLast(new NettyClientHandler());
                    }
                });
        bootstrap.connect(new InetSocketAddress("127.0.0.1",8080)).addListener(future -> {
            if (future.isSuccess()) {
                new Thread(() ->{
                    while (true) {
                        System.out.println("请输入要发送的数据：");
                        String nextLine = scanner.nextLine();
                        Channel channel = ((ChannelFuture) future).channel();
                        ByteBuf buffer = channel.alloc().buffer();
                        buffer.writeBytes(nextLine.getBytes(StandardCharsets.UTF_8));
                        channel.writeAndFlush(buffer);
                    }
                }).start();
            }else{
                System.exit(-1);
            }
        });


    }


}
