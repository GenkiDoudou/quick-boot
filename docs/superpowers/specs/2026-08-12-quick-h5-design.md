# quick-h5 移动端（uni-app）设计

日期：2026-08-12  
状态：已确认 Tab IA（2026-08-12）；登录脚手架已落地，壳层按本节扩展  
参考：`bak/h5`（uni-app Vue3 + uView Pro starter / 业务雏形）；静态原型 `docs/demo/quick-h5-tab-prototype.html`

## 1. 背景与目标

在 quickboot monorepo 中新增独立的多端客户端包 **`quick-h5/`**，对接现有 Spring Boot 后端的登录认证，作为管理端 `quick-ui` 之外的 H5 / 微信小程序入口。

**不做**整仓复制 `bak/h5`；采用**官方/社区 uni-app Vue3 脚手架新建**，再从 `bak/h5` **按需移植**工程能力（HTTP 拦截思路、uView Pro、Pinia 持久化、登录页结构等）。

## 2. 已确认决策

| 项 | 选择 |
|----|------|
| 定位 | 脚手架 + 登录 + **三 Tab 壳**（首页 / 工作台 / 我的）；砍掉 bak 任务等业务页 |
| 运行端 | 完整多端能力保留；第一版验收 H5 + 微信小程序脚手架可跑 |
| 业务对接 | 不补任务域后端；不保留任务/分类/四象限页面 |
| 目录 | `quick-h5/`（与 `quick-ui` 并列） |
| OAuth | 新建独立 client（`clientId=quick-h5`），仍走现有 Sa-Token / `/login` |
| 实现路径 | 方案 2：新建脚手架 + 移植，非整目录拷贝 |

## 3. 整体架构

```text
quickboot/
├── quick-ui/       # 管理端 Element Plus
├── quick-h5/       # 新增：uni-app 多端客户端
├── quickboot/      # 后端：复用 POST /login、GET /auth/me；新增 oauth client 种子
└── bak/h5/         # 仅参考，非运行入口
```

- H5：`pnpm dev:h5` / `pnpm build:h5`
- 微信小程序：`pnpm dev:mp-weixin`（开发者工具可打开）

## 4. 工程结构（目标）

```text
quick-h5/
├── package.json
├── vite.config.ts
├── uno.config.ts              # 可选；非第一版硬性要求
├── index.html
├── .env.development           # VITE_APP_BASE_API、VITE_OAUTH_CLIENT_ID/SECRET
├── src/
│   ├── main.ts
│   ├── App.vue
│   ├── pages.json             # login + tabBar(home/workbench/mine) + 子页
│   ├── manifest.json          # H5 + mp-weixin
│   ├── api/
│   │   ├── auth.ts
│   │   └── http.ts              # 后续可加 workbench 菜单 API
│   ├── mock/                    # 壳层 mock（菜单/消息/待办）；接 API 后替换
│   ├── utils/
│   │   └── oauthClientBasic.ts  # 与 quick-ui 同协议
│   ├── stores/
│   │   ├── index.ts
│   │   └── user.ts
│   ├── pages/
│   │   ├── login/login.vue
│   │   ├── home/home.vue        # 快捷入口 · 消息 · 今天待办
│   │   ├── workbench/workbench.vue  # 后台可配置菜单
│   │   └── mine/
│   │       ├── mine.vue         # 入口列表 + 退出
│   │       ├── profile.vue      # 个人信息
│   │       ├── contact.vue      # 联系我们
│   │       └── about.vue        # 关于 / 清缓存入口说明
│   ├── components/
│   ├── common/
│   └── static/tab/              # tabBar 图标
└── README.md
```

页面：**login** + Tab（**home / workbench / mine**）+ 我的子页（profile / contact / about）。

## 4.0 信息架构（Tab）

| Tab | 区块 | 说明 |
|-----|------|------|
| **首页** | 快捷入口 | 常用能力宫格；首版可本地 mock，后续可与工作台同源或独立配置 |
| | 消息 | 列表（类型标签、未读态、时间）；首版 mock |
| | 今天待办 | 当日待办（勾选态 / 优先级）；首版 mock，**非** bak 任务域整页搬迁 |
| **工作台** | 菜单 | **须后台可配置**（分组、排序、显隐、图标、跳转）；首版用 `mock` 模拟下发结果 |
| **我的** | 个人信息 | 展示 `/auth/me` 字段（用户名 / 昵称 / ID）；编辑能力后续按需 |
| | 联系我们 | 静态联系方式页（可后续改 CMS） |
| | 关于 / 清缓存 | 版本号；清缓存不误清登录态（或清后要求重登，须产品确认——默认**保留 token**） |
| | 退出登录 | 清本地态 + `reLaunch` 登录页 |

### 工作台菜单配置（约定）

- **目标**：管理端可配置 H5 工作台入口；客户端按登录用户权限拉取并渲染分组宫格。
- **首版**：`quick-h5/src/mock/workbenchMenus.ts` 本地数据，页面结构与真实 API 对齐（`groups[] → items[]`）。
- **后续后端**（不在登录脚手架必做范围）：复用 / 扩展 `sys_menu`（或独立 H5 菜单表）+ 管理端配置页 + H5 拉取接口；与 quick-ui 菜单可同源过滤 `client`/`platform` 或独立树。

