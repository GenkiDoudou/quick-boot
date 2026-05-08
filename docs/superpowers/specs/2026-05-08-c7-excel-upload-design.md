# C7ExcelUpload（C7 Excel 导入）设计说明

**日期**：2026-05-08  
**状态**：已定稿（经 brainstorming 澄清 **1A～7A** 与「确认定稿」）  
**依据**：`原始需求/前端/C7Excel导入.md` + Q&A 结论及实现路径 **路径 1**（单文件 `index.vue` + 必要时同文件内纯函数）

---

## 1. 背景与目标

- **背景**：Excel 导入需要统一的文件选择、类型/大小校验、导入策略选择（覆盖/忽略），并在导入完成后展示结果与错误明细下载入口。
- **目标**：提供 **`C7ExcelUpload`**：选择 **`.xls/.xlsx`**、选择 **`overwrite|ignore`**、调用业务 **`uploadFn(file, strategy)`**，展示 **`total/successCount/failCount`**，在 **`failCount>0` 且 `errorFileUrl` 存在** 时展示错误明细链接；支持 **`notify`** 替换默认 **`ElMessage`**；暴露 **`uploading`** 与 **`reset()`**；**`uploading`** 与 **`v-model:uploading`** 对齐 **`C7ExcelDownload`** 的 **`downloading`** 约定。
- **非目标**：不内置 **`ElDialog` / `ElCard`** 外壳（由业务布局）；不在组件内拼装 **`FormData`** 或约定 multipart 字段名（由 **`uploadFn`** 内完成）；错误明细链接**不**强制复用 **`C7ExcelDownload`** 的 Blob 链（普通 **`<a href>`** 打开/下载即可）。

---

## 2. 命名、边界与落点

| 项 | 约定 |
|----|------|
| **对外组件名** | **`C7ExcelUpload`** |
| **目录** | **`quick-ui/src/packages/C7ExcelUpload/index.vue`** |
| **实现形态** | **单文件组件**；扩展名/大小校验等可在同文件底部以 **纯函数** 形式组织。首版不抽 **`useC7ExcelUpload`**。 |
| **全局注册** | 在 **`quick-ui/src/packages/index.js`** 中 **`export`** 与 **`installPackages`** 注册，与其它 C7 一致。 |

**职责**：文件选择与校验、策略展示与双向绑定、触发 **`uploadFn`**、管理 **`uploading`**、展示导入结果与错误文件入口、**`reset`**。

**不负责**：接口 URL、鉴权、**`FormData`** 字段、后端 **`R`** 包装的解析（由 **`uploadFn`** 内整理为下文结果类型）。

---

## 3. `uploadFn` 与结果类型（**2A**）

- **`uploadFn: (file: File, strategy: 'overwrite' \| 'ignore') => Promise<C7ExcelUploadResult>`**  
- **`C7ExcelUploadResult`**（成功 resolve 值，字段与原始需求一致）：

```ts
/** 业务在 uploadFn 内从接口响应整理后 resolve */
interface C7ExcelUploadResult {
  total: number
  successCount: number
  failCount: number
  /** failCount>0 时可选，用于错误明细链接 */
  errorFileUrl?: string
}
```

- **`emit('success', result: C7ExcelUploadResult)`**：在 **`uploadFn` resolve** 且组件完成结果状态更新后触发（与 **`revokeObjectURL`** 无关；本组件无 Blob 链）。

---

## 4. Props、策略、通知

| 项 | 约定 |
|----|------|
| **`accept`** | 默认 **`'.xls,.xlsx'`**（实现可与需求一致；若允许覆盖须保证默认仍满足验收「仅 xls/xlsx」语义）。 |
| **`maxSizeMb`** | **必填**，`number`，允许的最大文件大小（**MB**）；与原始需求 **`fileSize`** 对应。 |
| **`duplicateStrategy`** | **`'overwrite' \| 'ignore'`**，配合 **`update:duplicateStrategy`** 支持 **`v-model:duplicateStrategy`**；**默认 `'ignore'`**（与 **`reset()`** 复位一致）。 |
| **`overwriteLabel` / `ignoreLabel`** | 可选；缺省时中文 **「覆盖」/「忽略」**（i18n 策略以实现/代码评审为准）。 |
| **`notify`** | **`(type: 'success' \| 'error' \| 'warning' \| 'info', message: string) => void`**，可选；未传时错误/校验提示用 **`ElMessage`**（**5A**）。 |
| **`uploadFn`** | 见第 3 节；缺失时实现可 **`console.warn` + no-op**，以代码评审为准。 |

