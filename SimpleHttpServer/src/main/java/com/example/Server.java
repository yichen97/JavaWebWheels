package com.example;

import com.example.Cache.Cache;
import com.example.Cache.LRUCache;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * @author yision
 * @className: Server
 * @description: 服务器主线程
 * @date: 2022/4/18
 **/
public class Server extends Thread{
    static int port = 8000;
    ServerSocket serverSocket;
    Logger log = Logger.getLogger("SERVER_LOG_JT");
    String path = "SimpleHttpServer/src/main/resources/";
    //使用线程池管理创建的线程
    //手动创建线程池而不是调用Eexctuors的方法，防止OOM

    static ExecutorService threadPool = new ThreadPoolExecutor(
            //核心线程池大小
            1,
            //最大核心线程池大小
            1,
            //空闲线程存活时间
            1L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(3),
            //线程工厂
            Executors.defaultThreadFactory(),
            //拒绝策略
            new ThreadPoolExecutor.DiscardOldestPolicy());

    public Server(){}

    public static void main(String[] args) {
        Server sever = new Server();
        sever.start();
    }

    /**
     * @param port: The port server listened
     **/
    public Server(int port){
        Server.port = port;
    }


    @Override
    public void run() {
        try{
            serverSocket = new ServerSocket(port);
            Socket socket = null;
            // Waiting for client
            // serverSocket.accept()是一个阻塞方法，意味着该循环用于不会结束且只有有连接时会进入循环体
            while ((socket = serverSocket.accept()) != null){
                //Child Thread here
                // if connected, let a httpServer thread to handle it.
                log.info(socket.toString());
                threadPool.execute(new HttpServer(socket));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * @description: 子线程，用于处理请求
     */
    class HttpServer implements Runnable{
        Socket currentSocket = null;
        InputStream inputStream = null;
        OutputStream outputStream = null;

        final static int SUCESS = 200;
        final static int NO_RESOURCE = 404;

        int resultStatus = 404;
        //Error info length
        long responseLength = 20;

        public HttpServer(Socket socket){
            try{
                this.currentSocket = socket;
                this.inputStream = socket.getInputStream();
                this.outputStream = socket.getOutputStream();
            }catch (Exception e){
                log.info("Connection aborted");
            }
        }

        //流程: 读取 -> 解析 -> 处理 -> 响应 ->写出


        @Override
        public void run() {
            try{
                String rawString = read();
                //观察到有时浏览器传来空串，可能是客户端主动关闭socket
                if(rawString != null){
                    String serverCommand = parse(rawString);
                    String resultString = process(serverCommand);
                    String rawResultString = response(resultString);
                    write(rawResultString);
                }
                log.info(currentSocket.toString() + "关闭");
                currentSocket.close();

            }catch (Exception e){
                log.info("Failed connection");
                e.printStackTrace();
            }
        }

        private String read(){
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            try{
                //处理了第一行， 包括请求方法、请求URL、协议机版本。
                String infoRead = bufferedReader.readLine();
                return infoRead;
            }catch(Exception e){
                log.info("Read failed");
                e.printStackTrace();
            }
            return null;
        }

        /**
         * 解析读取的字符串
         * @param rawString raw String from read()
         * @return server command from client
         */
        private String parse(String rawString){
            try{
                log.info("解析请求： " + rawString);
                String[] split = rawString.split(" ");
                //请求方法、请求URL、协议机版本。
                if(split.length != 3){
                    return "abort";
                }
                return split[1];
            }catch (Exception e){
                log.info("Parse failed");
                e.printStackTrace();
            }
            return null;
        }

        /**
         * 处理客户端命令
         * @param commandString 客户端命令
         * @return response info
         */
        private String process(String commandString){
            FileReader fileReader = null;
            BufferedReader bufferedReader = null;
            try{
                log.info("请求资源： " + commandString);
                switch (commandString) {
                    case "/": {
                        commandString = "index.html";
                        resultStatus = SUCESS;
                        break;
                    }
                    case "/1": {
                        commandString = "test1.html";
                        resultStatus = SUCESS;
                        break;
                    }
                    case "/2": {
                        commandString = "test2.html";
                        resultStatus = SUCESS;
                        break;
                    }
                    default: {
                        commandString = "404.html";
                        resultStatus = NO_RESOURCE;
                    }
                }
                String result = "";
                Cache cache = new LRUCache(4);
                if(cache.isExit(commandString)){
                    result = cache.get(commandString);
                }else{
                    File file = new File(path + commandString);
                    fileReader = new FileReader(file);
                    bufferedReader = new BufferedReader(fileReader);
                    StringBuffer stringBuffer = new StringBuffer();
                    String line;
                    while((line = bufferedReader.readLine()) != null){
                        stringBuffer.append(line + "\r\n");
                    }
                    responseLength = file.length();
                    result = stringBuffer.toString();
                    cache.add(commandString, result);
                }
                return result;
            }catch (Exception e){
                log.info("Process failed");
                e.printStackTrace();
            }finally{
                try{
                    bufferedReader.close();
                    fileReader.close();
                }catch (Exception e){
                    log.info("Bad io");
                    e.printStackTrace();
                }
            }
            return null;
        }

        private String response(String resultString){
            try{
                StringBuffer responseInfo = new StringBuffer();
                switch(resultStatus){
                    case SUCESS:{
                        responseInfo.append("HTTP/1.1 200 ok \r\n");
                        responseInfo.append("Content-Type:text/html \r\n");
                        responseInfo.append("Content-Length:" + Long.toString(responseLength)+ " \r\n");
                        //隔开响应头和响应体
                        responseInfo.append("\r\n");
                        responseInfo.append(resultString);
                        break;
                    }
                    case NO_RESOURCE:{
                        responseInfo.append("HTTP/1.1 "+ Integer.toString(NO_RESOURCE) + " NOT FOUND \r\n");
                        responseInfo.append("Content-Type:text/html \r\n");
                        responseInfo.append("Content-Length:" + Long.toString(responseLength)+ "\r\n");
                        //隔开响应头和响应体
                        responseInfo.append("\r\n");
                        responseInfo.append(resultString);
                        break;
                    } default:{
                        break;
                    }
                }
                String result = responseInfo.toString();
                log.info("响应返回： " + result.substring(0, Math.min(25, result.length())) + "...");
                return result;
            }catch (Exception e){
                log.info("response Failed");
                e.printStackTrace();
            }
            return null;
        }

        private void write(String rawResultString){
            try{
                outputStream.write(rawResultString.getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
                outputStream.close();
            }catch(Exception e){
                log.info("write failed");
                e.printStackTrace();
            }
        }
    }
}
