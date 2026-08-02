# OAuth 客户端管理：同步 Excel 导出

日期：2026-08-02  
状态：已定稿（待实现）

## 背景与目标

在「客户端管理」页增加**同步导出**能力：点击工具栏「导出」后立即下载 `.xlsx`。复用/恢复 `quick-ui` packages 中的 `C7ExcelDownload`，并在 `C7JsonTable` 内置 `exportFunction` 路径，便于其它列表页复用。

### 已确认决策

| 项 | 选择 |
| --- | --- |
| 导出模式 | 同步下载（立刻拿到文件） |
| 密钥 | **不包含** `clientSecret` |
| 范围 | 有勾选 → 仅勾选行；无勾选 → 按当前搜索条件全量 |
| 前端挂载 | 恢复 `C7ExcelDownload` + `C7JsonTable` 内置 `export-function` |
| 格式 | `.xlsx`（EasyExcel） |

### 非目标

- 不恢复 `C7ExcelUpload`、`excelExport.js`、导入导出中心 / 异步任务
- 不导出 `clientSecret`，不做「含密钥导出」双入口
- 不在本期做客户端导入

## 架构

```text
[oauthClient/index.vue]
  C7JsonTable(:export-function, :export-button-permi)
       │ 点击「导出」
       ▼
[C7JsonTable] 组装快照 → exportFunction(snapshot)
       │
       ▼
[C7ExcelDownload] downloadFn → Blob → 浏览器下载
       │
       ▼
[API] POST /sys/oauthclient/export  (responseType: blob)
       │
       ▼
[Service + EasyExcel] ids 优先 / 否则按查询条件 → xlsx（无 secret）
```

## 后端接口

### `POST /sys/oauthclient/export`

- 权限：`system:oauthClient:export`
- Content-Type：`application/json`
- 请求体（与列表搜索字段对齐，另加可选 `ids`）：

```json
{
  "clientId": "",
  "clientName": "",
  "ids": [1, 2]
}
```

### 服务端规则

1. `ids` 非空 → **只按主键 ids 导出**，忽略搜索条件。
2. `ids` 空或缺省 → 按 `clientId` / `clientName` 模糊条件导出（与 page 查询一致）。
3. 行数上限 **5000**；超限返回业务错误 JSON（非 xlsx），前端按 Blob 错误解析提示。
4. 成功响应：`application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`，`Content-Disposition` 建议文件名 `oauth-client.xlsx`。
5. **0 行**：仍生成仅表头的 xlsx（不视为失败）。
6. Excel 列（**无 secret**）：`id`、`clientId`、`clientName`、`apiPathPatterns`、`tokenTimeout`、`checkCaptcha`、`remark`、`createTime`（实体有则导出）。

### 依赖

- 引入 EasyExcel **3.3.4**（与 bak 对齐），落在 common 或 system 模块即可；仅服务本导出，不恢复旧导出中心。

### RBAC

- `data-sys-rbac.sql` 增加按钮权限（建议 id `2007`，父菜单 `2001`）：`system:oauthClient:export`（「客户端导出」）。
- 挂到超级管理员角色，模式与现有 `2002`–`2006` 一致。

## 前端约定

### 恢复 `C7ExcelDownload`

- 从 git 恢复 `quick-ui/src/packages/C7ExcelDownload/index.vue`。
- 在 `packages/index.js` 注册并导出（与其它 C7 组件一致）。
- 职责：执行 `downloadFn` → 解析 `Blob` 或 `{ data, headers }` → 触发下载；管理 loading；JSON 错误 Blob 提示。

### `C7JsonTable` 内置导出（仅同步）

新增 props（对齐历史 E2E，**不**恢复 import / `exportBizType`）：

| Prop | 说明 |
| --- | --- |
| `exportFunction` | `(snapshot) => Promise<Blob \| { data, headers }>` |
| `showExportButton` | 显式开关；默认有 `exportFunction` 即显示 |
| `exportButtonPermi` | 权限码数组 |
| `exportButtonText` | 默认「导出」 |
| `exportDefaultFileName` | 默认 `export.xlsx` |
| `exportLoadingOptions` | 全屏 loading；`false` 则仅按钮 loading |

工具栏在删除按钮旁渲染 `C7ExcelDownload`。点击时快照：

```js
{
  ...cloneDeep(searchParam),
  ids: selectedRows.length
    ? selectedRows.map((r) => r[rowKey])
    : undefined
}
```

- 有勾选才带 `ids`；无勾选不传（或空数组，与后端「ids 非空才走勾选」一致）。

### 客户端管理页

```vue
<C7JsonTable
  ...
  :export-function="exportOauthClient"
  :export-button-permi="['system:oauthClient:export']"
  export-default-file-name="oauth-client.xlsx"
/>
```

- `api/system/oauthClient.js` 增加 `exportOauthClient(snapshot)`：POST 导出接口，`responseType: 'blob'`，需要文件名时使用 `returnBlobWithHeaders: true`（与 `C7ExcelDownload` 约定一致）。注意：现有 `downloadRequest` 默认 `application/x-www-form-urlencoded`；本接口为 JSON body，应使用 `request`/`service` 的 blob 变体或扩展，避免错误编码。

### E2E

- `C7JsonTableE2E` 的 `:export-function` 应再次可用。
- 可选：断言有勾选时快照含 `ids`。

## 错误处理

| 场景 | 行为 |
| --- | --- |
| 业务失败（无权限、超限等） | 响应为 JSON Blob → `C7ExcelDownload` 解析 `msg` 并 toast |
| 网络/其它失败 | 按钮 `error` + `ElMessage` |
| 无导出权限 | 前端按钮不显示；直调接口由 Sa-Token 拒绝 |

## 验收标准

1. 无勾选 + 搜索条件 → 文件含匹配行，无 secret 列/内容。
2. 勾选若干行 → 仅这些行，忽略搜索框。
3. 无 `system:oauthClient:export` → 按钮不显示；接口拒绝。
4. `C7JsonTableE2E` 导出 mock 可用。
5. 后端可编译；前端可下载到 `.xlsx`。

## 实现顺序（概要）

1. 恢复并注册 `C7ExcelDownload`。
2. `C7JsonTable` 接回同步 `exportFunction`（含 `ids` 快照）。
3. 引入 EasyExcel；实现 export API + Service；RBAC SQL。
4. 前端 API + `oauthClient/index.vue` 接线。
5. 按验收标准手工验证；必要时补 E2E。
