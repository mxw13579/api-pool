[根目录](../../../CLAUDE.md) > [src](../../) > [main](../) > [java](.) > **apipool**

# API号池后端核心模块

## 变更记录 (Changelog)

### 2025-08-27 02:12:51
- 完成模块深度扫描和文档重构
- 更新架构说明，包含完整的控制器、服务层、数据访问层
- 添加详细的API接口文档和数据模型说明
- 完善安全配置、异常处理和测试策略描述
- 建立完整的开发指引和FAQ

### 2025-08-24 23:51:47
- 初始化后端核心模块文档
- 梳理API接口和数据模型
- 建立开发规范和FAQ指引

## 模块职责

Spring Boot后端核心模块，提供完整的RESTful API服务，负责：
- API请求的接收和响应处理
- 业务逻辑处理和数据验证
- 数据持久化和事务管理
- 统一异常处理和结果封装
- 认证授权和安全控制
- 定时任务和系统监控功能

## 入口与启动

**主入口类**：`ApiPoolApplication.java`
- 标准Spring Boot启动类，使用`@SpringBootApplication`注解
- 启用定时任务调度：`@EnableScheduling`
- 启动端口：8080

**启动命令**：
```bash
# 开发模式
mvn spring-boot:run

# 生产模式
java -jar target/api-pool-0.0.1-SNAPSHOT.jar
```

**配置文件**：`application.properties`
```properties
server.port=8080
spring.datasource.url=jdbc:sqlite:./data/apipool.db
spring.datasource.driver-class-name=org.sqlite.JDBC
mybatis.mapper-locations=classpath:mapper/*.xml
```

## 对外接口

### 主要控制器
| 控制器 | 路径前缀 | 职责 | 关键接口数量 |
|--------|---------|------|-------------|
| `PoolController` | `/api/pool` | 号池管理 | 13个接口 |
| `AccountController` | `/api/account` | 账户管理 | 登录认证、用户管理 |
| `ProxyController` | `/api/proxy` | 代理管理 | 代理配置和状态 |
| `PoolProxyRelationController` | `/api/pool-proxy` | 号池代理关联 | 关联关系管理 |

### 核心API端点
**号池管理接口**：
```bash
# 基础CRUD
GET    /api/pool/list                    # 获取号池列表
GET    /api/pool/{id}                   # 根据ID获取号池
POST   /api/pool/add                    # 新增号池
PUT    /api/pool/update                 # 更新号池
DELETE /api/pool/delete/{id}            # 删除号池

# 监控功能
GET    /api/pool/test-latency/{id}      # 测试号池延迟
GET    /api/pool/statistics/{id}        # 获取统计信息
GET    /api/pool/error-logs/{id}        # 获取错误日志

# 渠道管理
GET    /api/pool/getChannelsByPoolId           # 根据号池ID获取渠道
POST   /api/pool/batchAddChannelToAll          # 批量为所有号池添加渠道
POST   /api/pool/addChannelsByPoolId/{poolId}  # 为指定号池添加渠道
PUT    /api/pool/updateChannelByPoolId/{poolId} # 更新渠道信息
DELETE /api/pool/deleteChannelByPoolId/{poolId} # 删除渠道
PUT    /api/pool/testChannelByPoolId/{poolId}   # 测试渠道
```

### 统一响应格式
所有接口使用`Result<T>`统一封装：
```json
{
  "code": 200,
  "msg": "success",
  "data": { /* 实际数据 */ }
}
```

## 关键依赖与配置

### 核心依赖技术栈
- **Spring Boot 3.0.2**：核心框架，基于Java 17
- **sa-token 1.37.0**：轻量级认证框架
- **MyBatis Spring Boot Starter 3.0.2**：数据访问层ORM
- **SQLite JDBC 3.43.2.0**：嵌入式数据库驱动
- **OkHttp 4.11.0**：HTTP客户端，用于外部API调用
- **FastJSON2 2.0.47**：高性能JSON处理
- **Hutool 5.8.25**：Java工具类库
- **Lombok**：减少样板代码

### 关键配置类
| 配置类 | 文件路径 | 职责 |
|--------|---------|------|
| `SaTokenConfig` | `common/config/SaTokenConfig.java` | 认证拦截、CORS、SPA路由 |
| `PasswordConfig` | `common/config/PasswordConfig.java` | 密码加密编码器 |
| `ExecutorConfig` | `common/config/ExecutorConfig.java` | 异步任务线程池 |

### 安全配置详情
**认证机制**：
- 使用sa-token提供会话管理
- 除登录接口外，所有API需要认证
- 支持静态资源访问排除

**CORS配置**：
- 允许所有来源的跨域请求
- 支持GET、POST、PUT、DELETE、OPTIONS方法
- 允许携带凭证信息

## 数据模型

