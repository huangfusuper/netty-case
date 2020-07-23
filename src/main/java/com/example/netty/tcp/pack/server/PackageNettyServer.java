package com.example.netty.tcp.pack.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.nio.charset.StandardCharsets;

/**
 * @author huangfu
 */
public class PackageNettyServer {

    public static void main(String[] args) {
        EventLoopGroup boosGroup = new NioEventLoopGroup(1);
        EventLoopGroup workGroup = new NioEventLoopGroup();

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(boosGroup,workGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        //定长解码器
                        //nioSocketChannel.pipeline().addLast(new FixedLengthFrameDecoderFixedLengthFrameDecoder(2017));

                        // 行解码器 LineBasedFrameDecoder \n
                        //nioSocketChannel.pipeline().addLast(new LineBasedFrameDecoder(Integer.MAX_VALUE));

                        //DelimiterBasedFrameDecoder 分隔符解码IQ
                        //ByteBuf buffer = nioSocketChannel.alloc().buffer();
                        //byteBufDecoder("|",buffer);
                        //nioSocketChannel.pipeline().addLast(new DelimiterBasedFrameDecoder(Integer.MAX_VALUE,buffer));

                        //基于长度域拆包器 LengthFieldBasedFrameDecoder
                        //nioSocketChannel.pipeline().addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,0,4));

                        //基于长度域拆包器 请求头 LengthFieldBasedFrameDecoder
                        //nioSocketChannel.pipeline().addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,1,4));

                        //请求头   长度域拆包器 请求头 LengthFieldBasedFrameDecoder
                        //nioSocketChannel.pipeline().addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,1,4,1,0));


                        //请求头   长度域拆包器 请求头 LengthFieldBasedFrameDecoder  截取body
                        nioSocketChannel.pipeline().addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,1,4,1,6));

                        nioSocketChannel.pipeline().addLast(new PackageNettyServerHandler());
                    }
                })
                .bind(8080);
    }

    private static void byteBufDecoder(String mag,ByteBuf buffer){
        buffer.writeBytes(mag.getBytes(StandardCharsets.UTF_8));
    }
}
