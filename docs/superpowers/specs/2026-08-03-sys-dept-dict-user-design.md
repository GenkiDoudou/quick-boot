# 系统管理：部门 / 字典 / 用户（对齐 SysOauthClient）

日期：2026-08-03  
状态：已定稿（待实现）  
来源：`bak/quick-ui` 页面迁移 + 当前 `SysOauthClient` 后端契约

## 背景与目标

将 bak 前端的**字典管理、字典项管理、用户管理**迁入当前工程，并补齐缺失的**精简部门管理**。后端一律按当前仓库已落地的 `SysOauthClient` / `SysRole` 风格实现（显式字段赋值、`page/add/update/remove`、同步 Excel 导入导出），不照搬 bak 异步导入中心。

### 已确认决策

| 项 | 选择 |
| --- | --- |
| 范围 | 部门 + 字典类型 + 字典项 + 用户，一次交付 |
| 部门 | 精简模块（表 + CRUD + 树选）；用户侧用树选 |
| 用户扩展 | 分配角色 + 重置密码；**不含**个人中心 profile |
| 导入导出 | 同步；`updateSupport`；失败明细与 OauthClient 一致 |
| 删部门 | 若仍有用户绑定该部门 → **拒绝删除** |
| 方案组织 | 单 OpenSpec 变更，实现顺序：`Dept → Dict → User` |

### 非目标

- 个人中心（资料 / 改密 / 头像）
- 数据权限、岗位、异步导入导出中心 / `import-biz-type`
- bak 旧 URL（`/system/user/create` 等）原样保留
- 完整组织架构（仅精简部门树）

## 架构

```text
实现顺序
  1) SysDept     表/CRUD/树/导入导出
  2) SysDictType + SysDictData  CRUD/缓存刷新/按类型查询/导入导出
  3) SysUser     CRUD/导入导出/分配角色/重置密码（依赖部门树 + 角色）

前端
  bak 页面结构 → 现 C7JsonTable + 同步 C7ExcelUpload/Download
  已有 useDict / getDicts → 对齐新后端路径

后端模板
  SysOauthClientController / SysOauthClientServiceImpl
```

## 后端契约（统一）

各资源前缀：

| 模块 | 前缀 |
| --- | --- |
| 部门 | `sys/dept` |
| 字典类型 | `sys/dict/type` |
| 字典项 | `sys/dict/data` |
| 用户 | `sys/user` |

通用写读（对齐 OauthClient/Role）：

| 动作 | 方法 | 路径 |
| --- | --- | --- |
| 分页 | POST | `{prefix}/page` |
| 详情 | GET | `{prefix}/{id}` |
| 新增 | POST | `{prefix}/add` |
| 修改 | POST | `{prefix}/update` |
| 单删 | GET | `{prefix}/remove/{id}` |
| 批删 | POST | `{prefix}/remove` body=`List` |
| 导出 | POST | `{prefix}/export` |
| 导入模板 | GET | `{prefix}/import/template` |
| 导入 | POST | `{prefix}/import` multipart `file` + `updateSupport` |

### 模块特有 API

**部门**

- `GET sys/dept/treeselect`：下拉树（启用部门）
- 列表也可 `GET sys/dept/list` 返回树（管理页树表，可选；若用 JsonTable 分页则用 page 扁平 + 前端树，**推荐管理页树表 `list`，导出用扁平**）

**字典类型**

- `POST sys/dict/type/refresh`：刷新全部缓存
- `POST sys/dict/type/refresh/{dictType}`：刷新单类型
- 判重键：`dictType`

**字典项**

- `GET sys/dict/data/type/{dictType}`：按类型返回启用项（供 `useDict`）
- 判重键：同类型下 `dictValue`（或 `dictType`+`dictValue`）
- 删除类型前若存在字典项 → 拒绝（或级联删；**采用拒绝**）

**用户**

