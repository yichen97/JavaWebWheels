package com.example;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.CharsetUtil;
import org.apache.log4j.Logger;
import java.util.Date;


public class NioWebSocketServer {
    private final Logger logger = Logger.getLogger(this.getClass());

    private void init(){
        // 创建 BossGroup 和 WorkerGroup
        // 1. bossGroup 只处理连接请求
        // 2. 业务处理由 workerGroup 完成
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try{
            // 创建服务器端的启动对象
            ServerBootstrap bootStrap = new ServerBootstrap();
            // 配置参数
            bootStrap
                    // 设置线程组
                    .group(bossGroup, workerGroup)
                    // 说明服务器端通道的实现类（便于 Netty 做反射处理）
                    .channel(NioServerSocketChannel.class)
                    // 设置等待连接的队列容量 （当客户端连接请求速率大于接受速率或使用队列做缓冲）
                    // option() 用于给服务端的 serverSocketChannel 添加配置
                    .option(ChannelOption.SO_BACKLOG, 128)
                    // 设置连接保活
                    // childOption() 用于给服务端的 ServerSocketChannel
                    // 接收到的 socketChannel 添加配置
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    // handler 方法用于给BossGroup 设置业务处理器
                    // childHandler 用于给 WorkerGroup 设置业务处理器
                    .childHandler(
                            // 创建一个通道初始化对象
                            new NioWebSocketChannelInitializer()
                    );

            logger.info("webSocket服务器启动成");
            // 绑定端口，启动服务器，生成一个 channelFuture 对象
            ChannelFuture channelFuture = bootStrap.bind(8080).sync();
            // 对通道关闭进行监听
            channelFuture.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            logger.info("webSocket服务器已关闭");
        }
    }

    class NioWebSocketChannelInitializer extends ChannelInitializer<SocketChannel> {

        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ch.pipeline()
                    .addLast("logging", new LoggingHandler("DEBUG"))
                    .addLast("http-codec", new HttpServerCodec())
                    .addLast("aggregator", new HttpObjectAggregator(65536))
                    // 用于大数据分区传输
                    .addLast("http-chunked", new ChunkedWriteHandler())
                    .addLast("handler", new NioWebSocketHandler());
        }
    }

    static class NioWebSocketHandler extends SimpleChannelInboundHandler<Object>{

        private final Logger logger=Logger.getLogger(this.getClass());
        private WebSocketServerHandshaker handshaker;

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
            logger.debug("收到消息" + msg);
            if(msg instanceof FullHttpMessage){
                handleHttpRequest(ctx, (FullHttpRequest) msg);
            }else if(msg instanceof WebSocketFrame){
                handleWebSocketFrame(ctx, (WebSocketFrame) msg);
            }
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            logger.debug("客户端加入连接： " + ctx.channel());
            ChannelSupervise.addChannel(ctx.channel());
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            logger.debug("客户端口连接： " + ctx.channel());
            ChannelSupervise.removeChannel(ctx.channel());
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.flush();
        }

        private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame){
            // 判断是否关闭链路的指令
            if (frame instanceof CloseWebSocketFrame){
                handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
                return;
            }

            if(frame instanceof PingWebSocketFrame) {
                ctx.channel().write(
                        new PongWebSocketFrame(frame.content().retain()));
                return;
            }

            if(!(frame instanceof TextWebSocketFrame)) {
                logger.debug("本例程仅支持文本消息， 不支持二进制消息");
                throw new UnsupportedOperationException(String.format(
                        "%s frame types not supported", frame.getClass().getName()));
            }
            // 返回应答消息

            String request = ((TextWebSocketFrame) frame).text();
            logger.debug("服务端收到：" + request);
            TextWebSocketFrame tws = new TextWebSocketFrame(new Date().toString()
                    + ctx.channel().id() + "：" + request);
            // 群发
            ChannelSupervise.send2All(tws);
            // 返回【谁发的发给谁】
            // ctx.channel().writeAndFlush(tws);
        }
        /**
         * 唯一的一次http请求，用于创建websocket
         * */
        private void handleHttpRequest(ChannelHandlerContext ctx,
                                       FullHttpRequest req) {
            //要求Upgrade为websocket，过滤掉get/Post
            if (!req.decoderResult().isSuccess()
                    || (!"websocket".equals(req.headers().get("Upgrade")))) {
                //若不是websocket方式，则创建BAD_REQUEST的req，返回给客户端
                sendHttpResponse(ctx, req, new DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
                return;
            }

            WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                    "ws://localhost:8081/websocket", null, false);
            handshaker = wsFactory.newHandshaker(req);
            if (handshaker == null) {
                WebSocketServerHandshakerFactory
                        .sendUnsupportedVersionResponse(ctx.channel());
            } else {
                handshaker.handshake(ctx.channel(), req);
            }
        }
        /**
         * 拒绝不合法的请求，并返回错误信息
         * */
        private static void sendHttpResponse(ChannelHandlerContext ctx,
                                             FullHttpRequest req, DefaultFullHttpResponse res) {
            // 返回应答给客户端
            if (res.status().code() != 200) {
                ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(),
                        CharsetUtil.UTF_8);
                res.content().writeBytes(buf);
                buf.release();
            }
            ChannelFuture f = ctx.channel().writeAndFlush(res);
            // 如果是非Keep-Alive，关闭连接
            if (!HttpUtil.isKeepAlive(req) || res.status().code() != 200) {
                f.addListener(ChannelFutureListener.CLOSE);
            }
        }
    }
}
