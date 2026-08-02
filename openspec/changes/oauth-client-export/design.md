## Context

客户端管理页已用 `C7JsonTable` 完成 CRUD。工作区曾删除 `C7ExcelDownload`，`C7JsonTable` 当前无内置导出；后端无 export 接口、无 EasyExcel 依赖。产品设计已定稿于 `docs/superpowers/specs/2026-08-02-oauth-client-export-design.md`。

约束：密钥不落导出文件；同步下载；不恢复导入/异步导出中心。

## Goals / Non-Goals

**Goals:**

- 管理端可同步下载 OAuth 客户端配置 xlsx
- 勾选优先，否则按搜索条件全量（上限 5000）
- 恢复通用组件路径：`C7ExcelDownload` + `C7JsonTable.exportFunction`
- 权限码 `system:oauthClient:export` 前后端一致

**Non-Goals:**

- `C7ExcelUpload`、`excelExport.js`、导入导出中心
- 含 `clientSecret` 的导出
- 客户端导入 / 异步任务导出

## Decisions

### 1. 同步 Blob + EasyExcel 3.3.4

- **选择**：Controller 写 xlsx 流；依赖对齐 bak 的 EasyExcel 3.3.4。
- **备选**：CSV（更轻）/ 异步任务（需导出中心）。
- **理由**：已确认要 xlsx 同步体验；规模小（≤5000）不需要异步。

### 2. 导出请求体：JSON + 可选 ids

- **选择**：`POST /sys/oauthclient/export`，body 含搜索字段与可选 `ids`；`ids` 非空则忽略搜索条件。
- **备选**：仅 query；或勾选与条件拆成两个接口。
- **理由**：与列表搜索字段对齐；单接口覆盖勾选优先规则。

### 3. 前端下载走 JSON blob，不用默认 `downloadRequest`

- **选择**：`exportOauthClient` 用 `responseType: 'blob'` 的 JSON POST（可扩展 `returnBlobWithHeaders`），避免 `downloadRequest` 的 `x-www-form-urlencoded`。
- **理由**：后端接口约定为 JSON。

### 4. `C7JsonTable` 只恢复同步 export，不恢复 import/exportBizType

- **选择**：props 限于 `exportFunction` / `showExportButton` / `exportButtonPermi` / `exportDefaultFileName` / `exportLoadingOptions`；快照附带 `ids`。
- **备选**：整包回滚历史 Excel 能力。
- **理由**：YAGNI；勾选优先需相对历史仅传 `searchParam` 的增强。

### 5. 0 行仍出表头 xlsx

- **选择**：空结果生成仅表头文件，不报错。
- **理由**：避免用户误以为导出失败。

## Risks / Trade-offs

- [误把 secret 写入 Excel] → 导出 DTO/列显式排除 secret；验收检查列集
- [全量导出过大拖垮服务] → 硬上限 5000，超限返回 JSON 错误
- [工作区大量 `D` 文件干扰恢复] → 仅 `git checkout`/`restore` `C7ExcelDownload`，不整包还原 Upload
- [无权限角色看不到按钮但仍可猜 URL] → 后端 `@SaCheckPermission` 强制校验

## Migration Plan

1. 合入依赖与 RBAC SQL（新环境初始化带上；已有库需执行插入 2007 权限及角色关联）
2. 前端恢复组件后重新登录刷新权限缓存
3. 回滚：去掉 export 接口与按钮即可；EasyExcel 依赖可暂留无害

## Open Questions

- 无（设计文档已定稿）
