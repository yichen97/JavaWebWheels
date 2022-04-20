## 服务器功能
1. 能够访问网页
2. 能够读取文本信息
3. 能处理请求异常
4. 能持续处理请求

## HTTP生命周期
1. read: 读取socket数据流
2. parser: 解析数据流，分析报头，得到客户端的命令语义
3. process: 处理客户端命令，返回结果
4. reponse: 将处理结果打包，增加报头
5. write: 写入socket数据流

**一定要注意报文格式，头和体之间有一个"\r\n"**
HTTP 1.1 请求报文格式
![img.png](pic/img.png)
HTTP 1.1 响应报文格式
![img.png](pic/response.png)
