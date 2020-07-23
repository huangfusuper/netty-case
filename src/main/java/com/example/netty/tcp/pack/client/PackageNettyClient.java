package com.example.netty.tcp.pack.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.net.InetSocketAddress;

/**
 * @author huangfu
 */
public class PackageNettyClient {
    public static void main(String[] args) {
        EventLoopGroup workGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        nioSocketChannel.pipeline().addLast(new PackageNettyClientHandler());
                    }
                })
                .connect(new InetSocketAddress("127.0.0.1",8080))
                .addListener(future -> {
                    if(future.isSuccess()){
                        System.out.println("成功");
                    }else{
                        System.out.println("失败");
                        System.exit(-1);
                    }
                });
    }
}
