# quick-h5 用户管理（一期）设计

日期：2026-08-15  
状态：待用户审阅  
范围：工作台「用户」入口 → H5 用户列表与常用操作（对接现有 `/sys/user/*`）

## 1. 背景与目标

工作台 mock 菜单已有「用户」入口，但仅 toast「待接后台菜单」，无页面与 API。  
一期补齐 **列表 + 新增/编辑/启停/重置密码**，复用管理端同一套后端接口，便于与 quick-ui 数据一致。

## 2. 已确认决策

| 项 | 选择 |
|----|------|
| 能力范围 | 列表 + 新增/编辑/启停/重置密码（方案 B） |
| 表单字段 | 精简：账号、昵称、密码（仅新增）、手机、状态；**新增须选角色**（后端 `@NotEmpty`）；编辑静默回传原 `roleIds` |
| 交互结构 | 列表页 + 独立表单页；启停在列表；重置密码弹层（方案 A） |
| 后端策略 | 直连现有 `/sys/user/*`（及角色列表 `POST /sys/role/page`），不新增 H5 专用接口 |
| 非目标 | 部门、删除、导入导出、分配角色页、前端按钮级权限指令 |

## 3. 信息架构

### 3.1 入口

- 工作台「用户」→ `navigateTo` `/pages/system/user/index`
- `workbenchMenus.ts` 为该项配置 `path`
- `workbench.vue`：有 `path` 则跳转，否则保留 toast

### 3.2 页面

| 路径 | 职责 |
|------|------|
| `pages/system/user/index` | 搜索、分页列表、新增入口、行内编辑/启停/重置密码 |
| `pages/system/user/form` | 新增或编辑；`userId` query 有则编辑、无则新增 |

## 4. 接口约定

封装文件：`quick-h5/src/api/system/user.ts`（对齐 `quick-ui/src/api/system/user.js` 子集）。

| 方法 | HTTP | 说明 |
|------|------|------|
| `pageUser` | `POST /sys/user/page` | body：`{ current, size, param: { userName? } }` |
| `getUser` | `GET /sys/user/{userId}` | 编辑回填 |
| `addUser` | `POST /sys/user/add` | 精简字段；密码空则后端默认 `admin123` |
| `updateUser` | `POST /sys/user/update` | 不传密码则不改密；本期不传 `roleIds` 以免清空角色 |
| `changeUserStatus` | `POST /sys/user/changeStatus` | `{ userId, status }`，`0` 正常 / `1` 停用 |
| `resetUserPwd` | `POST /sys/user/resetPwd` | `{ userId, password }` |

统一走 `src/api/http.ts` 的 `request()`（Bearer、业务码、401 跳登录）。

## 5. 交互与校验

### 5.1 列表

- 搜索：用户账号（`userName`），确认后刷新第 1 页
- 下拉刷新：重置分页；触底加载下一页，无更多提示「没有更多了」
- 卡片展示：昵称、账号、手机、状态
- 超管：`userId === 1`（或字符串 `'1'`）禁止停用（与 quick-ui 一致）

### 5.2 表单

- 必填：`userName`、`nickName`
- 新增：`userName` 可编辑；`password` 可选；`status` 默认 `'0'`
- 编辑：账号只读；不传 `roleIds`/`deptId`（避免误清空）
- 提交成功：toast + `navigateBack`；列表页 `onShow` 刷新

### 5.3 重置密码

- 列表行操作打开弹层/输入框，密码非空后提交
- 成功 toast「密码已重置」

### 5.4 错误

- 业务/网络错误：`uni.showToast({ title: message, icon: 'none' })`
- 403：展示后端文案或「无权限」
- 401：现有 http 逻辑 reLaunch 登录页

## 6. 文件清单

| 文件 | 动作 |
|------|------|
| `src/api/system/user.ts` | 新增 |
| `src/pages/system/user/index.vue` | 新增 |
| `src/pages/system/user/form.vue` | 新增 |
| `src/pages.json` | 注册上述两页 |
| `src/mock/workbenchMenus.ts` | 用户项补 `path` |
| `src/pages/workbench/workbench.vue` | 按 `path` 跳转 |

UI：uView Pro + 现有 `.qb-page` 视觉 token（绿主色）。

## 7. 验收标准

1. 工作台点「用户」进入列表页（非 toast）
2. 可按账号搜索并分页加载
3. 可新增用户，管理端可见同一账号
4. 可编辑昵称/手机/状态并保存
5. 可启停（超管除外）
6. 可重置密码并登录验证（可选冒烟）
7. 未登录或 token 失效跳转登录

## 8. 风险与后续

- 后端 `add` 允许无角色；用户无角色时管理端部分能力可能受限——一期可接受，二期再补角色多选
- 工作台菜单仍为 mock；正式改为后端下发菜单时，需保证 path 与权限码一致
- 二期可选：删除、部门/角色、前端 `v-hasPermi` 等价能力
