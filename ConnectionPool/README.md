### ODBC
ODBC（Open Database Connectivity，开放数据库互连）提供了一种标准的API（应用程序编程接口）方法来访问数据库管理系统（DBMS）。 
这些API利用SQL来完成其大部分任务。ODBC本身也提供了对SQL语言的支持，用户可以直接将SQL语句送给ODBC。
ODBC的设计者们努力使它具有最大的独立性和开放性：与具体的编程语言无关，与具体的数据库系统无关，与具体的操作系统无关。


### JDBC
(Java DataBase Connectivity)，**独立于特定数据库管理系统、通用的SQL数据库存取和操作的公共接口（一组API）**，
定义了用来访问数据库的标准Java类库，使用这个类库可以以一种标准的方法、方便地访问数据库资源。

**JDBC接口（API）**包括两个层次：
- 面向应用的`API：Java API`，抽象接口，供应用程序开发人员使用（连接数据库，执行SQL语句，获得结果）。
- 面向数据库的`API：Java Driver API`，供开发商开发数据库驱动程序用。

JDBC 1.0 随JDK1.1一起发布，JDBC操作相关的接口和类位于java.sql包中。

包含了DriverManager类，Driver接口，Connection接口，Statement接口，ResultSet接口，SQLException 类等。



### 连接池的意义
节省连接建立的消耗。

### 数据源是什么 
JDBC2.0 提供了javax.sql.DataSource接口，它负责建立与数据库的连接，直接引用DataSource 即可获取数据库的连接对象。
用于获取操作数据Connection对象。

常用的数据库连接池技术：
C3P0、DBCP、Proxool和DruidX

[参考1](https://www.cnblogs.com/knowledgesea/p/11202918.html)
[参考2](https://www.cnblogs.com/jianshu/p/6023098.html)
[参考3](https://blog.csdn.net/a3427603/article/details/86449198)