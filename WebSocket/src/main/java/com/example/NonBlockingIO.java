package com.example;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @AuthorName: yiChen
 * @className: NoBlockingIO
 * @description: TODO 类描述
 * @date: 2022/4/25
 **/
public class NonBlockingIO {
    public static void main(String[] args) throws IOException {
        // serverSocketChannel 作为处理连接请求的channel
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        Selector selector = Selector.open();

        // 绑定端口
        serverSocketChannel.socket().bind(new InetSocketAddress((1080)));

        // 设置 serverSocketChannel 为非阻塞模式，该模式下accept方法会立刻返回
        serverSocketChannel.configureBlocking(false);

        // 注册 serverSocketChannel 到 selector, 关注 OP_ACCEPT 事件
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while(true) {
            if(selector.select(1000) == 0){
                continue;
            }

            // 有事件发生， 找到发生事件的 Channel 对应 SelectionKey 的集合
            Set<SelectionKey> selectionKeys = selector.selectedKeys();

            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while(iterator.hasNext()){
                SelectionKey selectionKey = iterator.next();

                // 发生 OP_ACCPT 事件，处理连接请求
                if (selectionKey.isAcceptable()) {
                    SocketChannel socketChannel = serverSocketChannel.accept();

                    // 将 socketChannel 也注册到 selector, 关注 OP_READ
                    // 事件，并将 socketChannel 管理 Buffer
                    socketChannel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(1024));
                }

                // 发生 OP_READ 事件， 读客户端数据
                if (selectionKey.isReadable()){
                    SocketChannel channel = (SocketChannel) selectionKey.channel();
                    ByteBuffer buffer = (ByteBuffer) selectionKey.attachment();
                    channel.read(buffer);

                    System.out.println("msg from client" + new String(buffer.array()));
                }

                // 手动从集合中移除当前的 selectionKey, 防止重复处理事件
                iterator.remove();
            }
        }
    }
}
