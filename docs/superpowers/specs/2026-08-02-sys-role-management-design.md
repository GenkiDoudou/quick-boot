# 角色管理（含 getInfo / getRouters）设计

**日期：** 2026-08-02  
**状态：** 已评审设计（待实现计划）  
**参考：** `bak/quickboot` 角色管理行为；现网 `SysOauthClient` / `SysUser` 分层与 API 风格  
**范围：** 角色 CRUD + 菜单授权 + 用户授权 + 最小 `sys_menu` + `/auth/me` 与 `/getRouters` 按角色动态返回

## 1. 背景与目标

主工程尚无 `SysRole` 业务实现；前端 `getInfo` 将 roles/permissions 硬编码为默认全权限；`/getRouters` 在 `ScaffoldCompatController` 中写死「系统管理 → 客户端管理」。  
目标是参考 bak 的若依式角色能力，在现网包结构下交付可运营的角色管理，并使登录后的权限与动态路由真正依赖角色-菜单数据。

### 成功标准

1. 管理员可对角色做分页 CRUD、改状态；可为角色全量保存菜单；可在角色页内为角色授权/取消用户。
2. 种子菜单同时包含「客户端管理」与「角色管理」；超级管理员角色（`role_key=admin`）绑定后可见两菜单。
3. `/auth/me`（前端 `getInfo`）返回真实 `roles` / `permissions`；`admin` 角色 permissions 为 `*:*:*`。
4. `/getRouters` 按当前用户角色从 `sys_menu` 组装路由树，不再使用硬编码列表。
5. 布尔语义字段使用 `CHAR(1)` 的 `0`/`1`，禁止 DB/实体 boolean；文本 UTF-8 无 BOM。

## 2. 决策摘要

| 项 | 决策 |
|----|------|
| 功能范围 | 角色 CRUD + 角色-菜单 + 角色-用户；最小菜单表与读树接口 |
| getInfo / getRouters | 本期一并改造，去掉硬编码 |
| 实现路径 | 参考 bak 行为，按 OauthClient/User 现网分层重写（非整包拷贝） |
| API 风格 | 以 `POST` 为主；路径 camelCase；前缀 `/sys/role`（与 `/sys/oauthclient` 对齐） |
| DDL | 扩展 `quickboot-web/.../db/schema-sys.sql`（本仓库当前无 Flyway migration 目录） |
| user_id | `sys_user_role.user_id` 为 `VARCHAR(64)`，对齐 `sys_user.user_id` |
| 权限注解 | 与现网 OauthClient 一致：本期可不强制 `@SaCheckPermission`；菜单 `perms` 仍入库供前端指令与后续接线 |
| 非目标 | 完整菜单管理页、dataScope/`sys_role_dept`、导入导出、用户侧 `auth-role.vue` |

## 3. 数据模型

### 3.1 `sys_role`

| 列 | 说明 |
|----|------|
| role_id | BIGINT PK（雪花/ASSIGN_ID，与 bak 一致） |
| role_name | 名称 |
| role_key | 权限字符，唯一约束 |
| role_sort | 排序 |
| data_scope | CHAR(1) 可保留默认 `'1'`，**本期无业务读写强制** |
| status | CHAR(1)：与现网 User/OauthClient 注释对齐（设计实现前核对实体；种子与列表语义写进注释） |
| del_flag | CHAR(1) `0` 未删 / `1` 已删 |
| remark / create_by / create_time / update_by / update_time | 常规审计 |

种子：`role_id=1`，`role_name=超级管理员`，`role_key=admin`。

### 3.2 `sys_menu`（最小集）

| 列 | 说明 |
|----|------|
| menu_id | BIGINT PK |
| parent_id | 父菜单，根为 0 |
| menu_name | 名称 |
| menu_type | `M` 目录 / `C` 菜单 / `F` 按钮 |
| path / component / route_name | 路由相关（F 可空） |
| perms | 权限字符（按钮/菜单） |
| icon / order_num | 展示 |
| status / del_flag | CHAR(1) |
| 审计字段 | 同角色 |

种子至少包含：

- 目录：系统管理（Layout，`/system`）
- 菜单：客户端管理 → `system/oauthClient/index`（保持现网组件路径）
- 菜单：角色管理 → `system/role/index`，`perms=system:role:list`
- 按钮：角色 add/edit/remove 等常用 perms（与前端按钮 `v-hasPermi` 预留一致即可）

### 3.3 关联表

- `sys_role_menu(role_id, menu_id)` 复合主键  
- `sys_user_role(user_id VARCHAR(64), role_id)` 复合主键  

种子：将 `admin` 角色绑定到现有可识别管理员用户（实现时查 `sys_user`；若仅有脚手架用户则绑定该用户）；`role_id=1` 绑定全部本期种子菜单。

## 4. 后端设计

### 4.1 包与分层

`io.github.genkidoudou.system` 下：

- `entity`：`SysRole`、`SysMenu`、`SysRoleMenu`、`SysUserRole`
- `mapper`：对应 BaseMapper
- `service` / `impl`：角色服务、权限查询服务（供 me / routers 复用）
- `controller`：`SysRoleController`；改造 `LoginController` / `ScaffoldCompatController`（或抽到独立 MenuRoute 服务）
- `vo`：分页/详情/授权请求体等；扩展 `AuthMeVo`

