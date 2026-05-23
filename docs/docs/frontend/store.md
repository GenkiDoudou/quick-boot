# 状态管理（Pinia）

入口：`src/store/index.js`，模块在 `src/store/modules/`。

## user

| 状态/方法 | 说明 |
|-----------|------|
| `token` | 与 Cookie `Admin-Token` 同步 |
| `name`、`avatar`、`roles`、`permissions` | `getInfo()` 填充 |
| `login`、`logOut` | 调用 `api/login.js` |

## permission

| 方法 | 说明 |
|------|------|
| `generateRoutes` | 拉取 `/getRouters` 并 `addRoute` |
| `routes`、`sidebarRouters` | 侧栏与顶栏菜单数据 |

## settings

主题色、侧栏折叠、是否显示 TagsView、固定 Header 等，持久化到 localStorage。

## dict

字典缓存（若启用），配合 `useDict(['sys_user_sex', ...])` 批量加载。

## tagsView

多页签打开列表、`affix` 固定首页等，与 `$tab` 插件联动。

## 使用示例

```js
import useUserStore from '@/store/modules/user'

const userStore = useUserStore()
await userStore.getInfo()
```

## 相关

- [路由配置](./router)
- [工具函数](./utils)