**根节点与透传**：触发导入、选文件等按钮建议使用 **`ElButton`**；常用展示属性通过 **`v-bind="$attrs"`** 透传至主要按钮（与 **`C7ExcelDownload`** 同类约定；**`inheritAttrs: false`** 时在根按钮合并 **`class` / `style`**，避免重复绑定内部事件名——细节写入实现计划）。

---

## 5. 事件、状态与 `reset`（**6A、7A**）

| 项 | 约定 |
|----|------|
| **`error`** | **`emit('error', err: unknown)`**：仅当 **`uploadFn` reject**（或内部将其视为失败的异步错误）时触发。 |
| **校验失败（扩展名/大小）** | **`notify('error', message)`**；**不** **`emit('error')`**，避免与接口失败混用。 |
| **`uploading`** | 内部 **`ref(false)`**；在 **`uploadFn` 开始时** 置 **`true`**，在 **`finally`** 置 **`false`**。 |
| **`v-model:uploading`** | **`uploading` prop + `update:uploading`**；组件内开始导入时 **`emit('update:uploading', true)`** 等与 Vue 3 习惯一致。 |
| **防重复** | **`uploading === true`** 时点击导入 **no-op**（**7A**）。 |
| **`defineExpose`** | **`{ uploading, reset }`**，其中 **`uploading`** 与内部状态一致（或只读 ref，以实现为准）。 |
| **`reset()`** | 清空已选文件展示、**将 file input 的 `value` 置空**（支持重复选择同一文件）、清空上次结果展示、**`duplicateStrategy` 复位为 `'ignore'`**（与默认一致）。 |

---

## 6. 文件选择与校验

- 使用 **隐藏 `input[type="file"`]** + 展示用 **`ElButton`**（或链接按钮）触发 **`click()`** 打开文件选择器。
- **选择后**：校验扩展名（在 **`accept`** 与业务约定下，仅允许 **`.xls` / `.xlsx`**）与 **文件大小 ≤ `maxSizeMb`**（注意 **MB → 字节** 换算边界）。不通过则 **第 5 节** 校验失败路径；**不调用** **`uploadFn`**。
- **每次选择并处理完 change 后**：重置 **input** 的 **value**（原始需求：允许重复选择同一文件）。

---

## 7. 导入结果与错误文件（**3A**）

- 成功后在界面展示 **`total` / `successCount` / `failCount`**。  
- 当 **`failCount > 0`** 且 **`errorFileUrl`** 为**非空字符串**（trim 后）：展示错误明细入口，例如 **`<a :href="errorFileUrl" target="_blank" rel="noopener noreferrer">`**，文案如 **「下载错误明细」**（实现计划可微调）。  
- **不**要求走 **`C7ExcelDownload`** 或 Blob 文件名解析；由服务端响应头/直链行为决定浏览器是下载还是预览。

---

## 8. UI 结构（无外壳 **4A**）

建议自上而下：**选文件** → **策略**（**`ElRadioGroup`** 或 **`ElSegmented`**，实现计划择一）→ **导入** → **结果区**（无结果时可折叠或隐藏，实现计划写清）。

---

## 9. 验收与测试建议

- 非法扩展名或超出 **`maxSizeMb`**：**阻止上传**、有 **`notify`**、**不调用** **`uploadFn`**、**不** **`emit('error')`**。  
- 合法文件： **`uploadFn`** 收到正确 **`File`** 与 **`strategy`**。  
- 成功 resolve：统计展示正确；**`failCount>0` 且有 `errorFileUrl`**：链接可见。  
- **`uploadFn` reject**：**`notify` + `emit('error')`**。  
- **`uploading` 为 true** 时再次点导入：**`uploadFn` 调用次数不增加**。  
- **`reset()`** 后可再次选同一文件；统计清空；策略为 **`ignore`**。

---

## 10. 后续流程

- 实现前：由 **`writing-plans`** 产出分步实现计划（含 **`packages/index.js`** 注册与示例用法）。  
- 本文档经用户审阅无修改后，再进入编码阶段。
