## 1. 基础设施与拦截器

- [ ] 1.1 在 `MybatisPlusPaginationConfig`（或新建专用 `@Configuration`）中为 `MybatisPlusInterceptor` 注册 `DataPermissionInterceptor`，并保证其位于 `PaginationInnerInterceptor` 之前
- [ ] 1.2 实现 `DataPermissionHandler`（或 `MultiDataPermissionHandler`）：读取登录 `userId`/`dept_id`，加载用户角色、`data_scope`、`sys_role_dept`，按并集规则生成 `sys_user` 的 SQL 片段；可选：请求级缓存片段
- [ ] 1.3 抽取或复用部门子树计算逻辑（与现有列表 `deptId` 筛选一致），供 Handler 计算 `data_scope=4` 使用

## 2. Mapper 与 Service 改造

- [ ] 2.1 在 `SysUserMapper` 增加带 `@DataPermission` 的显式读方法（含分页、按 id、按条件列表、计数等），必要时增加 `SysUserMapper.xml`
- [ ] 2.2 审计 `SysUserServiceImpl` 中所有 `userMapper.select*` 调用，改为走带数据权限的 Mapper 方法，确保无遗漏（含 `get`、`page`、`export`、`authRoleInfo`、`update`/`remove`/`changeStatus`/`resetPwd`/`saveAuthRole` 等路径）
- [ ] 2.3 确认生成 SQL 中主表名/别名与数据权限片段列引用一致（`sys_user.user_id` / `sys_user.dept_id`）

## 3. 业务校验与数据

- [ ] 3.1 在 `create`/`update`/`importData`（及模板行解析）中强制 `dept_id` 非空，违反则 `WarningException`
- [ ] 3.2 登录用户无 `dept_id`：写操作统一拒绝；读列表空集；读详情越权为不存在——与 `design.md` 对齐并实现
- [ ] 3.3 评估 Flyway 或数据脚本：补齐种子/历史用户 `dept_id`（若存在空值）

## 4. 验证

- [ ] 4.1 为 Handler 或片段生成编写单元测试（多角色、`data_scope` 组合、`sys_role_dept`）
- [ ] 4.2 补充或执行集成测试：不同 `data_scope` 账号请求 `/system/user/list`、`/{id}`、`/export`，断言无越权；`deptId` 与数据权限 AND 行为回归
- [ ] 4.3 本地 `mvn -pl quickboot-web -am test`（或项目约定命令）通过后再标记变更可归档
