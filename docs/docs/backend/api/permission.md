# 权限接口

## 菜单 `/system/menu`

| 方法 | 路径 |
|------|------|
| GET | `/list`、`/treeselect`、`/roleMenuTreeselect/{roleId}` |
| GET | `/{menuId}` |
| POST | `/update`、`/remove/{menuId}` |

## 角色 `/system/role`

| 方法 | 路径 |
|------|------|
| GET | `/list`、`/authUser/allocatedList`、`/authUser/unallocatedList` |
| GET | `/{roleId}` |
| POST | `/create`、`/update`、`/remove`、`/changeStatus` |
| POST | `/dataScope`、`/menu` |
| POST | `/authUser/selectAll`、`/cancel`、`/cancelAll` |
| POST | `/export`、`/import`、`/import/template` |

## 路由下发

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/getRouters` | 登录用户可见菜单树 → 前端动态路由 |

详见 [权限管理](../modules/permission-management)。
