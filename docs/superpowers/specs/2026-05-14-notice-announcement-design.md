# 通知公告设计文档

## 1. 背景与目标

在系统管理域内提供「通知公告」维护能力：支持按条件查询、分页列表、详情回显、新增、编辑、单条与批量删除；表单支持富文本内容；类型与状态依赖数据字典正确回显。

原始需求文档见 `原始需求/系统管理/通知公告-需求文档.md`。本文档为经 `/brainstorming` 澄清后的**定稿设计**，与需求不一致处以本文为准（尤其 HTTP 方法与列表分页契约）。

## 2. 需求澄清结论（已确认）

| 序号 | 结论 |
|------|------|
| 1 | 列表「创建人」与查询「操作人员」统一为**创建人**，查询条件字段为 `createBy`。 |
| 2 | 列表与写操作接口风格对齐仓库约定：读用 `GET`，**新增/修改/删除统一 `POST` 子路径**（不使用 `PUT`/`DELETE`）。 |
| 3 | 公告内容存 **HTML 字符串**；持久化前在服务端做 **白名单 HTML 消毒**；消毒后按 **UTF-8 字符数 ≤ 65535** 校验（与 `LONGTEXT` 常见用法一致；若驱动或库有更严限制则在实现中下调并注明）。 |
| 4 | 字典类型固定为 **`sys_notice_type`**、**`sys_notice_status`**（与 RuoYi 常用编码一致）。 |
| 5 | 界面「公告编号」即主键 **`noticeId`**，无独立业务流水号。 |

## 3. 范围与非范围

### 3.1 实施范围

- 后端：`sys_notice` 表、Flyway 迁移、领域模型、Mapper、Service、Controller、入参校验与富文本消毒。
- 同批次或衔接迁移中写入 **`sys_notice_type` / `sys_notice_status`** 字典类型及字典项初始数据，保证开箱可验收「字典显示正确」。
- 前端：`quick-ui` 路由、菜单、API 模块、列表页（`C7JsonTable`）、弹窗表单、富文本编辑器、权限指令。
- 权限点与需求一致：`system:notice:list`、`system:notice:add`、`system:notice:edit`、`system:notice:remove`（与后端鉴权、前端 `v-hasPermi` 对齐；若项目统一使用 `query` 替代 `list`，实现时以全局菜单脚本为准并在此文档勘误）。

### 3.2 明确不做

- 门户/首页滚动公告、站内信、已读未读、推送、评论、版本历史、回收站。
- 不做逻辑删除（删除为**物理删除**）。
- 不做独立 OSS 存正文（正文随表字段存储）。

## 4. 数据模型

### 4.1 表 `sys_notice`

| 字段 | 类型（建议） | 说明 |
|------|----------------|------|
| `notice_id` | BIGINT PK | 自增或雪花策略与现有 `sys_*` 表一致 |
| `notice_title` | VARCHAR(50) | 非空；与常见 RuoYi 模型一致，超长由前后端校验拦截 |
| `notice_type` | CHAR(1) | 非空；字典 `sys_notice_type` |
| `notice_content` | LONGTEXT | 可空；存消毒后 HTML |
| `status` | CHAR(1) | 默认 `0`；字典 `sys_notice_status`（如 0 正常 / 1 关闭） |
| `create_by` | VARCHAR(64) | 创建人 |
| `create_time` | DATETIME | 创建时间 |
| `update_by` | VARCHAR(64) | 更新人 |
| `update_time` | DATETIME | 更新时间 |

可选：`remark`（VARCHAR）——若不做可省略以保持最小表。

索引建议：`(create_time)` 降序列表默认排序；按需对 `notice_title` 前缀查询加索引（实现阶段评估数据量）。

### 4.2 字典种子数据

- **`sys_notice_type`**：至少两项，例如 `1` 通知、`2` 公告（`dict_label` 中文展示名与需求一致）。
- **`sys_notice_status`**：至少两项，例如 `0` 正常、`1` 关闭。

`dict_id` / `dict_code` 须在 Flyway 中选用**与既有迁移及运行库数据不冲突**的固定主键；若仓库尚无其他字典种子，本变更采用独立数值段（例如自 `800001` 起递增）并在 SQL 注释中标明，避免与后续手工录入冲突。

## 5. 接口契约

统一响应 `R`；列表分页载荷使用 `quickboot-common` 已有 **`PageInfo<T>`**（含 `records`、`total`、`current`、`size`、`pages`），以便前端 `C7JsonTable` 使用 `rows-key="data.records"`、`total-key="data.total"`（其中 `data` 为 `R` 的业务载荷）。

