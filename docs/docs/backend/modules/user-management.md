# 用户管理

## 概述

系统用户 CRUD、状态切换、密码重置、角色授权、Excel 导入导出。

| 项 | 值 |
|----|-----|
| Controller | `SysUserController` |
| 基础路径 | `/system/user` |
| 前端页面 | `quick-ui/src/views/system/user/` |
| API | `quick-ui/src/api/system/user.js` |
| 数据表 | `sys_user`（及用户角色关联表） |

## 主要接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/list` | 分页列表（支持部门、状态等筛选） |
| GET | `/{userId}` | 用户详情 |
| POST | `/create` | 新增 |
| POST | `/update` | 修改 |
| POST | `/remove` | 删除（逻辑删除） |
| POST | `/changeStatus` | 启用/停用 |
| POST | `/resetPwd` | 重置密码 |
| GET | `/authRole/{userId}` | 查询已分配角色 |
| POST | `/authRole` | 保存用户角色 |
| POST | `/importData` | Excel 导入（multipart） |
| GET | `/importTemplate` | 下载导入模板 |
| GET | `/importError` | 下载导入失败明细 |
| POST | `/export` | 导出 Excel |

## 权限标识（示例）

以菜单 SQL 为准，常见形如：

- `system:user:list`、`system:user:add`、`system:user:edit`、`system:user:remove`
- `system:user:export`、`system:user:import`、`system:user:resetPwd`

前端按钮使用 `v-hasPermi="['system:user:add']"`。

## 数据权限

用户列表受**角色数据权限**约束（全部 / 自定义 / 本部门 / 本部门及以下 / 仅本人），由角色模块 `dataScope` 配置，Service 层拼接数据范围条件。

## 开发注意

- 密码入库经 `PasswordCodec`（BCrypt + 可配置 SM4 传输层），禁止明文日志。
- 导入失败应返回可下载的错误行文件（`importError`）。
- 管理员账号受保护，删除/停用需业务校验。

## 相关文档

- [权限管理](./permission-management)
- [用户接口](../api/user)
