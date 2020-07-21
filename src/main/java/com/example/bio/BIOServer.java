package com.example.bio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Server 端首先创建了一个serverSocket来监听 8000 端口，
 * 然后创建一个线程，线程里面不断调用阻塞方法 serversocket.accept();
 * 获取新的连接，当获取到新的连接之后，给每条连接创建一个新的线程，这个线程负责从该连接中读取数据
 * @author huangfu
 */
public class BIOServer {
    /**
     * 处理线程的线程池
     */
    public static final ThreadPoolExecutor BIO_THREAD_POOL = new ThreadPoolExecutor(
            10,
            100,
            60L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(100),
            r ->new Thread(r, "BIO Thread Poll-" + r.hashCode()));
    /**
     * 服务端口
     */
    public static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        /**
         * 获取服Socket
         */
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            Socket socket;
            while (true) {
                /**
                 * 获取连接  当没有连接时  改代码处于阻塞状态
                 */
                socket = serverSocket.accept();
                Socket finalSocket = socket;
                BIO_THREAD_POOL.execute(() -> {
                    serverHandler(finalSocket);
                    try {
                        System.out.println("-----=该连接被关闭----");
                        finalSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        }

    }

    /**
     * 服务器处理器
     * @param accept 连接信息
     */
    public static void serverHandler(Socket accept) {
        /**
         * 读取的缓冲流
         */
        BufferedReader bufferedReader = null;
        /**
         * 输出的缓冲流
         */
        PrintWriter printWriter;

        try {
            bufferedReader = new BufferedReader(new InputStreamReader(accept.getInputStream()));
            //设置自动刷新
            printWriter = new PrintWriter(accept.getOutputStream(),true);
            while (true) {
                String readLine = bufferedReader.readLine();
                if("end".equals(readLine)) {
                    return;
                }
                System.out.println(String.format("服务器接收到信息为:%s",readLine));
                LocalDateTime localDateTime = LocalDateTime.now();
                String thisDateTime = localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                printWriter.println(thisDateTime);
            }
        }catch (Exception e) {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }

        }
    }
}
