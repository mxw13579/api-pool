[根目录](../CLAUDE.md) > **web**

# API号池前端应用模块

## 变更记录 (Changelog)

### 2025-08-27 02:12:51
- 初始化前端模块文档
- 完成Vue.js应用架构扫描和分析
- 添加路由配置、组件结构、API接口封装说明
- 建立前端开发规范和最佳实践指引
- 完善构建配置和开发环境设置

## 模块职责

Vue.js 3前端单页面应用，提供API号池管理系统的用户界面，负责：
- 用户交互界面的渲染和响应
- 与后端API的通信和数据展示
- 用户认证状态管理和路由守护
- 响应式数据绑定和状态管理
- 表单验证和用户输入处理
- 组件化UI构建和复用

## 入口与启动

**主入口文件**：`src/main.ts`
```typescript
import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'
```

**启动命令**：
```bash
# 开发模式
npm run dev         # 启动开发服务器 (localhost:5173)

# 生产构建
npm run build       # TypeScript编译 + Vite构建

# 预览构建结果
npm run preview     # 预览生产构建
```

**开发服务器配置**：
- 开发端口：5173
- 后端代理：`/api` -> `http://localhost:8080`
- 热更新：支持

## 对外接口

### 主要页面路由
| 路由路径 | 组件 | 页面标题 | 访问权限 |
|---------|------|---------|---------|
| `/` | Layout | 主布局 | 需要认证 |
| `/pool` | Pool/Index.vue | 号池管理 | 需要认证 |
| `/proxy` | Proxy/Index.vue | 代理管理 | 需要认证 |
| `/account` | Account/Index.vue | 账户管理 | 需要认证 |
| `/index.html` | Login/Index.vue | 登录页面 | 公开访问 |

### 认证机制
**Token管理**：
```typescript
// SessionStorage存储
auth_token: string              // 认证令牌
auth_token_expiry: string       // 过期时间戳

// 路由守护
router.beforeEach((to, _from, next) => {
    const token = getToken();
    if (to.meta.isPublic || token) {
        next();
    } else {
        next({ name: 'Login' });
    }
});
```

## 关键依赖与配置

### 核心技术栈
```json
{
  "dependencies": {
    "vue": "^3.4.27",                    // Vue.js 3框架
    "vue-router": "^4.3.3",             // Vue路由管理
    "element-plus": "^2.7.5",           // UI组件库
    "axios": "^1.7.2"                   // HTTP客户端
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^5.0.5",     // Vite Vue插件
    "typescript": "^5.2.2",             // TypeScript支持
    "unplugin-auto-import": "^0.17.6",  // 自动导入
    "unplugin-vue-components": "^0.27.0", // 组件自动导入
    "vite": "^5.2.0",                   // 构建工具
    "vue-tsc": "^2.0.21"                // Vue TypeScript编译器
  }
}
```

### 构建配置 (`vite.config.ts`)
```typescript
export default defineConfig({
  plugins: [
    vue(),
    AutoImport({ resolvers: [ElementPlusResolver()] }),
    Components({ resolvers: [ElementPlusResolver()] }),
  ],
  resolve: {
    alias: { '@': path.resolve(__dirname, './src') }
  },
  server: {
    proxy: { '/api': { target: 'http://localhost:8080', changeOrigin: true } }
  }
});
```

### Element Plus集成
- **自动导入**：组件和API自动按需导入
- **主题样式**：使用默认主题 + 自定义样式
- **图标库**：Element Plus内置图标

## 数据模型

### TypeScript类型定义 (`src/types/index.ts`)

**基础接口**：
```typescript
interface BaseEntity {
    id: number;
    createdAt?: number;
    updatedAt?: number;
    deletedAt?: number;
    createdBy?: string;
    updatedBy?: string;
    deletedBy?: string;
}

interface ApiResponse<T = any> {
    code: number;
    msg: string;
    data: T;
}
```

**核心实体类型**：
| 类型 | 对应后端实体 | 主要字段 |
|------|-------------|---------|
| `PoolEntity` | PoolEntity | name, endpoint, username, password, address |
| `AccountEntity` | AccountEntity | name, username, email, status, role |
| `ProxyEntity` | ProxyEntity | name, proxyUrl, source, address, status |
| `Channel` | ChannelEntity | id, type, key, status, name, baseUrl |

