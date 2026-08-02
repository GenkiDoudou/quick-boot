## Why

客户端管理已支持同步导出，但仍缺少批量导入，环境迁移与批量开户只能逐条手工录入。需在不做导入导出中心的前提下，提供与导出对称的同步 xlsx 导入，并保证密钥不经 Excel 流通。

## What Changes

- 精简恢复 `C7ExcelUpload`（去掉异步中心依赖），并在 `C7JsonTable` 内置导入按钮与对话框
- 新增 `GET /sys/oauthclient/import/template` 与 `POST /sys/oauthclient/import`（multipart，可选更新）
- 按 `clientId` 新增（自动生成 secret）或可选更新（保留 secret）；行级失败返回错误明细文件
- 增加权限码 `system:oauthClient:import` 及 Flyway 菜单 2008
- 客户端管理页接线导入

## Capabilities

### New Capabilities

- `oauth-client-import`: OAuth 客户端同步 Excel 导入（模板、multipart、判重更新、密钥策略、权限）
- `c7-json-table-import`: `C7JsonTable` + 精简 `C7ExcelUpload` 同步导入约定

### Modified Capabilities

- （无）主规格目录无既有 oauth-client / C7JsonTable 导入 REQUIREMENTS 需改；导出相关 change 规格不在本期修改

## Impact

- 后端：`SysOauthClientController` / Service、EasyExcel 读写、Flyway V4
- 前端：`packages/C7ExcelUpload`、`C7JsonTable`、`api/system/oauthClient.js`、`views/system/oauthClient/index.vue`
- 参考：`docs/superpowers/specs/2026-08-02-oauth-client-import-design.md`
- 不恢复 `excelImport.js` / 导入导出中心
