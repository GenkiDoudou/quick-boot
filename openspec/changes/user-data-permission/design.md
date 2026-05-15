## Context

- 仓库为 Spring Boot 3 + MyBatis-Plus，用户实体 `sys_user`，角色 `sys_role.data_scope`，自定义部门 `sys_role_dept`，部门 `sys_dept`；认证为 Sa-Token。
- 当前 MP 仅注册 `PaginationInnerInterceptor`；`SysUserServiceImpl` 通过 `LambdaQueryWrapper` + `userMapper.select*` 访问用户，列表可按请求 `deptId` 树筛选，**无**角色数据范围过滤。
- 已定稿产品/技术决策见 `docs/superpowers/specs/2026-05-14-user-data-permission-design.md` 与 `proposal.md`。

## Goals / Non-Goals

**Goals:**

- 使用 **`DataPermissionInterceptor` + `DataPermissionHandler`**（或 MP 推荐的 `MultiDataPermissionHandler`）对 **`sys_user`** 的读 SQL 注入与角色 `data_scope` 一致的条件。
- 多角色 **OR 并集**；任一角色为「全部」则本请求不追加数据权限条件。
- 用户模块内 **所有读 `sys_user` 的 Mapper 路径**均纳入（分页、详情、导出、`authRoleInfo`、以及更新/删除/状态/密码等前置 `selectById`/`selectOne`）。
- **`dept_id` 写入必填**；登录用户无 `dept_id` 时策略写死为：**拒绝依赖该上下文的用户管理写操作并返回业务错误**；读列表在无法计算部门类范围时返回 **空集**（`1=0`）或与详情「不存在」一致（实现统一文档化）。
- 列表/导出请求中的 **`deptId` 筛选与数据权限 AND**，不得放宽基线。
- **超级管理员不代码绕过**；需全部数据则配置 `data_scope=1` 角色。

**Non-Goals:**

- 不为部门管理、岗位等其它表批量接入数据权限（可后续复用 Handler）。
- 不修改若依前端工程；不强制改 OpenAPI 路径。
- 不在本变更中引入全新权限模型（仍用现有角色与 `data_scope` 字段）。

## Decisions

| 决策 | 选择 | 理由 |
|------|------|------|
| 拦截器顺序 | `DataPermissionInterceptor` **先于** `PaginationInnerInterceptor` | 先过滤再分页，避免页内漏拦与计数失真。 |
| 条件挂载点 | `@DataPermission` 落在 **`SysUserMapper` 显式方法**（必要时 XML），Service 改为调用这些方法替代裸 `BaseMapper` 默认读 | 默认 `selectById` 等无法稳定挂注解；与 MP 官方用法一致。 |
| 表引用形式 | SQL 片段使用 **`sys_user.user_id` / `sys_user.dept_id`**（与 MP 生成的主表形式对齐；若生成带别名则在实现阶段统一探测后写死别名策略） | 避免列名歧义。 |
| 部门树 | 与 `SysUserServiceImpl` 中 **`collectDeptSubtreeIds`** 同语义；抽取为 **共享组件**（如 `DeptTreeSupport`）供 Service 与 Handler 复用 | 单一真相，减少 Handler 与列表 `deptId` AND 逻辑分叉。 |
| 登录上下文 | 从 Sa-Token 会话读取 `userId`；`deptId` 从会话扩展字段或既有 `LoginUser` 载体读取（实现时对齐现有登录落库字段） | 与现网一致，避免重复查库；可做请求级缓存。 |
| 无 `dept_id` 登录用户 | **写**：`WarningException` 拒绝；**读列表**：`1=0`；**读单条**：返回 null/不存在 | 与设计推荐一致，避免静默越权。 |

**备选（未采纳）：**

- 仅在 Service 手写 `wrapper`：易漏接口，维护成本高。
- 超级管理员硬编码跳过拦截：与已确认需求冲突。

## Risks / Trade-offs

- **[Risk] 每条用户读 SQL 额外查角色/部门** → **Mitigation**：请求级缓存数据权限片段；部门全量列表可缓存短时。
- **[Risk] Mapper 改造遗漏某条 `selectById` 路径** → **Mitigation**：代码评审清单对照 `proposal` 读路径；静态检索 `userMapper.select`。
- **[Risk] H2 与 MySQL SQL 片段差异** → **Mitigation**：片段仅用标准布尔与 `IN`/`=`；集成测试双跑或 CI 以 H2 为主。
- **[Risk] 历史用户 `dept_id` 为空** → **Mitigation**：Flyway 或运维脚本补齐；登录时提示不可操作。

## Migration Plan

1. 部署前：审计 `sys_user.dept_id` 空值比例，批量赋值或禁用账号。
2. 部署：发版；观察用户列表访问量与慢查询。
3. 回滚：关闭 `DataPermissionInterceptor` Bean 注册（特性开关，可选）或回退版本；数据无破坏性变更。

## Open Questions

- （无）若产品改为「无部门登录用户可读本人」，需再改 spec 中单条读取策略。
