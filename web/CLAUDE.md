[根目录](../../CLAUDE.md) > **web**

# 前端应用模块 (frontend-webapp)

## 模块职责

前端应用模块是API号池管理系统的用户界面实现，基于Vue.js 3构建的单页面应用(SPA)。提供直观友好的管理界面，涵盖号池管理、代理管理、账户管理等核心功能，以及系统监控和数据可视化展示。

## 入口与启动

### 主入口文件
- **文件路径**：`src/main.ts`
- **应用初始化**：Vue应用创建、Element Plus组件库注册、路由挂载
- **启动方式**：
  ```bash
  cd web
  npm install
  npm run dev
  ```
- **访问地址**：http://localhost:5173

### 项目配置
- **构建工具**：Vite 5.2.0
- **TypeScript配置**：`tsconfig.json` + `tsconfig.node.json`
- **Vite配置**：`vite.config.ts` - 自动导入、组件自动注册等

## 对外接口

### 路由结构
```typescript
/ (Layout容器)
├── /pool          # 号池管理页面
├── /proxy         # 代理管理页面
├── /account       # 账户管理页面
└── /index.html    # 登录页面（公共访问）
```

### 页面功能模块
1. **登录模块** (`views/login/Index.vue`)
   - 用户身份验证
   - Token存储管理

2. **号池管理** (`views/pool/Index.vue`) 
   - 号池列表展示与操作
   - 渠道管理和配置
   - 延迟测试和监控

3. **代理管理** (`views/proxy/Index.vue`)
   - 代理服务器管理
   - 代理与号池绑定关系

4. **账户管理** (`views/account/Index.vue`)
   - 用户账户信息管理

## 关键依赖与配置

### 核心依赖
```json
{
  "dependencies": {
    "vue": "^3.4.27",           // Vue.js 3框架
    "vue-router": "^4.3.3",     // 路由管理
    "element-plus": "^2.7.5",   // UI组件库
    "axios": "^1.7.2"          // HTTP客户端
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^5.0.5",
    "typescript": "^5.2.2",
    "vite": "^5.2.0",
    "unplugin-auto-import": "^0.17.6",    // 自动导入
    "unplugin-vue-components": "^0.27.0"  // 组件自动注册
  }
}
```

### 开发工具配置
- **自动导入**：Vue API、Element Plus组件自动导入
- **类型支持**：完整的TypeScript类型定义
- **组件自动注册**：Element Plus组件按需加载

## 数据模型

### API接口封装
1. **基础API** (`api/index.ts`)
   - Axios实例配置
   - 请求/响应拦截器
   - 错误处理机制

2. **业务API模块**
   - `api/pool.ts` - 号池相关API
   - `api/proxy.ts` - 代理相关API  
   - `api/account.ts` - 账户相关API

### 类型定义
- **文件位置**：`src/types/index.ts`
- **内容**：接口响应类型、业务实体类型定义

### 工具函数
- **文件位置**：`src/utils/maps.ts`
- **功能**：数据映射、格式转换等工具方法

## 测试与质量

### 测试现状
- **当前状态**：暂无测试套件
- **建议补充**：
  - 组件单元测试：Vue Test Utils + Jest/Vitest
  - E2E测试：Cypress或Playwright
  - API Mock测试：MSW

### 代码质量
- **TypeScript**：强类型支持，减少运行时错误
- **Element Plus**：统一的UI组件规范
- **模块化**：清晰的目录结构和职责划分

## 常见问题 (FAQ)

### Q: 如何新增一个页面？
A: 
1. 在`views/`目录下创建新的Vue组件
2. 在`router/index.ts`中添加路由配置
3. 如需API调用，在对应的`api/`文件中添加接口方法

### Q: 如何修改主题样式？
A: 
1. 全局样式：修改`assets/main.css`或`assets/theme.css`
2. Element Plus主题：参考Element Plus官方主题定制文档

### Q: 认证机制如何实现？
A: 
1. 登录成功后将token存储在localStorage
2. 路由守卫检查token有效性
3. axios请求拦截器自动添加认证头

### Q: 如何处理API错误？
A: 
1. axios响应拦截器统一处理HTTP错误
2. 业务错误通过Element Plus的Message组件显示
3. 网络错误提供友好的用户提示

## 相关文件清单

### 核心目录结构
```
web/
├── src/
│   ├── api/              # API接口封装
│   ├── assets/           # 静态资源
│   ├── components/       # 可复用组件
│   ├── layout/          # 布局组件
│   ├── router/          # 路由配置
│   ├── types/           # TypeScript类型定义
│   ├── utils/           # 工具函数
│   ├── views/           # 页面组件
│   │   ├── account/     # 账户管理页面
│   │   ├── login/       # 登录页面  
│   │   ├── pool/        # 号池管理页面
│   │   └── proxy/       # 代理管理页面
│   ├── App.vue          # 根组件
│   └── main.ts          # 应用入口
├── index.html           # HTML模板
├── package.json         # 依赖配置
├── vite.config.ts       # Vite配置
├── tsconfig.json        # TypeScript配置
└── components.d.ts      # 组件类型声明(自动生成)
```

### 重要文件
- `main.ts` - 应用入口和初始化
- `router/index.ts` - 路由配置和导航守卫
- `App.vue` - 根组件
- `layout/Index.vue` - 主布局组件
- `vite.config.ts` - 构建配置

### 页面组件
- `views/login/Index.vue` - 登录页面
- `views/pool/Index.vue` - 号池管理页面
- `views/proxy/Index.vue` - 代理管理页面
- `views/account/Index.vue` - 账户管理页面

## 变更记录 (Changelog)

### 2025-08-24 23:51:47
- 初始化前端应用模块文档
- 梳理组件结构和API封装
- 建立开发指引和FAQ
- 标记测试补充需求