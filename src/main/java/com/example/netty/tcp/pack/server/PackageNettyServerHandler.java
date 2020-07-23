package com.example.netty.tcp.pack.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.StandardCharsets;

/**
 * @author huangfu
 */
public class PackageNettyServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
        //byteBuf.readByte();
        //byteBuf.readInt();
        //byteBuf.readByte();
        System.out.println(byteBuf.toString(StandardCharsets.UTF_8));
    }
}
