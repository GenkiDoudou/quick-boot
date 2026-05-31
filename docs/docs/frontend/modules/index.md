# 业务页面总览

`quick-ui/src/views` 与后端菜单、API 的对应关系。路由 path 以实际 `getRouters` 为准。

## 根级页面

| 文件 | 说明 |
|------|------|
| `index.vue` | 首页仪表盘 |
| `login.vue` | 登录（验证码/手机/二维码、**第三方 OAuth 登录**） |
| `redirect/index.vue` | 路由重定向 |
| `error/401.vue`、`404.vue` | 错误页 |

## 系统管理（`system/`）

| 目录/文件 | API 模块 | 后端 Controller |
|-----------|----------|-----------------|
| `user/index.vue` 等 | `api/system/user.js` | `SysUserController` |
| `role/index.vue` | `api/system/role.js` | `SysRoleController` |
| `dept/` | `api/system/dept.js` | `SysDeptController` |
| `menu/` | `api/system/menu.js` | `SysMenuController` |
| `dict/type`、`dict/data` | `api/system/dict/*` | `DictType/DataController` |
| `config/index.vue` | `api/system/config.js` | `SysConfigController` |
| `notice/index.vue` | `api/system/notice.js` | `SysNoticeController` |
| **`oauthClient/index.vue`** | `api/system/oauthClient.js` | `SysOauthClientController` |
| **`oauthProvider/index.vue`** | `api/system/oauthProvider.js` | `SysOauthProviderController` |
| `user/profile/` | — | 个人中心、改密、头像 |

**列表页参考模板**：`system/config/index.vue`（C7JsonTable + 查询区 + 工具栏）。

## 系统监控（`monitor/`）

| 页面 | API | 后端 |
|------|-----|------|
| `online/index.vue` | `api/monitor/online.js` | `SysUserOnlineController` |
| `operlog/index.vue` | `api/monitor/operlog.js` | `SysOperLogController` |
| `logininfor/index.vue` | `api/monitor/logininfor.js` | `SysLogininforController` |
| `job/index.vue` | `api/monitor/job.js` | `SysJobController` |
| `job-log/index.vue` | `api/monitor/jobLog.js` | `SysJobLogController` |
| **`clientTrack/index.vue`** | `api/monitor/clientTrack.js` | `SysClientTrackController` |
| **`clientTrack/events.vue`** | 同上 | 事件链 |
| **`clientTrack/timeline.vue`** | 同上 | 行为轨迹 |

前端采集 SDK 见 **[用户行为监控](./user-behavior-monitor)**（`src/monitor`）。

## 系统工具（`tool/gen/`）

| 页面 | 说明 |
|------|------|
| `index.vue` | 表列表、导入、生成 |
| `edit.vue` | 生成配置编辑 |
| `components/*Dialog.vue` | 导入表、预览代码、在线建表 |

API：`api/tool/gen.js` → `GenController`。

## OAuth（`oauth/`）

| 页面 | 说明 |
|------|------|
| **`authorize.vue`** | 用户授权确认；同意后跳转后端 `/oauth2/doConfirm` |

登录页联邦：`login.vue` 调用 `api/oauth/authorize.js` → `GET /oauth/login/providers`。

## 开发演示（`dev/`）

C7 组件 E2E 页，菜单由后端动态下发，例如：

- `C7JsonTableE2E`、`C7JsonTableColumnE2E`
- `C7DatePickerE2E`、`C7DialogE2E`、`C7DictTagE2E`
- `C7ButtonE2E`、`C7CardE2E`、`C7WatermarkE2E` 等

用于组件回归与文档示例对照。

---

## API 模块索引（`src/api`）

| 文件 | 用途 |
|------|------|
| `login.js` | 登录、验证码、getInfo、logout |
| `menu.js` | getRouters |
| `oauth/authorize.js` | 登录页 IdP 列表 |
| `system/*.js` | 系统管理各域 |
| `monitor/*.js` | 监控与任务 |
| `tool/gen.js` | 代码生成 |

---

## 全局能力（非 views）

| 能力 | 位置 |
|------|------|
| Client HMAC | `utils/clientSign.js` + `request.js` |
| 字典 | `utils/dict.js` → `useDict` |
| 权限指令 | `directive/permission/hasPermi.js` |
| 页签 | `plugins/tab.js` |
| 弹窗封装 | `plugins/modal.js` |

---

## 相关文档

- [前端概述](../index)
- [OAuth2 集成](../../backend/modules/oauth2)
- [后端功能模块](../../backend/modules/index)
