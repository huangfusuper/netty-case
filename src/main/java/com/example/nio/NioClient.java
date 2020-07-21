package com.example.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

/**
 * @author huangfu
 */
public class NioClient {
    private static Scanner SCANNER_INPUT = new Scanner(System.in);

    public static void main(String[] args) throws IOException {
        //获取Socket通道
        SocketChannel socketChannel = SocketChannel.open();
        //获取选择器
        Selector selector = Selector.open();
        //注册选择器
        socketChannel.configureBlocking(false);
        socketChannel.connect(new InetSocketAddress("127.0.0.1",8080));
        socketChannel.register(selector, SelectionKey.OP_CONNECT);

        while (true) {
            selector.select(1000);
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                iterator.remove();
                //查看是否验证
                if(!selectionKey.isValid()){
                    continue;
                }
                //如果连接成功
                if(selectionKey.isConnectable()){
                    //刷新连接
                    socketChannel.finishConnect();
                    //连接准备完成后 绑定为写事件
                    //selectionKey.interestOps(SelectionKey.OP_WRITE);
                    socketChannel.register(selector,SelectionKey.OP_WRITE);
                }else if(selectionKey.isWritable()){
                    //如果为写事件
                    if (doWrite(socketChannel)) {
                        socketChannel.register(selector,SelectionKey.OP_READ);
                    }else{
                        selectionKey.cancel();
                        socketChannel.close();
                        System.exit(-1);
                    }
                }else if(selectionKey.isReadable()) {
                    handlerInput(socketChannel);
                    socketChannel.register(selector,SelectionKey.OP_WRITE);
                }
            }
        }
    }

    /**
     * 读取数据处理器
     * @param socketChannel
     * @throws IOException
     */
    private static void handlerInput(SocketChannel socketChannel) throws IOException {
        ByteBuffer allocate = ByteBuffer.allocate(1024);
        int read = socketChannel.read(allocate);
        if(read > 0){
            System.out.println(new String(allocate.array(), StandardCharsets.UTF_8));
        }
    }

    /**
     * 写数据
     * @param socketChannel 通道
     * @return 是否结束
     * @throws IOException
     */
    private static boolean doWrite(SocketChannel socketChannel) throws IOException {
        String nextLine = SCANNER_INPUT.nextLine();
        socketChannel.write(ByteBuffer.wrap(nextLine.getBytes(StandardCharsets.UTF_8)));
        if("end".equals(nextLine)){
            return false;
        }
        return true;
    }
}
