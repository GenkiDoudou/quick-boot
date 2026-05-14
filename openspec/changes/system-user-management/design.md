## Context

定稿产品说明见 `docs/superpowers/specs/2026-05-14-user-management-design.md`。仓库已有 `sys_user`、`SysUserMapper`、`SysUser` 实体，角色域已实现 `sys_user_role` 的「按角色授权用户」写入；部门域提供树与 `treeselect`。用户列表前端雏形存在，但 API 路径与后端契约未统一。

## Goals / Non-Goals

**Goals:**

- 落地设计文档 §5 全部接口与 §6 分层、校验、内置用户规则。
- `sys_user_role` 在用户侧「保存分配角色」与角色侧批量授权之间**单一写入策略**（私有辅助或极薄共享层），避免两套业务规则漂移。
- 部门树筛选：选中节点含**子孙部门**的 `dept_id` 集合过滤（基于内存部门全量表计算子树，与 `DeptServiceImpl` 树思路一致）。
- 导入结果 JSON + 可选 `errorKey` 下载失败 xlsx；导出与列表筛选一致。

**Non-Goals:**

- 岗位、个人中心 profile 重构、行级数据权限 SQL 切面（见定稿设计 §3.2）。

## Decisions

| 决策 | 说明 | 备选 |
|------|------|------|
| 包与控制器 | 新建 `io.github.genkidoudou.web.system.user`，`SysUserController` 映射 `/system/user`，写操作用 `POST`、读用 `GET`，与 `SysRoleController` 子路径风格一致。 | 挂在现有零散 Controller；否决，不利维护。 |
| 分配角色 | 用户侧 `GET/POST .../authRole`；保存时对该 `user_id` **先删后插** `sys_user_role`。 | 仅复用角色侧接口；否决，与 `system:user:edit` 权限及产品路径不一致。 |
| 内置用户 | `user_id == 1`：`remove` 若列表含 `1` 则**整单拒绝**并提示；`update`/`changeStatus` 拦截改 `user_name`、停用。 | 跳过 `1` 部分删除；定稿推荐整单拒绝，实现按设计文档。 |
| 角色校验 | 新增/编辑 `roleIds` **非空**；每个 id 在 `sys_role` 存在且有效。 | 允许零角色；与已定稿冲突，否决。 |
| 密码 | 与登录鉴权相同编码（实现阶段对齐 `Auth` / 安全组件现有 `PasswordEncoder` 或等价）。 | 新引入第三方算法；非必要。 |
| 导入失败文件 | 短时 `errorKey`（内存或 Spring Cache，TTL 建议 5～15 分钟）+ `GET /system/user/importError?errorKey=`。 | 仅 JSON 内嵌错误；不满足验收「下载失败明细」。 |

## Risks / Trade-offs

| 风险 | 缓解 |
|------|------|
| `errorKey` 仅内存时多实例部署不一致 | 首期单实例可接受；若上集群改为 Redis 或粘性会话，在 tasks 中标注可选后续项。 |
| `UserService` 与 `RoleService` 循环依赖 | 用户域依赖部门与 Role **只读**；角色写用户关联保持 **Role → UserMapper** 或抽取无环的 `UserRoleRelationWriter` 置于 `user` 或 `common` 子包并由两侧调用。 |
| Flyway 菜单 id 冲突 | 实现前查询现有 `sys_menu` 最大 id，选用未占用区间；迁移脚本内注释说明。 |

## Migration Plan

1. 先合并并执行 Flyway（菜单 + `sys_role_menu` 绑定），再部署后端，再部署前端（或同时发布）。
2. 回滚：回退部署版本；若需数据回滚，仅当迁移含破坏性 DDL 时准备 down 脚本；本变更以 INSERT 菜单为主，一般可保留菜单行或提供手动 DELETE 说明。
3. 调用方：全局搜索 `/sys/user` 与旧路径，替换为 `/system/user/*`。

## Open Questions

- （无）实现细节以定稿设计文档为准；若实现中发现 `importError` 存储策略与运维环境冲突，在 PR 中补充说明并可选升级为 Redis。
