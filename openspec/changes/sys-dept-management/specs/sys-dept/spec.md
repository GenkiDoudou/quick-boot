## ADDED Requirements

### Requirement: 部门表 `sys_dept` 持久化

系统 MUST 通过 Flyway 迁移提供 `sys_dept` 表，至少包含：`dept_id`（主键）、`parent_id`（顶级为 **-1**）、`dept_name`、`order_num`、`leader`、`phone`、`email`、`status`、`remark`、`del_flag` 及项目约定的审计字段；索引 MUST 支持按 `parent_id` 与 `del_flag` 查询子部门。

#### Scenario: 迁移在空库上可执行

- **WHEN** 在干净数据库上应用本变更所含 Flyway 版本
- **THEN** `sys_dept` 表存在且主键与 `parent_id` 查询不因缺少索引而不可接受地慢（具备 `parent_id`+`del_flag` 组合索引或等价方案）

---

### Requirement: 部门列表接口返回嵌套树

`GET /system/dept/list` MUST 与 `quick-ui/src/api/system/dept.js` 中 `listDept` 的路径一致；成功时 HTTP 层与项目统一约定一致，且 JSON 体中 **`data` 为根节点数组**，每个节点 MUST 含业务字段及 **`children`**；无子节点时 **`children` MUST 为空数组**。

#### Scenario: 无筛选返回全树

- **WHEN** 调用 `GET /system/dept/list` 且不带 `deptName`、`leader`、`status` 筛选参数（或等价「无筛选」约定）
- **THEN** `data` 包含所有未逻辑删除且可从 `parent_id = -1` 到达的节点，结构为嵌套树

#### Scenario: 有筛选时剪枝且保留祖先路径

- **WHEN** 调用 `GET /system/dept/list` 且带有任一非空筛选参数（`deptName` 和/或 `leader` 和/或 `status`）
- **THEN** `data` 为森林，仅包含「自身或子孙满足筛选条件」的节点，且对每个命中节点保留从根（`parent_id = -1`）到该节点的祖先链；无匹配时 `data` MUST 为空数组

---

### Requirement: 部门下拉树 `treeselect`

`GET /system/dept/treeselect` MUST 与 `dept.js` 中 `listTreeDept` 路径一致；成功时 `data` 为树，节点 MUST 至少包含 **`id`**（等于 `dept_id`）、**`label`**（等于 `dept_name`）、**`children`**（规则同列表嵌套树，空子为数组）。

#### Scenario: 下拉树可绑定树选择器

- **WHEN** 调用 `GET /system/dept/treeselect`
- **THEN** 返回的树可被前端用于选择「上级部门」且节点具备 `id`/`label`/`children`

---

### Requirement: 部门详情

`GET /system/dept/{deptId}` MUST 返回单条部门信息于 `data`，且 MUST 包含编辑回显所需字段（至少含 `deptId` 与 `parentId`）；**不**要求必须包含嵌套 `children`。

#### Scenario: 读取已存在部门

- **WHEN** 请求存在的 `deptId` 且该记录未逻辑删除
- **THEN** 响应成功且 `data.deptId` 等于路径参数

#### Scenario: 读取不存在或已删除部门

- **WHEN** 请求不存在或已逻辑删除的 `deptId`
- **THEN** 响应为业务失败（`code` 非成功约定值）且带有可读 `msg`

---

### Requirement: 新增与修改部门校验

`POST /system/dept` 与 `PUT /system/dept` MUST 校验：`parent_id` 指向的父部门存在且未删除；**禁止**将节点父级设为自身；**禁止**改父级后形成环（父级不得落在当前节点及其子孙 id 集合内）；`parent_id = -1` MUST 表示顶级；`status` 取值 MUST 与字典 **`sys_normal_disable`** 一致（`0`/`1`）；邮箱与手机格式 MUST 按项目统一校验策略校验（若项目暂无则允许宽松校验但 MUST 在代码注释中说明）。

#### Scenario: 新增顶级部门成功

- **WHEN** `POST` 合法载荷且 `parentId = -1`
- **THEN** 响应成功且数据库中新增一行，`parent_id` 为 -1

#### Scenario: 修改父级成环被拒绝

- **WHEN** `PUT` 将某节点的 `parentId` 设为其子孙节点 id
- **THEN** 响应失败且数据库中该节点 `parent_id` 未被更新为非法值

---

### Requirement: 删除部门仅受子部门约束

`DELETE /system/dept/{deptId}` MUST 在校验登录/权限（与项目一致）后，若存在未逻辑删除的子部门则 MUST 拒绝删除并返回业务失败；若无子部门则 MUST 执行逻辑删除（或项目约定的删除语义）。本迭代 MUST NOT 依赖 `sys_user` 或任何用户表计数作为删除条件。

#### Scenario: 有子部门时删除失败

- **WHEN** 目标部门存在至少一个未逻辑删除的子部门
- **THEN** `DELETE` 返回业务失败且子部门数据仍存在

#### Scenario: 叶子部门删除成功

- **WHEN** 目标部门无未逻辑删除的子部门
- **THEN** `DELETE` 返回成功且该部门对后续列表/详情查询不再可见（逻辑删除语义）

---

### Requirement: 前端部门管理页

系统 MUST 提供部门管理前端路由页：包含对 `deptName`、`leader`、`status` 的筛选；使用 `el-table` 树形展示，数据源为 `listDept` 返回的 **`data` 根数组**；提供新增、修改、删除、查看；编辑时上级部门选择 MUST 使用 `listTreeDept` 数据；删除前 MUST 二次确认并展示接口错误 `msg`。权限指令与路由 meta MUST 与 tasks 中列出的权限标识一致。

#### Scenario: 筛选后表格展示剪枝树

- **WHEN** 用户输入筛选条件并触发查询
- **THEN** 表格绑定数据与后端剪枝语义一致且无控制台因数据结构非树而报错
