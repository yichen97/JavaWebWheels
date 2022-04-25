package com.example;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

/**
 * @AuthorName: yiChen
 * @className: BIO
 * @description: TODO 类描述
 * @date: 2022/4/25
 **/
public class BlockingIO {
    public static void main(String[] args) throws IOException {
        ExecutorService threadPool = new ThreadPoolExecutor(
                //核心线程池大小
                3,
                //最大核心线程池大小
                5,
                //空闲线程存活时间
                1L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(3),
                //线程工厂
                Executors.defaultThreadFactory(),
                //拒绝策略
                new ThreadPoolExecutor.DiscardOldestPolicy());
        ServerSocket serverSocket = new ServerSocket(8000);

        while(true) {
            final Socket socket = serverSocket.accept();
            threadPool.execute(() -> {
                try {
                    handler(socket);
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private static void handler(Socket socket) throws IOException {
        byte[] bytes = new byte[1024];
        InputStream inputStream = socket.getInputStream();

        while(true) {
            int read = inputStream.read(bytes);
            if(read != -1){
                System.out.println("msg from client" + new String(bytes, 0, read));
            }else{
                break;
            }
        }
    }
}
