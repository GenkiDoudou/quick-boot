## ADDED Requirements

### Requirement: 角色表与关联表可持久化

系统 MUST 提供 `sys_role`、`sys_menu`、`sys_role_menu`、`sys_user_role` 表定义（扩展 `schema-sys.sql`）。`sys_user_role.user_id` MUST 为 `VARCHAR(64)`。是否类字段 MUST 使用 `CHAR(1)` 的 `0`/`1`，禁止 boolean 列。MUST 提供超级管理员角色与系统管理下「客户端管理」「角色管理」及必要按钮权限的种子数据，并将 admin 角色绑定到可识别的管理员用户与本期种子菜单。

#### Scenario: 应用 schema 后表与种子存在

- **WHEN** 应用更新后的 `schema-sys.sql`
- **THEN** 上述四表存在，且存在 `role_key=admin` 的角色及角色管理相关菜单种子

### Requirement: 角色分页与 CRUD

系统 MUST 提供角色分页、详情、新增、修改、删除与改状态接口，路径风格为 `/sys/role/*`，修改类操作优先 `POST`。`role_key` MUST 唯一；删除 `role_id=1` MUST 失败；仍存在用户绑定时删除 MUST 失败。

#### Scenario: 创建并分页查询角色

- **WHEN** 管理员调用 `POST /sys/role/add` 创建合法角色后调用 `POST /sys/role/page`
- **THEN** 列表中包含该角色关键字段

#### Scenario: 禁止删除超级管理员

- **WHEN** 管理员请求删除 `role_id=1`
- **THEN** 操作失败并返回业务错误，角色仍存在

#### Scenario: 有用户绑定时禁止删除

- **WHEN** 角色仍存在 `sys_user_role` 记录且管理员请求删除该角色
- **THEN** 操作失败并返回业务错误

### Requirement: 角色菜单全量授权

系统 MUST 提供 `POST /sys/role/menu` 以 `roleId` + `menuIds` 全量替换 `sys_role_menu`。系统 MUST 提供 `GET /sys/role/menuTree` 返回菜单树与当前角色已选 menuId 列表。本期 MUST NOT 提供完整菜单管理 CRUD 页面接口（读树即可）。

#### Scenario: 保存角色菜单后树回显已选

- **WHEN** 管理员为角色保存一组 menuIds 后再次请求 menuTree
- **THEN** 返回的已选 keys 与保存集合一致（在菜单仍存在的前提下）

### Requirement: 角色用户授权

系统 MUST 在角色维度提供已分配/未分配用户分页，以及授权、取消、批量取消接口。用户主键类型 MUST 与 `sys_user.user_id` 一致。

#### Scenario: 授权用户后出现在已分配列表

- **WHEN** 管理员将用户授予某角色后查询已分配用户分页
- **THEN** 该用户出现在结果中

### Requirement: 角色管理前端页面

前端 MUST 提供 `views/system/role/index.vue`，支持列表 CRUD/改状态、分配菜单、分配用户；API 封装 MUST 调用 `/sys/role/*` 新契约。页面风格 MUST 对齐现网 `oauthClient` 列表页模式（C7JsonTable 等）。

#### Scenario: 从动态路由进入角色页

- **WHEN** 拥有角色管理菜单权限的用户登录且 routers 含角色管理
- **THEN** 可打开角色管理页并加载列表
