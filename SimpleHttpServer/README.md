### Quick Start
将项目下载到本地，用idea打开
1. 查看运行流程
   将log等级调整为info，直接运行Server，用浏览器访问127.0.0.1:8000

2. 查看限流效果
   将log等级调整为warning，直接运行Server，然后运行Client
   
3. 查看LRU缓存效果
   运行TestLRU可以看到编写的缓存类的效果

### 服务器功能
1. 模拟处理GET请求访问网页
2. 能处理请求异常
3. 能持续处理请求

### HTTP生命周期
1. read: 读取socket数据流
2. parser: 解析数据流，分析报头，得到客户端的命令语义
3. process: 处理客户端命令，返回结果
4. reponse: 将处理结果打包，增加报头
5. write: 写入socket数据流

### quick start
- clone本项目, 运行测试类Test
- 用浏览器访问 127.0.0.1:8080
- 访问根目录会返回首页，访问其他均会返回404

一定要注意报文格式，头和体之间有一个 "\r\n"

---

HTTP 1.1 请求报文格式
![img.png](pic/img.png)

---

HTTP 1.1 响应报文格式
![img.png](pic/response.png)


### 改进
1. 为get方法添加LRU缓存
基于LinkedHashMap, 重写removeEldestEntry 即可，每次put 或 putAll 会调用该方法。
> Returns true if this map should remove its eldest entry. 
> This method is invoked by put and putAll after inserting a new entry into the map. 
> It provides the implementor with the opportunity to remove the eldest entry each time a new one is added. 
> This is useful if the map represents a cache: it allows the map to reduce memory consumption by deleting stale entries.

2. 为服务端添加限流功能

2.1 使用Guava RateLimiter 控制连接产生的速度。
```java
    RateLimiter limiter = RateLimiter.create(1.0);
    while ((socket = serverSocket.accept()) != null){
        limiter.acquire();
        threadPool.execute(new HttpServer(socket));
    }
```

2.2 使用CountdownLatch 保证服务端同时发送多个连接。
```java
    //在所有线程准备好之前阻塞消息
    countDownLatch.await();
    output.flush();
```

```java
    for(int i=0; i<10; i++){
        ClientConn clientConn = new ClientConn();
        clientConn.start();
        //每准备好一个线程，计数减一
        countDownLatch.countDown();
    }
```

    如图可见，每秒仅会处理一次连接。
![实验结果](pic/img_1.png)

参考：
- [Java 从零开始手撸一个 HTTP 服务器](https://blog.csdn.net/rizero/article/details/111410244)
- Java 并发开发的艺术：一个基于线程池技术的简单服务器
