package com.example.sc1;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;

import java.io.IOException;

public class Client {

    public static void main(String[] args) throws IOException, InterruptedException {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();

        try{
            Bootstrap bootstrap = new Bootstrap();
            bootstrap
                    .group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .handler(
                            new ChannelInitializer<SocketChannel>() {

                                @Override
                                public void initChannel(SocketChannel socketChannel) throws Exception{
                                    socketChannel.pipeline().addLast(
                                            new NettyClientHandler()
                                    );
                                }
                            }
                    );
            System.out.println("client is ready... ");

            ChannelFuture channelFuture = bootstrap.connect(
                    "127.0.0.1",
                    8080).sync();
            channelFuture.channel().closeFuture().sync();
        } finally {
            eventLoopGroup.shutdownGracefully();
        }
    }

    static class NettyClientHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            ctx.writeAndFlush(
                    Unpooled.copiedBuffer(
                            "hello server",
                            CharsetUtil.UTF_8
                    )
            );
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            System.out.println("server address: " + ctx.channel().remoteAddress());

            ByteBuf byteBuf = (ByteBuf) msg;
            System.out.println("data from server: " + byteBuf.toString(CharsetUtil.UTF_8));
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            ctx.channel().close();
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            super.channelReadComplete(ctx);
        }
    }
}
