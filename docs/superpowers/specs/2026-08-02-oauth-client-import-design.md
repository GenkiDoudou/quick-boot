# OAuth 客户端管理：同步 Excel 导入

日期：2026-08-02  
状态：已定稿（待实现）  
关联：`docs/superpowers/specs/2026-08-02-oauth-client-export-design.md`

## 背景与目标

在「客户端管理」页增加**同步导入**能力：工具栏「导入」打开对话框，上传 xlsx 后立即返回成功/失败统计。复用/精简恢复 `C7ExcelUpload`，并在 `C7JsonTable` 内置导入路径，与已落地的同步导出对称。

### 已确认决策

| 项 | 选择 |
| --- | --- |
| 导入模式 | 同步（立刻返回结果） |
| 重复 clientId | 可选更新（勾选「是否更新已经存在的数据」） |
| 密钥 | 模板**不含** secret；新增自动生成；更新保留原密钥 |
| 前端挂载 | 精简 `C7ExcelUpload` + `C7JsonTable` 内置导入 |
| 方案 | 精简组件 + multipart API（不做导入中心） |

### 非目标

- 异步任务 / 导入导出中心 / `excelImport.js` 平台 bizType
- 导入密钥列、更新时重置密钥、响应回传新建 secret
- 整包回滚历史 Upload 异步分支

## 架构

```text
[oauthClient/index.vue]
  C7JsonTable(:import-function, :import-template-fn, :import-button-permi)
       │ 点击「导入」
       ▼
[C7JsonTable] 对话框 → 精简 C7ExcelUpload
       │ 模板下载 / 上传 + updateSupport
       ▼
[API]
  GET  /sys/oauthclient/import/template → xlsx 模板（无 secret）
  POST /sys/oauthclient/import          → multipart(file, updateSupport)
       │
       ▼
[Service + EasyExcel]
  按 clientId 新增(生成 secret) / 可选更新(保留 secret)
  → JSON：统计 + 可选失败明细
```

## 后端接口

### `GET /sys/oauthclient/import/template`

- 权限：`system:oauthClient:import`
- 返回仅表头（或可选示例行）的 `.xlsx`

### `POST /sys/oauthclient/import`

- 权限：`system:oauthClient:import`
- `multipart/form-data`：`file`（必填）、`updateSupport`（`true`/`false` 或 `1`/`0`）
- 行数上限 **5000**；超限整单失败（业务错误 JSON）

### 模板/解析列（无 secret、无 id、无 createTime）

| 列 | 必填 | 说明 |
| --- | --- | --- |
| clientId | 是 | 业务唯一键；判重依据 |
| clientName | 是 | |
| apiPathPatterns | 是 | 逗号分隔 Ant 路径 |
| tokenTimeout | 否 | 秒；空=全局 |
| checkCaptcha | 否 | `0`/`1`；空默认 `0` |
| status | 是 | `0` 启用 / `1` 停用 |
| remark | 否 | |

### 服务端规则

1. 按 `clientId` 查重。
2. 不存在 → 新增，**自动生成 secret**（与单条 add 一致）。
3. 已存在且 `updateSupport=false` → 该行失败「客户端已存在」。
4. 已存在且 `updateSupport=true` → 更新可写字段，**保留原 secret / 主键**。
5. 行级校验失败记入失败明细，其它行继续（部分成功）。
6. **响应不得包含任何 clientSecret**。

### 同步响应 JSON

```json
{
  "code": 200,
  "data": {
    "mode": "sync",
    "total": 10,
    "successCount": 8,
    "failCount": 2,
    "errorFileBase64": "...",
    "errorFileName": "oauth-client-import-errors.xlsx"
  }
}
```

- `failCount === 0` 时可省略错误文件字段。

### RBAC

- 权限码：`system:oauthClient:import`
- Flyway：`V4__oauth_client_import_perm.sql`，菜单 id **2008**（父 `2001`），挂超级管理员角色（模式对齐 V3 导出权限）。

## 前端约定

### 精简 `C7ExcelUpload`

- 从 git 恢复后裁剪：去掉 `@/api/import/task`、`excelImport.js`、异步 `mode` / `async-submitted`。
- 保留：拖拽选文件、「是否更新已经存在的数据」、模板下载（`C7ExcelDownload`）、确定/取消、同步结果统计、失败明细本地下载（`errorFileBase64` → Blob）。
- `uploadFn(file, strategy)`：`strategy` 为 `overwrite` | `ignore`（由勾选映射）。
- 注册到 `packages/index.js`。

### `C7JsonTable` 内置导入

新增 props（不做 `importBizType`）：

| Prop | 说明 |
| --- | --- |
| `importFunction` | `(file, strategy) => Promise<result>` |
| `importTemplateDownloadFn` | 模板 Blob 下载 |
| `showImportButton` | 显式开关 |
| `importButtonPermi` | 权限码数组 |
| `importButtonText` | 默认「导入」 |
| `importMaxSizeMb` | 默认如 10 |

- 工具栏「导入」打开对话框，内嵌 `C7ExcelUpload`。
- 成功后 `refreshData`；`emit('import-success')`。
- 工具栏顺序建议：新增 / 修改 / 删除 / 导入 / 导出。

### 客户端管理页

```vue
:import-function="importOauthClient"
:import-template-download-fn="downloadOauthClientImportTemplate"
:import-button-permi="['system:oauthClient:import']"
:show-import-button="true"
```

- API：`FormData` POST 导入；模板用 blob 下载（可 `returnBlobWithHeaders`）。

## 错误处理

| 场景 | 行为 |
| --- | --- |
| 类型/大小不合规 | 前端先拦，后端再校验 |
| 超限 / 无权限 / 空文件 | 业务错误 JSON + toast |
| 行级失败 | 部分成功 + 失败明细 xlsx |
| 未勾选更新且已存在 | 行失败「客户端已存在」 |
| 导入成功 | 刷新列表；不弹密钥 |

## 验收标准

1. 模板列正确且无 secret。
2. 仅新增：重复 clientId 失败可下明细；新行成功且库中有 secret。
3. 勾选更新：重复行字段更新、secret 不变。
4. 无 `system:oauthClient:import` → 按钮隐藏；接口拒绝。
5. 导入后列表刷新；后端可编译。

## 实现顺序（概要）

1. 精简恢复并注册 `C7ExcelUpload`。
2. `C7JsonTable` 内置导入按钮与对话框。
3. 后端 template + import API、RBAC V4。
4. 前端 API + `oauthClient/index.vue` 接线。
5. 按验收标准手工验证。
