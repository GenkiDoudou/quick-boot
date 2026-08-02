## Why

客户端管理页已有 CRUD，但缺少批量导出能力，运维/对接方无法快速拿到客户端配置清单。现有 `C7ExcelDownload` 与 `C7JsonTable` 导出路径在工作区被移除，需按已定稿设计恢复同步导出，并保证密钥不落盘。

## What Changes

- 恢复 `C7ExcelDownload`，并在 `C7JsonTable` 内置同步 `exportFunction`（快照含可选 `ids`）
- 新增后端 `POST /sys/oauthclient/export`：同步返回 `.xlsx`（EasyExcel），不含 `clientSecret`
- 导出范围：有勾选仅导出勾选行；无勾选按当前搜索条件全量（上限 5000）
- 增加权限码 `system:oauthClient:export` 及 RBAC 菜单按钮
- 客户端管理页与 API 接线导出按钮

## Capabilities

### New Capabilities

- `oauth-client-export`: OAuth 客户端同步 Excel 导出（接口、权限、列集、勾选优先规则）
- `c7-json-table-export`: `C7JsonTable` + `C7ExcelDownload` 同步导出约定（快照、权限按钮、Blob 下载）

### Modified Capabilities

- （无）当前 `openspec/specs/` 无既有 oauth-client / C7JsonTable 主规格需改 REQUIREMENTS

## Impact

- 后端：`SysOauthClientController` / Service、EasyExcel 依赖、`data-sys-rbac.sql`
- 前端：`packages/C7ExcelDownload`、`C7JsonTable`、`api/system/oauthClient.js`、`views/system/oauthClient/index.vue`
- 参考设计：`docs/superpowers/specs/2026-08-02-oauth-client-export-design.md`
- 不恢复导入 / 异步导出中心
