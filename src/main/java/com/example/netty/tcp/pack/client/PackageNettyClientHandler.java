package com.example.netty.tcp.pack.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.StandardCharsets;

/**
 * @author huangfu
 */
public class PackageNettyClientHandler extends ChannelInboundHandlerAdapter {
    //2017
    public static final String STRING = "皇甫科星皇甫科星皇甫科星皇甫科星皇甫科星皇甫科星皇甫科星皇甫科星皇甫科星皇甫科星皇甫科星皇甫科星皇甫科星皇甫科星皇甫科星|";


    // 请求头  有长度域的写入方式  请求头 body
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("---------触发----------");
        Channel channel = ctx.channel();
        int i = 0;
        while (i<=1000) {
            ByteBuf buffer = ByteBufAllocator.DEFAULT.ioBuffer();

            byte[] bytes = STRING.getBytes(StandardCharsets.UTF_8);
            //buffer.writeByte(1);
            //buffer.writeInt(bytes.length);
            //buffer.writeByte(1);
            //buffer.writeBytes(bytes);
            channel.writeAndFlush(buffer);
            i++;
        }

    }



    //有 请求头 有长度域的写入方式
//    @Override
//    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        System.out.println("---------触发----------");
//        Channel channel = ctx.channel();
//        int i = 0;
//        while (i<=1000) {
//            ByteBuf buffer = ByteBufAllocator.DEFAULT.ioBuffer();
//
//            byte[] bytes = STRING.getBytes(StandardCharsets.UTF_8);
//            buffer.writeByte(1);
//            buffer.writeInt(bytes.length);
//            buffer.writeBytes(bytes);
//            channel.writeAndFlush(buffer);
//            i++;
//        }
//
//    }

    //有长度域的写入方式
//    @Override
//    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        System.out.println("---------触发----------");
//        Channel channel = ctx.channel();
//        int i = 0;
//        while (i<=1000) {
//            ByteBuf buffer = ByteBufAllocator.DEFAULT.ioBuffer();
//
//            byte[] bytes = STRING.getBytes(StandardCharsets.UTF_8);
//            buffer.writeInt(bytes.length);
//            buffer.writeBytes(bytes);
//            channel.writeAndFlush(buffer);
//            i++;
//        }
//
//    }

    //没有自定义长度域的写入方式
    /*@Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("---------触发----------");
        Channel channel = ctx.channel();
        int i = 0;
        while (i<=1000) {
            ByteBuf buffer = channel.alloc().buffer();
            buffer.writeBytes(STRING.getBytes(StandardCharsets.UTF_8));
            channel.writeAndFlush(buffer);
            i++;
        }

    }*/
}
