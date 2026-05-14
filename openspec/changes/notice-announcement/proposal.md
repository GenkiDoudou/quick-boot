## Why

系统管理域缺少「通知公告」的统一维护入口，公告无法按标题、创建人、类型检索与分页管理，也无法安全保存富文本内容。需在 `quickboot` + `quick-ui` 内补齐与现有系统参数、部门等模块一致的分层与接口风格，满足运维与运营公告发布需求。

## What Changes

- 新增通知公告管理能力：分页列表、条件查询、详情回显、新增、编辑、单条与批量物理删除。
- 公告正文以 HTML 存储；持久化前服务端白名单消毒，并按 UTF-8 字符数 ≤ 65535 校验。
- 新增 `sys_notice` 表及 Flyway 迁移；同批或衔接迁移写入 `sys_notice_type`、`sys_notice_status` 字典类型与种子数据。
- 列表与写操作 HTTP 风格对齐仓库约定：查询用 `GET`，新增/修改/删除用 `POST` 子路径（**与原始需求文档中的 PUT/DELETE 表述不一致，以本变更为准**）。
- 列表返回 `R<PageInfo<SysNoticeVo>>`，与 `C7JsonTable` 的 `data.records` / `data.total` 契约一致。
- 前端新增路由、菜单、API、`C7JsonTable` 列表页、弹窗表单、富文本编辑与权限点 `system:notice:*`。
- 明确不包含：门户展示、站内信、已读、推送、回收站、OSS 独立存正文。

## Capabilities

### New Capabilities

- `system-notice-management`: 提供通知公告的查询、分页列表、详情、创建、更新、批量删除及富文本安全存储与字典展示能力。

### Modified Capabilities

- （无）

## Impact

- 后端：`quickboot-web` 下新增 `system.notice` 包（Controller/Service/Mapper/Domain/DTO）、Flyway 脚本、可选依赖 OWASP Java HTML Sanitizer（或与项目既有 HTML 消毒方案对齐）。
- 前端：`quick-ui` 路由、菜单、`src/api/system/notice.js`、系统管理公告页面与字典、`DESIGN.md` 约束下的表单与富文本。
- 数据库：`sys_notice` 表及字典种子数据；需注意 `dict_id`/`dict_code` 与现有数据不冲突（设计文档建议独立数值段并 SQL 注释）。
- 权限与安全：新增四类权限标识；富文本与 XSS 防火墙能力边界需在实现中对照 `openspec/specs/firewall-xss` 等既有规格，避免重复或冲突。
