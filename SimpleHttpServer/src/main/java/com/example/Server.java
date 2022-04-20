package com.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

/**
 * @className: Server
 * @description: 服务器主线程
 * @date: 2022/4/18
 **/
public class Server extends Thread{
    private int port = 8000;
    Logger log = Logger.getLogger("SERVER_LOG_JT");

    public Server(){}

    public static void main(String[] args) {
        Server sever = new Server();
        sever.start();
    }

    /**
     * @param port: The port server listened
     **/
    public Server(int port){
        this.port = port;
    }

    /**
     * @description: 子线程，用于处理请求
     */
    class HttpServer extends Thread{
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
                String serverCommand = parse(rawString);
                String resultString = process(serverCommand);
                String rawResultString = response(resultString);
                write(rawResultString);
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
                log.info(infoRead);
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
                String[] split = rawString.split(" ");
                //请求方法、请求URL、协议机版本。
                if(split.length != 3){
                    return null;
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
                log.info(commandString);
                if("/".equals(commandString)){
                    commandString = "SimpleHttpServer/src/main/resources/index.html";
                    resultStatus = SUCESS;
                }else{
                    commandString = "SimpleHttpServer/src/main/resources/404.html";
                    resultStatus = NO_RESOURCE;
                }
                File file = new File(commandString);
                fileReader = new FileReader(file);
                bufferedReader = new BufferedReader(fileReader);
                StringBuffer stringBuffer = new StringBuffer();
                String line;
                while((line = bufferedReader.readLine()) != null){
                    stringBuffer.append(line + "\r\n");
                }
                responseLength = file.length();
                String result = stringBuffer.toString();
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
                return responseInfo.toString();
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

    @Override
    public void run() {
        super.run();
        try{
//            @SuppressWarnings("resource") // ingore warnings
            ServerSocket serverSocket = new ServerSocket(this.port);
            // Waiting for client
            while (true){
                Socket socket = serverSocket.accept();
                //Child Thread here
                // if connected, let a httpServer thread to handle it.
                new HttpServer(socket).start();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