### 分页结果类型
```typescript
interface PageResult<T> {
    items: T[];
    total: number;
    pageNum: number;
    pageSize: number;
}
```

## 测试与质量

### 当前测试状态
- **单元测试**：暂无，建议补充
- **组件测试**：暂无，建议使用Vue Test Utils
- **E2E测试**：暂无，建议使用Playwright或Cypress

### 建议的测试策略
```bash
# 推荐测试工具
npm install --save-dev @vue/test-utils vitest jsdom
npm install --save-dev playwright  # 或 cypress

# 测试脚本建议
"scripts": {
  "test:unit": "vitest",
  "test:e2e": "playwright test"
}
```

### 代码质量工具
- **TypeScript**：类型安全检查
- **Vite**：快速构建和热重载
- **ESLint**：建议添加代码规范检查
- **Prettier**：建议添加代码格式化

## 常见问题 (FAQ)

**Q: 如何添加新的页面？**
A: 
1. 在`src/views/`下创建新的Vue组件
2. 在`src/router/index.ts`中添加路由配置
3. 如需菜单导航，在布局组件中添加菜单项

**Q: 如何调用后端API？**
A: 
1. 在`src/api/`目录下创建对应的API文件
2. 使用封装好的request实例（基于axios）
3. 参考`src/api/pool.ts`中的实现方式

**Q: 如何处理用户认证？**
A: 系统使用SessionStorage存储token，路由守卫自动检查认证状态。登录后token会自动添加到请求头。

**Q: 如何自定义UI组件样式？**
A: 
1. 在`src/assets/theme.css`中添加全局样式
2. 在组件内使用scoped样式进行局部定制
3. 利用Element Plus的CSS变量进行主题定制

**Q: 开发时如何解决跨域问题？**
A: Vite配置中已设置代理，开发时`/api`请求会自动代理到后端服务器。

## 相关文件清单

### 核心目录结构
```
web/
├── public/                    # 静态资源
├── src/
│   ├── main.ts               # 应用入口
│   ├── App.vue               # 根组件
│   ├── assets/               # 静态资源
│   │   ├── main.css         # 全局样式
│   │   └── theme.css        # 主题样式
│   ├── api/                  # API接口封装
│   │   ├── index.ts         # axios配置和请求拦截
│   │   ├── pool.ts          # 号池相关API
│   │   ├── proxy.ts         # 代理相关API
│   │   └── account.ts       # 账户相关API
│   ├── components/           # 通用组件
│   │   ├── DialogManager.vue      # 对话框管理器
│   │   ├── PoolCard.vue          # 号池卡片组件
│   │   └── ChannelDetailDialog.vue # 渠道详情对话框
│   ├── views/                # 页面组件
│   │   ├── login/
│   │   │   └── Index.vue     # 登录页面
│   │   ├── pool/
│   │   │   └── Index.vue     # 号池管理页面
│   │   ├── proxy/
│   │   │   └── Index.vue     # 代理管理页面
│   │   └── account/
│   │       └── Index.vue     # 账户管理页面
│   ├── layout/               # 布局组件
│   │   └── Index.vue         # 主布局
│   ├── router/               # 路由配置
│   │   └── index.ts          # 路由定义和守卫
│   ├── types/                # TypeScript类型定义
│   │   └── index.ts          # 通用类型
│   └── utils/                # 工具函数
│       └── maps.ts           # 映射工具
├── package.json              # 项目配置和依赖
├── tsconfig.json            # TypeScript配置
├── tsconfig.node.json       # Node.js TypeScript配置
├── vite.config.ts           # Vite构建配置
└── index.html               # HTML入口模板
```

### 主要组件功能
- **App.vue**：根组件，提供应用框架
- **Layout/Index.vue**：主布局，包含导航菜单和内容区域
- **Pool/Index.vue**：号池管理，包含号池列表、添加、编辑功能
- **PoolCard.vue**：号池卡片，展示号池信息和操作按钮
- **ChannelDetailDialog.vue**：渠道详情弹窗，用于查看和编辑渠道信息

### API接口封装
- **api/index.ts**：axios实例配置，请求/响应拦截器
- **api/pool.ts**：号池相关API（增删改查、延迟测试、统计信息）
- **api/proxy.ts**：代理服务器相关API
- **api/account.ts**：账户管理相关API