对齐参考：`SysOauthClientController`（`page/get/add/update/remove` + `PageRequest`/`PageInfo`/`R`）。

### 4.2 角色 API

| 能力 | 方法与路径 |
|------|------------|
| 分页 | `POST /sys/role/page` |
| 详情 | `GET /sys/role/get?roleId=` |
| 新增 | `POST /sys/role/add` |
| 修改 | `POST /sys/role/update` |
| 删除 | `POST /sys/role/remove`（支持批量 ids） |
| 改状态 | `POST /sys/role/changeStatus` |
| 保存菜单 | `POST /sys/role/menu` body：`roleId` + `menuIds[]`（全量替换） |
| 菜单树+已选 | `GET /sys/role/menuTree?roleId=` |
| 已分配用户分页 | `POST /sys/role/authUser/allocatedPage` |
| 未分配用户分页 | `POST /sys/role/authUser/unallocatedPage` |
| 授权用户 | `POST /sys/role/authUser/grant` |
| 取消用户 | `POST /sys/role/authUser/cancel` / `cancelAll` |

硬规则：

- `role_id=1` 不可删除  
- 仍有 `sys_user_role` 绑定的角色不可删除  
- `role_key` 唯一冲突返回业务友好错误（现网已有 `uk_sys_role_key` 文案可复用）

### 4.3 `/auth/me`（getInfo 数据源）

`GET /auth/me`：

1. `StpUtil.checkLogin()`  
2. 加载用户基本信息  
3. 查启用未删角色的 `role_key` 列表 → `roles`  
4. 查角色关联菜单的非空 `perms` 去重 → `permissions`；若含 `admin` 角色则 `permissions = ["*:*:*"]`  
5. 响应结构需让前端 `getInfo` 能映射为 `{ user, roles, permissions }`（可在后端直接返回该结构，或扩展 `AuthMeVo` 后改前端适配）

注意：`AuthMeVo.userId` 与 `LoginUser` / `sys_user.user_id` 类型不一致时，以实现时统一为 **String（VARCHAR 用户主键）** 为准，避免 Long 强转错误。

### 4.4 `/getRouters`

替换 `ScaffoldCompatController` 硬编码：

1. 取当前用户角色  
2. 查询这些角色可见的 `menu_type in ('M','C')` 且启用未删菜单  
3. 组装若依形态树：`name/path/hidden/redirect/component/alwaysShow/meta/children`  
4. 与现 `quick-ui/src/store/modules/permission.js` 消费格式兼容  

`admin`：可返回全部启用菜单（或与 `*:*:*` 一致的全量菜单策略，实现时二选一并在注释写明；推荐 **admin 全量启用菜单**）。

### 4.5 缓存（最小）

本期可不做分布式缓存；角色菜单变更后，若登录态把 perms 缓存在 Session，则在 `saveRoleMenu` / 用户角色变更时清理对应用户 Session 权限快照。若当前登录仅每次 `/auth/me` 现查库，则可省略。

## 5. 前端设计

1. 新增 `quick-ui/src/views/system/role/index.vue`  
   - 对齐 `oauthClient/index.vue`：C7JsonTable + C7Dialog  
   - 操作：新增/修改/删除/改状态、分配菜单（树勾选）、分配用户（已分配/未分配 + 授权/取消）  
2. 改写 `quick-ui/src/api/system/role.js` 对齐新后端路径；移除或注释本期不用的 dataScope/import/export 调用  
3. 改写 `quick-ui/src/api/login.js` 的 `getInfo()`：使用后端返回的 roles/permissions，去掉写死的 `ROLE_DEFAULT` / `*:*:*`（空 roles 时保留 store 侧兜底策略）  
4. 路由：依赖 `/getRouters` 动态注入；无需再维护硬编码系统菜单（静态路由注释保持即可）

## 6. 验证计划

1. 执行/应用 `schema-sys.sql` 增量后表与种子存在。  
2. 登录管理员：`/auth/me` 含 `admin` 与 `*:*:*`（或等价）；`/getRouters` 含客户端管理 + 角色管理。  
3. 角色页 CRUD、菜单授权、用户授权成功。  
4. 新建非 admin 角色只勾选客户端管理并授权测试用户：该用户 routers 仅客户端、permissions 不含角色按钮。  
5. 删除有用户绑定的角色失败；删除 `role_id=1` 失败。

## 7. 实现顺序（供 writing-plans）

1. DDL + 种子  
2. Entity/Mapper  
3. 权限查询服务（roles/perms/menu tree for user）  
4. 角色 Service/Controller（CRUD + menu + authUser）  
5. 改造 `/auth/me` 与 `/getRouters`  
6. 前端 API + role 页面 + getInfo 适配  
7. 按第 6 节验证  

## 8. 非目标（再次确认）

- 菜单管理完整后台 CRUD 页面  
- 数据权限 dataScope / 部门  
- 角色导入导出  
- 用户管理页内的独立 `auth-role` 路由页（角色页内授权已覆盖本期需求）
