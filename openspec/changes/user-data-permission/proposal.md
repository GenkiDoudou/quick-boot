## Why

用户列表与相关读接口目前仅支持按请求参数筛选，**未**按登录用户所绑定角色的 `data_scope`（全部 / 自定义 / 本部门 / 本部门及以下 / 仅本人）收紧数据范围，存在越权查看他人用户数据的风险。需在用户管理模块内与若依语义对齐，并以 MyBatis-Plus `DataPermissionInterceptor` 统一落地。

## What Changes

- 注册 **`DataPermissionInterceptor`**（置于分页拦截器之前），实现 **`DataPermissionHandler`**：根据当前登录用户、角色 `data_scope`、`sys_role_dept`、部门树生成对 `sys_user` 的 SQL 过滤片段。
- **`SysUserMapper`** 对「读 `sys_user`」路径统一挂载 **`@DataPermission`**（或等价 XML 占位），覆盖分页、详情、导出、分配角色页读用户、以及更新/删除/状态/密码等路径中的 **`selectById` / `selectOne` / `selectList` / `selectPage` / `selectCount`**，避免绕过。
- **多角色**：取最宽并集（任一角色为「全部」则不追加限制；否则各角色可见范围 **OR**）。
- **超级管理员**：**不**代码绕过数据权限；需「全部数据」时通过 `data_scope=1` 角色配置实现。
- **`dept_id` 必填**：创建 / 修改 / 导入等写入路径强制校验；登录用户无部门时的错误策略在 `design.md` 与实现中写死为一种。
- 列表 / 导出中的 **`deptId`（含子孙）** 与数据权限条件为 **AND** 关系（前端筛选不得放宽基线）。
- 参考：`docs/superpowers/specs/2026-05-14-user-data-permission-design.md`；若依 RuoYi-Vue3 数据范围语义作对照（本仓库未必含其源码目录）。

## Capabilities

### New Capabilities

- `system-user-data-permission`：系统用户管理在读库维度上受角色数据范围约束；与 `DataPermissionInterceptor`、登录上下文、`sys_role` / `sys_role_dept` / `sys_dept` 协同的行为需求。

### Modified Capabilities

- （无）当前 `openspec/specs/` 下无「用户管理」既有能力文档；本次以新增 delta spec 为主。

## Impact

- **后端**：`MybatisPlusPaginationConfig`（或等价 MP 配置类）、新增 Handler 与可能的部门树工具复用、`SysUserMapper` / `SysUserServiceImpl`、登录会话中需可读取 `userId` 与 `deptId`。
- **API 行为**：部分账号可见用户集合缩小；详情 / 导出 / 分配角色页在越权目标上返回「不存在」或与列表一致的空/错策略。
- **数据**：建议校验并补齐历史用户 `dept_id`；种子数据若缺部门需调整。
- **前端**：无强制接口契约变更；列表 `deptId` 筛选语义为在数据权限之上的 AND（与设计一致）。
