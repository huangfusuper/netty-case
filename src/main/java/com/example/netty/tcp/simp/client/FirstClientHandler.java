package com.example.netty.tcp.simp.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.StandardCharsets;

/**
 * 客户端连接
 * @author huangfu
 */
public class FirstClientHandler extends ChannelInboundHandlerAdapter {
    /**
     * 这个方法会在客户端连接建立成功之后被调用
     * 客户端连接建立成功之后，调用到 channelActive() 方法，在这个方法里面，我们编写向服务端写数据的逻辑
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("客户端写出数据");


        ctx.channel().writeAndFlush(getByteBuf(ctx));
    }

    private ByteBuf getByteBuf(ChannelHandlerContext channelHandlerContext) {
        //取到一个 ByteBuf 的内存管理器，这个 内存管理器的作用就是分配一个 ByteBuf
        ByteBuf buffer = channelHandlerContext.alloc().buffer();

        byte[] bytes = "hello,皇甫科星".getBytes(StandardCharsets.UTF_8);
        buffer.writeBytes(bytes);
        return buffer;
    }
}
