[根目录](../CLAUDE.md) > **backend-core**

# 后端核心模块 (backend-core)

## 模块职责

后端核心模块是API号池管理系统的服务端实现，基于Spring Boot 3.0.2框架构建。负责提供完整的RESTful API服务，包括用户认证、号池管理、代理服务器管理、账户管理以及系统监控等核心业务功能。

## 入口与启动

### 主启动类
- **文件路径**：`src/main/java/com/fufu/apipool/ApiPoolApplication.java`
- **启动方式**：
  ```bash
  # 开发模式
  mvn spring-boot:run
  
  # 生产模式
  java -jar target/api-pool-0.0.1-SNAPSHOT.jar
  ```
- **端口配置**：默认8080端口

### 配置文件
- **应用配置**：`src/main/resources/application.properties`
- **数据库配置**：SQLite，文件位置`./data/apipool.db`
- **MyBatis映射**：`src/main/resources/mapper/*.xml`

## 对外接口

### API端点总览
| 模块 | 基础路径 | 主要功能 |
|------|---------|---------|
| 号池管理 | `/api/pool` | 号池CRUD、渠道管理、延迟测试 |
| 代理管理 | `/api/proxy` | 代理服务器管理、绑定关系 |
| 账户管理 | `/api/account` | 用户账户管理、认证 |

### 核心API接口
1. **号池管理接口**
   - `GET /api/pool/list` - 查询所有号池
   - `POST /api/pool/add` - 新增号池
   - `PUT /api/pool/update` - 更新号池
   - `DELETE /api/pool/delete/{id}` - 删除号池
   - `GET /api/pool/test-latency/{id}` - 测试号池延迟

2. **渠道管理接口**
   - `GET /api/pool/getChannelsByPoolId` - 获取号池渠道列表
   - `POST /api/pool/addChannelsByPoolId/{poolId}` - 为号池添加渠道
   - `PUT /api/pool/updateChannelByPoolId/{poolId}` - 更新渠道信息

3. **代理管理接口**
   - 提供代理服务器的增删改查功能
   - 代理与号池的绑定关系管理

## 关键依赖与配置

### Maven依赖
```xml
<!-- 核心框架 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- 认证框架 -->
<dependency>
    <groupId>cn.dev33</groupId>
    <artifactId>sa-token-spring-boot3-starter</artifactId>
    <version>1.37.0</version>
</dependency>

<!-- 数据库相关 -->
<dependency>
    <groupId>org.xerial</groupId>
    <artifactId>sqlite-jdbc</artifactId>
    <version>3.43.2.0</version>
</dependency>
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter</artifactId>
    <version>3.0.2</version>
</dependency>

<!-- HTTP客户端 -->
<dependency>
    <groupId>com.squareup.okhttp3</groupId>
    <artifactId>okhttp</artifactId>
    <version>4.11.0</version>
</dependency>
```

### 配置类
- **SaTokenConfig** - 认证配置
- **PasswordConfig** - 密码加密配置  
- **ExecutorConfig** - 线程池配置

## 数据模型

### 核心实体
1. **PoolEntity** - 号池实体
   - 包含：name, endpoint, username, password, address
   - 监控配置：monitoringIntervalTime, minActiveChannels, maxMonitorRetries

2. **AccountEntity** - 账户实体
3. **ProxyEntity** - 代理实体
4. **ChannelEntity** - 渠道实体
5. **ErrorLogEntity** - 错误日志实体

### 数据访问层
- **Mapper接口**：提供数据访问抽象
- **XML映射文件**：MyBatis SQL映射配置
- **位置**：`src/main/resources/mapper/`

## 测试与质量

### 测试结构
- **测试目录**：`src/test/java/com/fufu/apipool/`
- **测试类**：`ApiPoolApplicationTests.java` (主要的应用启动测试)
- **测试框架**：JUnit 5 + Spring Boot Test

### 代码质量
- **Lombok使用**：简化实体类代码，减少样板代码
- **统一异常处理**：`AllExceptionHandle` 全局异常处理器
- **统一响应格式**：`Result` + `ResultUtil` 响应包装

## 常见问题 (FAQ)

### Q: 如何添加新的API接口？
A: 
1. 在对应的Controller中添加方法
2. 在Service层实现业务逻辑
3. 如需数据库操作，在Mapper和XML中添加SQL映射
4. 使用`@RestController`和`@RequestMapping`注解

### Q: 如何修改数据库结构？
A: 
1. 修改对应的Entity类
2. 更新Mapper XML中的SQL语句
3. 如需新表，考虑添加初始化脚本

### Q: 认证机制如何工作？
A: 系统使用sa-token进行认证，配置在`SaTokenConfig`中。前端需要在请求头中携带token。

## 相关文件清单

### 核心目录结构
```
src/main/java/com/fufu/apipool/
├── common/           # 通用组件
│   ├── config/      # 配置类
│   ├── constant/    # 常量定义
│   ├── handle/      # 异常处理
│   └── task/        # 定时任务
├── controller/       # 控制器层
├── entity/          # 实体类
├── service/         # 业务服务层
│   └── impl/        # 服务实现
├── mapper/          # 数据访问层
├── domain/          # 领域对象
│   ├── dto/         # 数据传输对象
│   ├── vo/          # 视图对象
│   └── newapi/      # 新API相关对象
└── monitor/         # 监控相关
```

### 重要文件
- `ApiPoolApplication.java` - 主启动类
- `application.properties` - 应用配置
- `pom.xml` - Maven依赖管理
- `src/main/resources/mapper/` - MyBatis映射文件

## 变更记录 (Changelog)

### 2025-08-24 23:51:47
- 初始化后端核心模块文档
- 梳理API接口和数据模型
- 建立开发规范和FAQ指引