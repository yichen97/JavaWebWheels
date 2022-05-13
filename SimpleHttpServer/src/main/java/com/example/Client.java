package com.example;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;

public class Client {

    private static CountDownLatch countDownLatch=new CountDownLatch(10);

    public static void main(String[] args) {
        Client client = new Client();

        for(int i=0; i<10; i++){
            ClientConn clientConn = new ClientConn();
            clientConn.start();
            countDownLatch.countDown();
        }
        // 自旋，防止客户端发送完消息直接关闭，导致连接建立失败
        while(true){

        }
    }

    static class ClientConn extends Thread{
        @Override
        public void run() {
            try {
                String message = "GET / HTTP/1.1\r\n";
                Socket socket = new Socket("127.0.0.1", 8000);
                OutputStream output = socket.getOutputStream();
                output.write(message.getBytes(StandardCharsets.UTF_8));
                countDownLatch.await();
                output.flush();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }





}
