package com.example.netty.tcp.time.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.StandardCharsets;

public class NettyClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println(decoding((ByteBuf)msg));
    }

    /**
     * 数据解码
     * @param byteBuf
     * @return
     */
    public String decoding(ByteBuf byteBuf){
        return byteBuf.toString(StandardCharsets.UTF_8);
    }
}
