## Java 网络编程
本部分共四个Demo，逐渐递进：
1. BlockingIO 基于同步阻塞IO的 Server
2. NonBlockingIO 基于同步阻塞IO Server
   前两个IO运行和，可以直接使用浏览器访问端口进行测试。
3. sc1文件中 基于 Netty的TCP Server/Client
    展示了使用 Netty 的基本使用
4. NioWebSocketServer 使用 netty 实现了一个简单的仅支持文本消息的 WebSocket 服务器

Netty: 对 java.nio 的封装，利用同步非阻塞的底层为用户提供两异步IO的效果。主要结构类似于“主从Reactor”
形式，大大简化了网络编程的复杂性。


### BIO
Demo流程:
```
1. 死循环阻塞监听请求
2. 监听到请求，创建线程处理请求
3. 处理线程阻塞在read操作上
```



### NIO
- 以缓冲区的方式处理数据，每个线程依靠`selector`处理多个连接
- Buffer 本质是可读可写的内存块
- nio.socketChannel 可对应于 net.socket

Demo流程:
```
1. Selector 收到客户端连接请求，ServerSocketChannel 创建对应的SocketChannel。
2. 将 SocketChannel 注册到 Selector 上，注册会返回SelectionKey, 可以通过key获取到响应的Channel
3. Selector 会调用 select 对内部的SekectionKeySet 关联的 Channel 进行监听
4. 利用 SocketChannel 完成数据处理。
```

### Netty TCP Server/Client

- `BootStrap` 和 `ServerBootStrap` 分别是客户端和服务端的引导类，一个 Netty 应用
  程序通常由一个引导类开始，主要用来配置整个 Netty 程序、设置业务处理类（Handler）、绑定端口、发起连接等。
- 客户端创建一个`NioSocketChannel` 作为客户端通道，去连接服务器。
- 服务端首先创建一个`NioServerSocketChannel` 作为服务器通道，
  每当接收一个客户端连接就产生一个`NioSocketChannel`应对客户端。
- 每一个 NioEventLoop 包含 Selector、任务队列、执行器等。
   
### NioWebSocketServer
- 运行NioWebSocketServer, 打开test.html
- 浏览器会自动向Server 发送http请求，建立 webSocket 连接


[参考1](https://cloud.tencent.com/developer/article/1754078)
[参考2](https://www.jianshu.com/p/56216d1052d7)