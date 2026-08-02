## 1. Frontend packages

- [x] 1.1 从 git 恢复 `quick-ui/src/packages/C7ExcelDownload/index.vue`（不恢复 Upload）
- [x] 1.2 在 `packages/index.js` 导出并 `installPackages` 注册 `C7ExcelDownload`
- [x] 1.3 为 `C7JsonTable` 增加同步导出 props 与工具栏 `C7ExcelDownload`（快照含可选 `ids`）
- [x] 1.4 确认 `C7JsonTableE2E` 的 `:export-function` 可再次工作

## 2. Backend export API

- [x] 2.1 引入 EasyExcel 3.3.4（父 POM / common 或 system，对齐 bak）
- [x] 2.2 实现导出查询：`ids` 非空优先，否则按 clientId/clientName；上限 5000；0 行仍出表头
- [x] 2.3 新增 `POST /sys/oauthclient/export`，权限 `system:oauthClient:export`，xlsx 流且不含 secret
- [x] 2.4 Flyway `V3__oauth_client_export_perm.sql` 增加菜单按钮 2007 及超管角色关联（原 `data-sys-rbac.sql` 已迁 Flyway）

## 3. OAuth client page wiring

- [x] 3.1 `api/system/oauthClient.js` 增加 JSON body + `responseType: 'blob'` 的 `exportOauthClient`（支持 headers 文件名）
- [x] 3.2 `oauthClient/index.vue` 接线 `:export-function` / `:export-button-permi` / 默认文件名

## 4. Verification

- [x] 4.1 后端编译通过（避免 IDE 脏 class：必要时 `mvn clean compile`）
- [ ] 4.2 手工验收：搜索全量、勾选优先、无权限隐藏、文件无 secret 列
