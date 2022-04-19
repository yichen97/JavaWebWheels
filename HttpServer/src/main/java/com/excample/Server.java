package com.excample;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * @className: Server
 * @description: 服务器主线程
 * @date: 2022/4/18
 **/
public class Server extends Thread{
    private int port = 8000;
    Logger log = Logger.getLogger("SERVER_LOG_JT");

    public Server(){};

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
     * @deprecated 子线程，用于处理请求
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
                // Read Interface
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
                //Parse Interface
                return null;
            }catch (Exception e){
                log.info("Parse failed");
                e.printStackTrace();
            }
            return null;
        }

        /**
         * 处理客户端命令
         * @param commandString
         * @return response info
         */
        private String process(String commandString){
            try{
                // process Interface
                return null;
            }catch (Exception e){
                log.info("Process failed");
                e.printStackTrace();
            }finally{
                try{
                    //close IO
                }catch (Exception e){
                    log.info("Bad io");
                    e.printStackTrace();
                }
            }
            return null;
        }

        private String response(String resultString){
            try{
                //response Interface
                return null;
            }catch (Exception e){
                log.info("response Failed");
                e.printStackTrace();
            }
            return null;
        }

        private void write(String rawResultString){
            try{
                //write Interface
                return;
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
