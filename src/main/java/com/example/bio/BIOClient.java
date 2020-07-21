package com.example.bio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * BIO客户端连接
 * @author huangfu
 */
public class BIOClient {

    public static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        //连接服务器
        Socket socket = new Socket("127.0.0.1", PORT);
        while (true) {
            //获取输入流
            Scanner sc = new Scanner(System.in);
            String line = sc.nextLine();
            //输出流信息
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream(),true);
            printWriter.println(line);

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("客户端收到："+bufferedReader.readLine());
            //关闭信号
            if("end".equals(line)) {
                socket.close();
                printWriter.close();
                bufferedReader.close();
                return;
            }
        }
    }
}
