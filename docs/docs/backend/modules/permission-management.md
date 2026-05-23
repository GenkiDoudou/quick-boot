# 权限管理

权限由 **菜单（路由+按钮）**、**角色**、**用户-角色** 与 **数据权限** 共同组成，基于 Sa-Token 注解与前端 `perms` 字符串。

## 菜单管理

| 项 | 值 |
|----|-----|
| Controller | `SysMenuController` |
| 路径 | `/system/menu` |

| 接口 | 说明 |
|------|------|
| GET `/list` | 菜单列表（树形） |
| GET `/treeselect` | 下拉树 |
| GET `/roleMenuTreeselect/{roleId}` | 角色已选菜单树 |
| GET `/{menuId}` | 详情 |
| POST `/update` | 新增/修改（按 Bo 分组） |
| POST `/remove/{menuId}` | 删除 |

菜单字段要点：

- `menuType`：目录 M / 菜单 C / 按钮 F
- `perms`：按钮权限标识，前端 `v-hasPermi` 使用
- `component`：前端组件路径，如 `system/user/index`
- `path`、`query`：路由 path 与参数

登录后 `GET /getRouters` 将菜单转为前端路由（见 [前端路由配置](../../frontend/router)）。

## 角色管理

| 项 | 值 |
|----|-----|
| Controller | `SysRoleController` |
| 路径 | `/system/role` |
| 前端 | `views/system/role/index.vue` |

| 接口 | 说明 |
|------|------|
| GET `/list` | 角色分页 |
| POST `/create`、`/update`、`/remove` | CRUD |
| POST `/changeStatus` | 状态 |
| POST `/dataScope` | **数据权限**范围 |
| POST `/menu` | 保存角色菜单 |
| GET `/authUser/allocatedList`、`unallocatedList` | 角色下已/未分配用户 |
| POST `/authUser/selectAll`、`cancel`、`cancelAll` | 批量授权/取消 |
| POST `/export`、`/import`、`/import/template` | Excel |

### 数据权限类型（dataScope）

| 值 | 含义 |
|----|------|
| 1 | 全部数据 |
| 2 | 自定义部门 |
| 3 | 本部门 |
| 4 | 本部门及以下 |
| 5 | 仅本人 |

## 用户角色

在用户管理中 `authRole` 接口维护；角色侧也可反向分配用户。

## 相关文档

- [用户管理](./user-management)
- [权限接口](../api/permission)
