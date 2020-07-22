package com.example.netty.tcp.time.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author huangfu
 */
public class TimeServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("【系统消息，服务器收到消息】："+decoding((ByteBuf) msg));
        ByteBuf buffer = ctx.alloc().buffer();

        LocalDateTime localDateTime = LocalDateTime.now();
        String format = localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        encoding(format,buffer);
        ctx.channel().writeAndFlush(buffer);

    }

    public void encoding(String thisDateTime, ByteBuf byteBuf){
        byteBuf.writeBytes(thisDateTime.getBytes(StandardCharsets.UTF_8));
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
