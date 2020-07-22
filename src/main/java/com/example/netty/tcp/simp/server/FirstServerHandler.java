package com.example.netty.tcp.simp.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.StandardCharsets;

/**
 * @author huangfu
 */
public class FirstServerHandler extends ChannelInboundHandlerAdapter {
    /**
     * 这个方法在接收到客户端发来的数据之后被回调。
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf)msg;
        System.out.println("服务端读到数据:"+byteBuf.toString(StandardCharsets.UTF_8));
    }
}
