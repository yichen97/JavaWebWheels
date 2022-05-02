package com.example.sc1;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.CharsetUtil;


public class Server {

    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try{
            ServerBootstrap bootstrap = new ServerBootstrap();

            bootstrap
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(
                            new ChannelInitializer<SocketChannel>() {
                                @Override
                                protected void initChannel(
                                        SocketChannel socketChannel
                                )throws Exception {
                                    socketChannel.pipeline().addLast(
                                            new NettyServerHandler()
                                    );
                                }
                            }
                    );
            System.out.println("server is ready...");

            ChannelFuture channerlFuture = bootstrap.bind(8080).sync();

            channerlFuture.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    static class NettyServerHandler extends ChannelInboundHandlerAdapter{
        //可读时
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            System.out.println("client address: " + ctx.channel().remoteAddress());
            ByteBuf byteBuf = (ByteBuf) msg;
            System.out.println("data from client: " + byteBuf.toString(CharsetUtil.UTF_8));

        }

        //读取完毕后


        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.writeAndFlush(
                    Unpooled.copiedBuffer(
                            "hello client! I have got your data.",
                            CharsetUtil.UTF_8
                    )
            );
        }

        //发生异常时

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            ctx.channel().close();
        }
    }
}
