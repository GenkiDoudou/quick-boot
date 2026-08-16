## Why

quick-h5 业务列表/表单相对 quick-ui 偏精简：搜索多为单关键词、卡片字段少、表单缺主字段与客户端校验。运维在手机端需要更接近 PC 的常用查询与录入体验，同时保持 H5 卡片交互。

## What Changes

- 共享：`validate` 工具、`usePagedList` 支持 filters、列表页关键词 + 状态类筛选
- 系统域页（用户/角色/部门/参数/字典/客户端/文件分类/文件）：增强搜索、用 `QbJsonCardFields` 补列表字段、表单补主字段与校验
- 监控域页（任务/调度日志/登录日志/操作日志/在线/慢 SQL）：增强搜索与列表字段；详情保持；任务仍无 Cron 编辑
- **非 BREAKING**：不做导入导出、不搬 C7 全量、不新增 H5 BFF

## Capabilities

### New Capabilities

- `quick-h5-crud-align-shared`: H5 CRUD 共享搜索筛选与表单校验约定
- `quick-h5-crud-align-system`: 系统域列表/表单实用对齐 quick-ui
- `quick-h5-crud-align-monitor`: 监控域列表/筛选实用对齐 quick-ui

### Modified Capabilities

- （无）

## Impact

- 前端：`quick-h5/src/utils/validate.ts`、`composables/usePagedList.ts`、`components/qb/*`（按需）、`pages/system/**`、`pages/monitor/**`
- 依赖：`docs/superpowers/specs/2026-08-16-quick-h5-crud-align-ui-design.md`；复用 `QbJsonCardFields`
- 后端：原则上无新接口；筛选以现网 API 为准