### 核心实体类
| 实体类 | 数据表 | 主要字段 | 验证约束 |
|--------|--------|---------|---------|
| `PoolEntity` | pool | name, endpoint, username, password, address | @NotBlank, @Pattern, @Min/@Max |
| `AccountEntity` | account | username, password, email, phone, status, role | 用户认证信息 |
| `ProxyEntity` | proxy | name, proxyUrl, source, address, status | 代理服务器配置 |
| `ChannelEntity` | channel | name, type, status, baseUrl, key | API渠道配置 |
| `ErrorLogEntity` | error_log | poolId, errorMsg, createdAt | 系统错误记录 |
| `PoolProxyRelationEntity` | pool_proxy_relation | poolId, proxyId | 号池代理关联 |

### 基础实体特征
所有实体继承`BaseEntity`，包含：
- `id`：主键
- `createdAt/updatedAt/deletedAt`：时间戳字段
- `createdBy/updatedBy/deletedBy`：审计字段

### 数据访问层架构
**Mapper接口**：
- 定义数据访问方法
- 位置：`mapper/`目录
- 命名规范：`XxxMapper.java`

**XML映射文件**：
- 位置：`src/main/resources/mapper/`
- 实现软删除：使用`deleted_at IS NULL`条件
- 支持复杂查询和结果映射

## 测试与质量

### 现有测试覆盖
**主测试类**：`ApiPoolApplicationTests`
- HTTP客户端集成测试
- 第三方API调用测试（包含登录认证流程）
- JSON序列化/反序列化测试
- 使用HuTool HTTP工具和FastJSON2

**测试运行**：
```bash
mvn test                          # 运行所有测试
mvn test -Dtest=ApiPoolApplicationTests  # 运行指定测试类
```

### 代码质量保障
**统一异常处理**：
- `AllExceptionHandle`：全局异常处理器
- `BusinessException`：业务异常
- `AuthenticationException`：认证异常

**数据验证**：
- Jakarta Validation注解验证
- 实体字段级验证（@NotBlank, @Size, @Pattern等）
- 控制器方法参数验证（@Valid注解）

**代码规范**：
- Lombok注解减少样板代码
- 统一Result结果封装
- 分层架构明确（Controller-Service-Mapper）

## 常见问题 (FAQ)

**Q: 如何添加新的API接口？**
A: 
1. 在对应Controller中添加方法，使用@RequestMapping相关注解
2. 在Service接口和实现类中添加业务逻辑
3. 如需数据库操作，在Mapper接口和XML文件中添加方法
4. 使用@Valid注解进行参数验证，用Result包装返回结果

**Q: 如何处理跨域问题？**
A: 已在`SaTokenConfig`中配置CORS，支持所有来源。生产环境建议指定具体前端地址。

**Q: 数据库表结构如何管理？**
A: 当前使用手动管理，数据库文件位于`./data/apipool.db`。建议后续集成Flyway进行版本控制。

**Q: 如何添加定时任务？**
A: 参考`PoolMonitoringTask`和`SecurityTask`，使用@Scheduled注解，已在主类启用@EnableScheduling。

**Q: 认证失败如何处理？**
A: sa-token会自动抛出异常，由`AllExceptionHandle`全局异常处理器统一处理并返回相应错误码。

**Q: 如何测试HTTP客户端功能？**
A: 参考`ApiPoolApplicationTests`中的测试方法，使用HuTool的HttpUtil进行HTTP请求测试。

## 相关文件清单

### 核心目录结构
```
src/main/java/com/fufu/apipool/
├── ApiPoolApplication.java         # 主启动类
├── common/                         # 通用组件
│   ├── config/                    # 配置类
│   │   ├── SaTokenConfig.java     # 认证和CORS配置
│   │   ├── PasswordConfig.java    # 密码编码器配置
│   │   └── ExecutorConfig.java    # 线程池配置
│   ├── constant/                  # 常量定义
│   ├── exception/                 # 异常类
│   ├── handle/                    # 异常处理器
│   ├── init/                      # 初始化组件
│   ├── task/                      # 定时任务
│   └── util/                      # 工具类
├── controller/                     # 控制器层
│   ├── PoolController.java        # 号池管理接口
│   ├── AccountController.java     # 账户管理接口
│   ├── ProxyController.java       # 代理管理接口
│   └── PoolProxyRelationController.java  # 关联关系接口
├── entity/                        # 实体类
├── service/                       # 业务服务接口
│   └── impl/                      # 业务服务实现
├── mapper/                        # 数据访问接口
├── domain/                        # 领域对象
│   ├── dto/                      # 数据传输对象
│   ├── vo/                       # 视图对象
│   └── newapi/                   # 第三方API对象
└── monitor/                       # 监控相关
```

### 资源文件
```
src/main/resources/
├── application.properties         # 应用配置
├── mapper/                       # MyBatis映射文件
│   ├── PoolMapper.xml
│   ├── AccountMapper.xml
│   ├── ProxyMapper.xml
│   └── ...
└── static/                       # 静态资源
```

### 测试文件
```
src/test/java/com/fufu/apipool/
└── ApiPoolApplicationTests.java   # 主要测试类
```