| 能力 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 分页列表 | GET | `/system/notice/list` | Query：`pageNum`、`pageSize`、`noticeTitle`（模糊）、`noticeType`（精确）、`createBy`（模糊，对应创建人）；返回 `R<PageInfo<SysNoticeVo>>` |
| 详情 | GET | `/system/notice/{noticeId}` | 含 `noticeContent`，供编辑回显 |
| 新增 | POST | `/system/notice/create` | Body：`SysNoticeBo`；校验分组 Add |
| 修改 | POST | `/system/notice/update` | Body：`SysNoticeBo`，含 `noticeId`；分组 Update |
| 删除 | POST | `/system/notice/remove` | Body：`List<Long>`，支持批量 |

**与原始需求文档第 6 节差异**：不以 `PUT`/`DELETE` 暴露删除与修改语义，以符合 `AGENTS.md` 与现有 `SysConfigController` 风格。

## 6. 后端设计

### 6.1 分层与包结构

- 包路径：`io.github.genkidoudou.web.system.notice`（或项目统一 `web.system` 下与 `config`、`dept` 并列的 `notice` 子包）。
- 分层：`controller` / `service` / `service.impl` / `mapper` / `domain` / `dto`（Bo、QueryBo、Vo）。
- 公开类型及 public 成员补充 **JavaDoc**（简体中文为主）。

### 6.2 校验与异常

- 写接口入参 **Jakarta Validation**；标题、类型必填与需求一致。
- 业务失败使用项目自定义异常（如 `WarningException` + 错误码），**禁止**以 `IllegalArgumentException` 表达业务失败。
- 详情/更新时若 `noticeId` 不存在：明确中文提示。
- `remove` 入参为空列表或含非法 ID：校验拒绝。

### 6.3 富文本消毒与长度

- 在 **Service 层**、持久化前执行 HTML 白名单消毒（推荐 **OWASP Java HTML Sanitizer**；若项目已统一使用其他等价组件则与之对齐）。
- 消毒后若内容为空而原始提交非空（例如全部为非法标签），视为业务错误并提示用户。
- 消毒后再校验 UTF-8 字符长度不超过 **65535**（与第 2 节澄清一致）。

### 6.4 审计字段

- `create_by` / `update_by` / 时间与现有模块一致，从**当前登录用户上下文**写入（实现时对照 `dept`、`config` 等已有写法）。

### 6.5 OpenAPI

- Controller 标注 `@Tag`、`@Operation`，路径参数与关键 Query 使用 `@Parameter`。

## 7. 前端设计（quick-ui）

### 7.1 页面与组件

- 列表：优先 **`C7JsonTable`**，`listFunction` 调用 `GET /system/notice/list`，直接兼容 `R.data` 为 `PageInfo` 的 `records`/`total`。
- 查询项：公告标题、公告类型、创建人（界面文案建议使用「创建人」，与澄清结论一致，避免与「操作人员」歧义）。
- 表格列：公告编号（`noticeId`）、公告标题、公告类型、状态、创建人、创建时间；类型/状态使用字典（`useDict('sys_notice_type','sys_notice_status')` 或项目等价方式）。
- 新增/编辑：弹窗表单；必填标题、类型；状态默认与 RuoYi 参考一致（如 `0`）；内容区使用富文本编辑器。
- 删除：行内删除 + 批量删除，调用 `POST /system/notice/remove`；成功后 `refreshData()`。

### 7.2 权限

- 列表查询：`system:notice:list`
- 新增：`system:notice:add`
- 编辑：`system:notice:edit`
- 删除：`system:notice:remove`

### 7.3 视觉与依赖

- 新建/改造页面前遵循仓库根目录 **`DESIGN.md`** 与 `sdd/前端代码规范.md`。
- 富文本组件选型以实现阶段依赖为准，须满足设计系统间距与表单布局。

## 8. 验收映射

| 原始验收项 | 设计对应 |
|------------|----------|
| 新增/编辑后列表及时更新 | 提交成功后关闭弹窗并调用表格 `refreshData()` |
| 类型和状态字典显示正确 | 依赖 Flyway 种子 + 前端 `dict` 拉取与列渲染 |

## 9. 测试建议

- Service 单测或集成测：HTML 含 `script`/事件属性被剔除；超长拒绝；不存在 ID 更新/删除失败提示。
- 接口测：分页边界、`createBy` 与 `noticeTitle` 组合筛选。
- 手动：权限四类账号或 Mock 下按钮显隐。

## 10. 后续流程

实现前阅读：`openspec/project.md`、`sdd/后端代码规范.md`、`sdd/前端代码规范.md`、`sdd/数据库设计规范.md` 与 `DESIGN.md`。

本设计批准后，使用 **`writing-plans`** 产出分步实现计划，再进入编码与联调。
