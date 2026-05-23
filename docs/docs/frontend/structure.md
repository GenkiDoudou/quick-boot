# 前端项目结构

## 顶层目录

```text
quick-ui/
├── package.json
├── vite.config.js
├── .env.development / .env.production
└── src/
    ├── main.js              # 入口：Pinia、Router、C7、插件、指令
    ├── permission.js        # 路由守卫
    ├── settings.js          # 应用配置、白名单
    ├── api/                 # 接口模块
    ├── views/               # 页面
    ├── packages/            # C7 组件库
    ├── components/          # 通用/布局组件
    ├── layout/              # 主布局
    ├── router/index.js      # 常量路由
    ├── store/               # Pinia
    ├── utils/               # 工具
    ├── plugins/             # 全局插件
    ├── directive/           # 权限指令
    └── assets/              # 样式、SVG
```

## 路由模型

**常量路由**（`router/index.js`）：登录、404、首页、字典数据子页、代码生成编辑页等。

**动态路由**（`store/modules/permission.js`）：

1. 用户登录成功
2. 调用 `getRouters` 获取菜单树
3. 将后端 `component` 字段映射为 `views/**/*.vue`
4. `router.addRoute` 挂载到 Layout 下

OAuth 管理菜单通常由 Flyway（如 `V26__oauth2_menu.sql`）下发，**不在**常量路由硬编码。

## 状态管理（Pinia）

| 模块 | 职责 |
|------|------|
| `user` | Token、用户信息、角色 |
| `permission` | 路由、侧边栏菜单 |
| `settings` | 主题、布局、标签页 |
| `dict` | 字典缓存（若启用） |
| `tagsView` | 多页签 |

## 请求链路（`utils/request.js`）

1. 从 Cookie 读取 `Admin-Token`，写入 `Authorization: Bearer …`
2. `applyClientSignHeaders`：按 canonical 算法加 `X-Client-*` 头
3. 响应统一处理：`code !== 200` 提示、401 触发重登
4. 下载场景单独处理 blob

签名实现与后端一致：`utils/clientSign.js`。

## C7 组件库（`packages/`）

通过 `packages/index.js` → `installPackages(app)` 全局注册。

| 组件 | 用途 |
|------|------|
| C7JsonTable / C7JsonTableColumn | JSON 配置列表（核心） |
| C7JsonForm | JSON 配置表单 |
| C7ExcelUpload / C7ExcelDownload | 导入导出 |
| C7DictTag | 字典标签 |
| C7Dialog、C7MessageBox | 弹窗 |
| C7DatePicker、C7TimePicker、C7TreeSelect、C7Cascader | 表单控件 |
| C7Button、C7Card、C7Title、C7Watermark 等 | 布局与展示 |

组件文档目录：`docs/frontend/components/通用组件/`（持续补齐）。

## 权限

- **路由级**：动态菜单 + 后端 `perms`
- **按钮级**：`v-hasPermi="['system:user:add']"` 
- **编程式**：`checkPermission`、`$auth.hasPermi`

## 列表页规范

新建业务列表页应：

1. 优先使用 **C7JsonTable**
2. 布局/样式参照 `src/views/system/config/index.vue`
3. 遵循仓库 `DESIGN.md` 与 `sdd/前端代码规范.md`

## 相关文档

- [前端概述](./index)
- [业务页面总览](./modules/index)
