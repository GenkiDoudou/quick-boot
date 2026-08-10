## Context

权威产品设计见 `docs/superpowers/specs/2026-08-03-sys-dept-dict-user-design.md`。本文件为 OpenSpec 实现向设计：如何按 `SysOauthClient` / `SysRole` 契约落地部门、字典、用户管理。

现状：无 `sys_dept` / 字典表；`SysUserServiceImpl` 仅登录查询；`quick-ui` 有 `useDict` 与 dict API 桩（旧 `/system/dict/**`）；bak 页面依赖异步导入中心。

## Goals / Non-Goals

**Goals:**

1. 部门、字典类型、字典项、用户管理端完整可用（CRUD + 同步导入导出）。
2. 用户：分配角色、重置密码、启停；部门树选。
3. 字典：刷新缓存；`GET .../data/type/{dictType}` 供 `useDict`。
4. API/Service/前端风格对齐 OauthClient；实现顺序 Dept → Dict → User。

**Non-Goals:**

- 个人中心、数据权限、岗位、异步导入中心。
- bak 旧 URL 兼容层。

## Decisions

### 1. 路径统一 `sys/*`

- `sys/dept`、`sys/dict/type`、`sys/dict/data`、`sys/user`。
- 备选：保留 `/system/dict` 兼容桩 → 否决，同改前端桩。

### 2. 部门管理页用树表 `list`，导出扁平

- `GET /sys/dept/list`（可按名称/状态过滤，返回树）。
- `GET /sys/dept/treeselect` 供用户表单。
- 导出/导入走扁平行；导入判重建议 `(parentId, deptName)` 或可选 `deptId`。

### 3. 字典缓存

- 服务端可用 Spring Cache（`dictType` 为 key）；`refresh` / `refresh/{dictType}` 清缓存。
- 前端 `useDict` store 在管理端刷新后由用户重新进入或调 store 清理（类型页「刷新缓存」后提示即可）。

### 4. 用户密码与超管保护

- 新增默认密码 `admin123`（BCrypt/现有编码器）。
- 列表/详情/导出清除 `password` 字段。
- `userId=1` 或 `userName=admin`：不可删、不可停用（与种子对齐）；重置密码允许管理员操作。

### 5. user_id 类型

- 实现用户管理时将 `sys_user.user_id` 与实体统一为 BIGINT（Flyway alter 若当前为 VARCHAR）；`sys_user_role.user_id` 同步。

### 6. 权限字

按设计文档：`system:dept:*`、`system:dict:*`（类型含 refresh）、`system:dictData:*`、`system:user:*`（含 resetPwd）。

## Risks / Trade-offs

- [user_id 迁移] → H2/MySQL 兼容 ALTER；本地可重建库。
- [体量大] → tasks 严格分阶段，先通 Dept 再 Dict 再 User。
- [删部门拒绝] → 明确错误码/文案，避免静默失败。

## Migration Plan

1. 合并 Flyway 后重启。
2. 超管重新登录拉菜单权限。
3. 回滚：撤菜单与表迁移（开发库可 drop）。

## Open Questions

- 无。
