## Context

仓库为 Spring Boot 3 多模块后端（`quickboot-web` 等）与 Vue 3 + Vite 前端（`quick-ui`）。系统管理域已有参数、部门等模块，接口写操作普遍采用 `POST` 子路径；列表与 `C7JsonTable` 约定成功响应体为 `R`，业务载荷中分页使用 `PageInfo`（`records`、`total` 等）。

定稿业务设计见 `docs/superpowers/specs/2026-05-14-notice-announcement-design.md`；原始需求见 `原始需求/系统管理/通知公告-需求文档.md`。本 `design.md` 将上述内容收敛为可实施技术决策，供 `tasks.md` 拆解。

## Goals / Non-Goals

**Goals:**

- 交付通知公告完整维护闭环：分页列表、筛选、详情、新增、编辑、批量物理删除。
- 正文 HTML 在 Service 层白名单消毒后落库，并按 UTF-8 字符数 ≤ 65535 强校验。
- Flyway 创建 `sys_notice` 并种子化 `sys_notice_type`、`sys_notice_status`，保证字典开箱可用。
- 前后端权限点与 `system:notice:list|add|edit|remove` 对齐（若全局菜单脚本使用 `query` 替代 `list`，实现阶段勘误并同步文档）。

**Non-Goals:**

- 门户/首页公告、站内信、已读未读、推送、评论、版本历史、回收站、逻辑删除。
- 正文独立对象存储（OSS 等）。

## Decisions

### 决策1：接口形状对齐 `SysConfigController`

- **选择**：`GET /system/notice/list`、`GET /system/notice/{noticeId}`；`POST /system/notice/create`、`POST /system/notice/update`、`POST /system/notice/remove`（body 为 ID 列表）。
- **原因**：与 `AGENTS.md` 及现有 `SysConfigController` 一致，降低风格分裂。
- **备选**：沿用需求文档 `PUT`/`DELETE`（与仓库约束冲突，否决）。

### 决策2：列表返回 `R<PageInfo<SysNoticeVo>>`

- **选择**：后端列表接口直接返回 `PageInfo`，使前端 `listFunction` 可直传 axios 解析结果，`rows-key="data.records"`、`total-key="data.total"`。
- **原因**：避免像参数页早期那样在 `listFunction` 内客户端伪造 `total`；公告条数与正文体积更适合真分页。
- **备选**：全量查询 + 前端切片（实现快，扩展性差，否决）。

### 决策3：HTML 消毒组件

- **选择**：优先引入 **OWASP Java HTML Sanitizer**（或若 `quickboot` 已存在等价工具则复用），在 **Service 持久化前**统一调用；禁止用 `IllegalArgumentException` 表达业务失败，使用项目既有业务异常 + 错误码。
- **原因**：满足 XSS 风险控制与规格可测性。
- **备选**：仅前端过滤（不安全，否决）。

### 决策4：字典主键与种子数据

- **选择**：Flyway 使用**固定** `dict_id`/`dict_code`，选取与现有迁移及常见手工数据不冲突的数值段（设计文档建议自 `800001` 起），SQL 注释标明用途。
- **原因**：当前 `V3__sys_dict.sql` 仅建表无种子，需保证全新库可启动即有条目。
- **备选**：依赖人工在字典管理录入（验收不稳定，否决作唯一手段）。

### 决策5：删除策略

- **选择**：**物理删除** `sys_notice` 行。
- **原因**：与 RuoYi 常见模型及当前需求范围一致；无回收站诉求。
- **备选**：`del_flag` 软删（审计更强，超出当前范围）。

## Risks / Trade-offs

- **[风险]** 消毒策略过严导致合法富文本样式丢失 → **[缓解]** 选用成熟默认策略并在联调阶段用典型公告样例回归；文档记录允许标签集调整入口（常量/配置类集中）。
- **[风险]** 字典种子主键与生产已有数据冲突 → **[缓解]** 迁移脚本注释说明区间；上线前在目标库核对 `sys_dict_*` 最大主键，必要时在实现 MR 中调整数值段。
- **[风险]** `LONGTEXT` 与「字符数」计量误解（字节 vs 字符）→ **[缓解]** 规格与代码统一为 UTF-8 **字符数** `String.length()` 语义，并在错误提示中写清。
- **[风险]** 富文本编辑器与 `DESIGN.md` 动效/间距不一致 → **[缓解]** 实现前必读 `DESIGN.md`，表单布局复用系统管理现有弹窗模式。

## Migration Plan

1. 合并 Flyway：先执行 `sys_notice` 表与字典种子（可在同一版本或连续版本，遵守当前最大 `V*` 序号）。
2. 部署后端：无特性开关时，确保新接口与 Spring Security 权限配置一并上线。
3. 部署前端：路由与菜单指向新页面；字典键名与后端一致。
4. **回滚**：回退应用版本；若已执行迁移，准备对应 `DROP TABLE`/删除种子字典的逆向脚本（仅在紧急回滚时使用，生产慎用）。

## Open Questions

- 富文本编辑器具体选型（若项目已有全局封装则直接复用）：在实现 PR 中最终确定并写入 `package.json` 依赖说明。
- 是否在首版将通知维护操作接入系统操作日志（proposal 未强制）：若接入，在 `tasks.md` 增补子任务并与现有日志切面模式对齐。
