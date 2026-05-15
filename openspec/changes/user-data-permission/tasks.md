## 1. 基础设施与拦截器

- [x] 1.1 在 `MybatisPlusPaginationConfig` 中注册 `SysUserDataPermissionInnerInterceptor`（等价原 `DataPermissionInterceptor`），并保证其位于 `PaginationInnerInterceptor` 之前
- [x] 1.2 实现 `UserDataScopeSnapshotLoader`（JDBC 读角色、`sys_role_dept`、部门树）+ 请求 Filter 写入 `UserDataScopeSnapshot`；拦截器消费快照生成 `sys_user` SQL 片段（等价 DataPermissionHandler）
- [x] 1.3 抽取或复用部门子树计算逻辑（与现有列表 `deptId` 筛选一致），供 Handler 计算 `data_scope=4` 使用

## 2. Mapper 与 Service 改造

- [x] 2.1 通过 `SysUserDataPermissionInnerInterceptor` 对 `SysUserMapper` 的受控 `selectPage`/`selectList`/`selectById`/`_mpCount` 注入片段（MP OSS 无 `@DataPermission` / `DataPermissionInterceptor`）
- [x] 2.2 审计 `SysUserServiceImpl` 中所有 `userMapper.select*` 调用，改为走带数据权限的 Mapper 方法，确保无遗漏（含 `get`、`page`、`export`、`authRoleInfo`、`update`/`remove`/`changeStatus`/`resetPwd`/`saveAuthRole` 等路径）
- [x] 2.3 确认生成 SQL 中主表名/别名与数据权限片段列引用一致（`sys_user.user_id` / `sys_user.dept_id`）

## 3. 业务校验与数据

- [x] 3.1 在 `create`/`update`/`importData`（及模板行解析）中强制 `dept_id` 非空，违反则 `WarningException`
- [x] 3.2 登录用户无 `dept_id`：写操作统一拒绝；读列表空集；读详情越权为不存在——与 `design.md` 对齐并实现
- [x] 3.3 评估 Flyway 或数据脚本：补齐种子/历史用户 `dept_id`（若存在空值）

## 4. 验证

- [x] 4.1 为 Handler 或片段生成编写单元测试（多角色、`data_scope` 组合、`sys_role_dept`）
- [ ] 4.2 补充或执行集成测试：不同 `data_scope` 账号请求 `/system/user/list`、`/{id}`、`/export`，断言无越权；`deptId` 与数据权限 AND 行为回归
- [ ] 4.3 本地 `mvn -pl quickboot-web -am test`（或项目约定命令）通过后再标记变更可归档
