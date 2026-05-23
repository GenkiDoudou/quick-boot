# 路由配置

## 常量路由

定义于 `src/router/index.js`，无需权限即可访问：

| path | 说明 |
|------|------|
| `/login` | 登录 |
| `/index` | 首页（Layout 内） |
| `/user/profile` | 个人中心 |
| `/redirect/:path` | 刷新重定向 |
| `/401`、`/*` | 错误页 |

**注意**：C7 演示菜单由 Flyway 写入 `sys_menu`，登录后动态下发，**不要**在常量路由重复注册 `dev/*`。

## 动态路由

流程（`permission.js` + `store/modules/permission.js`）：

1. 存在 `Admin-Token` 且访问非白名单路径  
2. 若 `roles` 为空 → `userStore.getInfo()`  
3. `permissionStore.generateRoutes()` → 请求 `GET /getRouters`  
4. 将后端菜单转为 `RouteRecordRaw`，`router.addRoute`  
5. 组件路径：`import.meta.glob('../../views/**/*.vue')` 按 `component` 字段懒加载  

后端菜单字段映射：

| 菜单字段 | 路由 |
|----------|------|
| `path` | `route.path` |
| `component` | `views` 下相对路径 |
| `meta.title` / `icon` | 侧栏与 TagsView |
| `perms` | 按钮权限（非路由） |

## 白名单

`settings.js` → `permissionWhiteList`：默认 `/login`、`/register`。

OAuth 授权页 `/oauth/authorize` 若需未登录访问，须在后端菜单或网关单独配置。

## 相关

- [状态管理](./store)
- [权限管理](../backend/modules/permission-management)
