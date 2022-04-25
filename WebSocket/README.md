## Java 网络编程
### BIO
Demo流程:
1. 死循环阻塞监听请求
2. 监听到请求，创建线程处理请求
3. 处理线程阻塞在read操作上


### NIO
- 以缓冲区的方式处理数据，每个线程依靠`selector`处理多个连接
- Buffer 本质是可读可写的内存块
- nio.socketChannel 可对应于 net.socket

Demo流程:
1. Selector 收到客户端连接请求，ServerSocketChannel 创建对应的SocketChannel。
2. 将 SocketChannel 注册到 Selector 上，注册会返回SelectionKey, 可以通过key获取到响应的Channel
3. Selector 会调用 select 对内部的SekectionKeySet 关联的 Channel 进行监听
4. 利用 SocketChannel 完成数据处理。

   
### netty




[参考1](https://cloud.tencent.com/developer/article/1754078)
[参考2](https://www.jianshu.com/p/56216d1052d7)