### 4.1 从 `bak/h5` 移植

- uni + uView Pro + Pinia 初始化方式（按新脚手架接入）
- HTTP 拦截骨架 → 重写为解包 `R`、Basic/Bearer、401 回登录
- `user` store 字段思路（去掉 aitodo 专用逻辑）
- 登录页 UI 结构 → 改调本仓库 `/login`
- 按需：`app-page` 页面壳（登录页需要自定义导航时再加）

### 4.2 明确不移植

- `pages/tasks*`、`categories`、`quadrant`、`summary-jobs`、`admin/users` 及对应 API/组件
- uView Starter 演示页（`home/*-demo`、`about/*`）
- 硬编码且不解包 `R` 的旧 HTTP 客户端写法

### 4.3 脚手架来源

优先 uView Pro 官方 starter 或 `@dcloudio/uni-preset-vue` Vue3 TS 模板；依赖版本尽量贴近 `bak/h5` 已验证组合，降低冲突。UnoCSS 非必须，可后加。

## 5. 登录 / OAuth / 数据流

### 5.1 协议（与 `quick-ui` 对齐）

- 未登录（含 `POST /login`）：`Authorization: Basic <混淆(clientId:clientSecret)>`  
  混淆算法与 `quick-ui` 的 `oauthClientBasic` 一致（XOR 盐 `QuickBootOAuth1` + URL-safe Base64）
- 已登录：`Authorization: Bearer <accessToken>`
- 响应：`R { code, msg, data }`；登录成功判定与 `quick-ui` 现网成功码一致；`data` 含 `accessToken`、`tokenName`
- 登录成功后**必须**调用 `GET /auth/me` 填充用户名/昵称（失败则提示并可退回登录）

### 5.2 OAuth client

| 项 | 约定 |
|----|------|
| clientId | `quick-h5` |
| clientSecret | 实现时生成；开发种子 + `.env.development`；不提交生产密钥 |
| 用途 | 仅 H5/小程序 Client Basic，与管理端 client 分离 |
| 落库 | 优先 Flyway migration 插入 `sys_oauth_client`（与现网 client 种子风格一致） |

### 5.3 前端流程

1. 启动读 Pinia / `uni.storage` 登录态  
2. 未登录 → `reLaunch` 登录页  
3. 账号密码 → `POST /login`（Basic）→ 存 token  
4. 调用 `/auth/me` → `reLaunch` / `switchTab` 进 **首页 Tab**  
5. 后续请求 Bearer；业务 `code` 未授权或 HTTP 401 → 清态回登录  
6. 退出：清本地；若现网有 logout 则调用，否则仅本地清态；Tab 间用 `switchTab`  

### 5.4 环境

- H5：Vite 代理或 `VITE_APP_BASE_API`（对齐 `quick-ui` 习惯，或直连 + CORS）
- 小程序：独立可访问 `baseUrl`；开发期可关合法域名校验
- 验证码：若后端开启，按 `quick-ui` 最小协议对接；否则文档说明本地关闭方式

## 6. 验收标准

1. 存在可独立 `pnpm install` 的 `quick-h5/`
2. `pnpm dev:h5` 打开登录页；系统账号 + `quick-h5` client 可登录进首页 Tab
3. 登录后 Bearer；未登录受保护页回登录
4. Tab：首页含快捷入口 / 消息 / 今天待办；工作台展示可配置菜单（mock）；我的含个人信息 / 联系我们 / 关于·清缓存 / 退出
5. `pnpm dev:mp-weixin` 产物可被微信开发者工具打开
6. 后端存在 `quick-h5` OAuth client 种子；`bak/h5` 仅参考
7. 无 bak 任务/分类/四象限等业务页（首页「今天待办」仅为壳层 mock）

## 7. 风险与缓解

| 风险 | 缓解 |
|------|------|
| 脚手架与 bak 依赖版本冲突 | 版本贴近 bak；Uno 可后加 |
| 小程序无 Vite 代理 | 独立 baseUrl；文档写清开发设置 |
| 验证码卡住第一版 | 最小对接或文档关验证码 |
| secret 误提交 | 开发示例值；生产占位；遵循 gitignore |

## 8. 非目标

- bak 任务域后端与整页业务（分类 / 四象限等）
- 本阶段实现工作台菜单的**管理端配置页与真实 API**（仅约定 + H5 mock）
- 社交 / 短信登录
- App / Harmony 上架发布
- 与 `quick-ui` 共享前端工程（仅共享协议与可复制的 Basic 算法）

## 9. 建议后续流程

1. 壳层 UI 与 design 对齐后，接工作台菜单 API + 管理端配置  
2. 消息 / 待办 / 快捷入口按真实域替换 mock  
3. 需要时 OpenSpec 增量 change 覆盖「H5 菜单配置」  