- `POST sys/user/changeStatus`：启停
- `POST sys/user/resetPwd`：重置密码（管理员）
- `GET sys/user/authRole/{userId}`：已选/可选角色
- `POST sys/user/authRole`：保存用户角色全量
- 判重键：`userName`
- 响应与导出**不得**含密码哈希明文展示需求外的可逆密码；列表/详情/导出均不返回 `password`
- 新增：密码默认 `admin123`（或配置项），存储用现有 PasswordEncoder
- 更新：请求体密码空则保留原哈希
- 禁止删除/停用超级管理员约定用户（`userId=1` 或 `userName=admin`，与现种子一致）

### Service 写法

- `toEntity` 后**显式** `set` 关键字段与默认值（对齐 `SysRoleServiceImpl#add` / `SysOauthClientServiceImpl#add`）
- 导入：`ExcelUtils.importExcel` + `ExcelDataCheckException` 行失败 + `writeErrorFile()`
- 导出：ids 优先，否则搜索条件；上限 5000

## 数据模型（Flyway）

### `sys_dept`

- `dept_id` BIGINT PK、`parent_id`、`dept_name`、`order_num`、`leader`、`phone`、`email`、`status`、审计字段 / `del_flag` / `remark`
- 种子：可选一个「总公司」根节点

### `sys_dict_type`

- `dict_id`、`dict_name`、`dict_type`(UK)、`status`、审计字段

### `sys_dict_data`

- `dict_code` PK、`dict_sort`、`dict_label`、`dict_value`、`dict_type`、`css_class`、`list_class`、`is_default`、`status`、审计字段
- 种子：至少 `sys_normal_disable`（正常/停用），供列表状态字典

### 菜单权限

在「系统管理」下增加：

- 部门管理及按钮：`list/query/add/edit/remove/export/import`
- 字典管理（类型）及按钮，含 `refresh`
- 字典项可从类型页进入（菜单可只挂类型，数据页路由参数 `dictType`）；或挂隐藏路由
- 用户管理及按钮：含 `resetPwd`、`export`、`import`（分配角色可用 `edit` 或独立 `authRole`）

权限字示例：`system:dept:*`、`system:dict:*`、`system:dictData:*` 或沿用若依习惯 `system:dict:type` / `system:dict:data`——**实现时与前端 `v-hasPermi` 统一为**：

- `system:dept:list|query|add|edit|remove|export|import`
- `system:dict:list|query|add|edit|remove|export|import|refresh`（类型）
- `system:dictData:list|query|add|edit|remove|export|import`（字典项）
- `system:user:list|query|add|edit|remove|export|import|resetPwd`

（若与 bak 权限字冲突，以本表为准并改前端。）

## 前端

| 页面 | 来源 | 改造点 |
| --- | --- | --- |
| `views/system/dept/index.vue` | 新建（精简树表或 JsonTable） | Oauth 风格 API |
| `views/system/dict/type/index.vue` | bak | 去 bizType；同步导入导出；刷新缓存 |
| `views/system/dict/data/index.vue` | bak | 同上；路由带 `dictType` |
| `views/system/user/index.vue` + `add-or-update` + `auth-role` | bak | 去异步导入；部门树；分配角色；重置密码；无 profile |
| `api/system/*.js` | 重写 | 对齐 `sys/*` 与 OauthClient 的 page/blob/FormData |

已有 `utils/dict.js` 的 `getDicts` 改为调用 `GET /sys/dict/data/type/{dictType}`。

## 实现顺序与验收

1. Flyway 表 + 菜单权限 + 种子字典/根部门  
2. Dept 后端 + 前端  
3. DictType/DictData 后端 + 前端 + `useDict` 冒烟  
4. User 后端 + 前端（含 authRole、resetPwd、导入导出）  
5. `mvn compile`；手工：各部门/字典/用户 CRUD、导入失败明细、删有用户部门被拒、字典缓存刷新后标签更新

## 风险

- [user_id 类型] 当前 DDL 为 `VARCHAR`，实体为 `Long` → 实现用户模块时统一为 BIGINT/雪花，必要时 Flyway 修正（与 Role 用户授权已用的 id 形态对齐）
- [权限字与 bak 不一致] → 前后端同 PR 改齐
- [变更体量大] → 严格按 Dept→Dict→User 阶段合并，避免半成品联调

## Open Questions

- 无（已确认范围与行为）
