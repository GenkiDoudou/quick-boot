## Context

客户端管理已有 CRUD 与同步导出；`C7ExcelUpload` / `excelImport.js` 在工作区删除，历史实现依赖导入导出中心。产品设计已定稿于 `docs/superpowers/specs/2026-08-02-oauth-client-import-design.md`。

约束：同步导入；密钥不经 Excel；可选更新；不恢复异步中心。

## Goals / Non-Goals

**Goals:**

- 管理端可下载无 secret 的导入模板并同步上传 xlsx
- 按 `clientId` 新增（自动生成 secret）或可选更新（保留 secret）
- 精简 `C7ExcelUpload` + `C7JsonTable` 内置导入，与导出对称
- 权限 `system:oauthClient:import` 前后端一致

**Non-Goals:**

- 异步任务 / 导入导出中心 / `excelImport.js` / `importBizType`
- 导入密钥列、更新重置密钥、响应回传 secret

## Decisions

### 1. 同步 multipart + 失败明细 base64

- **选择**：`POST` multipart 立即返回 JSON 统计；失败行打包为错误 xlsx 的 base64。
- **备选**：异步中心；仅返回失败行文本。
- **理由**：对齐导出的同步体验；无需中心模块。

### 2. 精简恢复 C7ExcelUpload，去掉异步分支

- **选择**：保留拖拽、updateSupport、模板下载、同步结果与本地下载错误文件。
- **备选**：整包回滚历史 Upload。
- **理由**：历史依赖 `import/task`，当前仓库不可用。

### 3. 判重键用 clientId，不用主键 id

- **选择**：模板无 id；业务键 `clientId`。
- **理由**：与登录 Basic 业务标识一致；导出/迁移友好。

### 4. 部分成功

- **选择**：行级失败不回滚已成功行；提供错误明细。
- **备选**：整单事务失败。
- **理由**：大批量导入更实用；与常见后台导入一致。

### 5. RBAC 菜单 2008 / V4

- **选择**：新 Flyway，避免改已执行的 V1/V3。
- **理由**：与导出 2007 并列，互不覆盖。

## Risks / Trade-offs

- [误导入/覆盖生产客户端] → 默认不更新；需显式勾选 updateSupport
- [secret 泄漏到 Excel 或响应] → 模板列与响应 DTO 显式排除；验收检查
- [大文件拖垮服务] → 前端大小限制 + 后端 5000 行上限
- [裁剪 Upload 破坏其它页] → 本期仅 oauth 页接线；不恢复 bizType 平台路径

## Migration Plan

1. 部署后端 + 跑 V4；重新登录刷新权限
2. 前端发布含精简 Upload 与表格导入
3. 回滚：下线 import 接口与按钮；V4 权限可留存无害

## Open Questions

- 无（设计文档已定稿）
