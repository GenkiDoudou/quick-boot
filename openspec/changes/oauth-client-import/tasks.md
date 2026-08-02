## 1. Frontend packages

- [x] 1.1 从 git 恢复并裁剪 `C7ExcelUpload`（去掉 import/task、excelImport、异步分支；保留 updateSupport / 模板 / 同步结果 / 错误 base64 下载）
- [x] 1.2 在 `packages/index.js` 导出并注册 `C7ExcelUpload`
- [x] 1.3 为 `C7JsonTable` 增加导入按钮、对话框与 props（`importFunction` / 模板 / 权限等），成功后刷新列表

## 2. Backend import API

- [x] 2.1 实现导入模板下载 `GET /sys/oauthclient/import/template`（无 secret/id/createTime 列）
- [x] 2.2 实现同步导入 `POST /sys/oauthclient/import`：multipart、`updateSupport`、按 clientId 新增/可选更新、上限 5000、失败明细、响应无 secret
- [x] 2.3 Flyway `V4__oauth_client_import_perm.sql`：菜单 2008 + 超管角色关联

## 3. OAuth client page wiring

- [x] 3.1 `api/system/oauthClient.js` 增加模板下载与 FormData 导入 API
- [x] 3.2 `oauthClient/index.vue` 接线导入 props 与权限码

## 4. Verification

- [x] 4.1 后端编译通过
- [ ] 4.2 手工验收：模板、仅新增、勾选更新、无权限隐藏、列表刷新且无 secret 回显
