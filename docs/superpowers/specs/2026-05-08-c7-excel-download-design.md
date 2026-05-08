# C7ExcelDownload（C7 Excel / Blob 下载按钮）设计说明

**日期**：2026-05-08  
**状态**：已定稿（经 brainstorming 澄清与「确认定稿」）  
**依据**：`原始需求/前端/C7Excel下载.md` + Q&A 结论（**1B、2A、3A、4A、5A、6A**）及实现路径 **路径 1**（单文件 `index.vue` + 必要时同文件内纯函数）

---

## 1. 背景与目标

- **背景**：导出接口通常返回 **Blob**，文件名可能在响应头 **`Content-Disposition`** 中；页面需要统一的下载按钮、下载触发、进行中状态与错误提示。
- **目标**：提供 **`C7ExcelDownload`**：点击执行 **`downloadFn`**，解析文件名并触发浏览器下载，管理 **`downloading`**，失败时 **`notify` + `emit('error')`**；成功 **`emit('success', fileName)`**。
- **非目标**：不强制替换全局 **`$download` / `download()`** 的既有行为；不限定仅 Excel 扩展名（组件名为系列命名，**6A**）；不负责业务查询参数与 URL 拼装（由业务 API / `downloadRequest` 等负责）。

---

## 2. 命名、边界与落点

| 项 | 约定 |
|----|------|
| **对外组件名** | **`C7ExcelDownload`** |
| **目录** | **`quick-ui/src/packages/C7ExcelDownload/index.vue`** |
| **实现形态** | **单文件组件**；`Content-Disposition` 解析、`objectURL` 触发下载等可在同文件底部以 **纯函数** 形式组织，避免过早抽包。若后续非按钮场景也要复用，再评估 **`useC7BlobDownload`**（**本期不做**）。 |
| **全局注册** | 在 **`quick-ui/src/packages/index.js`** 中 **`export`** 与 **`installPackages`** 注册，与其它 C7 一致。 |

**职责**：触发 **`downloadFn`**；归一返回值；按优先级解析文件名；校验 Blob（含 JSON 错误体）；`objectURL` + `<a download>` 下载并 **`revokeObjectURL`**；**`downloading`** 与 **`v-model:downloading`**；默认 **`ElMessage`** 错误提示。

**不负责**：axios 实例的全局策略改造（除下文约定的 **可选 headers 返回** 外）；未传入 **`downloadFn`** 时的占位行为（实现时若 `downloadFn` 缺失可 `console.warn` + no-op，以代码评审为准）。

---

## 3. 与 `downloadRequest` / `request` 的关系（**1B**）

- **分工**：业务侧继续使用 **`downloadRequest`**（或等价 axios 调用）发起导出请求；**`downloadFn` 由父组件传入**，组件负责点击、文件名解析、触发下载与状态。
- **现状与缺口**：当前响应拦截器在 **`responseType === 'blob'`** 时 **`return res.data`**，调用方默认**仅得 `Blob`**，**无法**读取 **`Content-Disposition`**，因此文件名优先级 **第 4 节之 2）、3）** 在无补充手段时不可用。
- **实现阶段要求**（写入实现计划）：在 **`quick-ui/src/utils/request.js`**（或同层薄封装）增加**向后兼容**能力，使调用方可选拿到 **`{ data: Blob, headers }`**，例如：
  - **`axios` 请求 `config` 显式开关**（如 **`returnBlobWithHeaders: true`**），拦截器在 blob 分支：若开启则 **`return { data: res.data, headers: res.headers }`**，否则仍 **`return res.data`**；**或**
  - 新增 **`downloadRequestWithHeaders(url, params, config)`**，与 **`downloadRequest`** 同鉴权、同 **`baseURL`**，仅返回形态含 **headers**。
- **组件归一**：`downloadFn` 的 resolve 值支持 **`Blob`** 或 **`{ data: Blob; headers?: Record<string, unknown> }`**（headers 以实现时 axios 类型为准）；若仅有 **`Blob`**，文件名仅依赖 **`fileName` prop** 与 **`defaultFileName`**（**第 4 节步骤 1、4**）。

---

## 4. 文件名解析优先级（与原始需求一致）

1. **`fileName` prop**：非空（trim 后）则直接使用，**不再**解析响应头。
2. **`Content-Disposition`** 中的 **`filename*=UTF-8''...`**（RFC 5987）：对 `''` 后片段做 **`decodeURIComponent`**（解析失败则顺延下一项）。
3. **`filename="..."`** 或 **`filename=...`**（无引号时按 token 规则截取，实现计划写清边界）。
4. **`defaultFileName`**：在 **1～3** 均未得到有效文件名时使用。

若 **1～4** 均无法得到可用文件名：**不触发下载**，**`emit('error', err)`**（`err` 可为 **`Error`** 或项目约定类型），**`notify('error', message)`**（默认 **`ElMessage.error`**，文案在实现计划中固定一句中文即可）。

---

## 5. `downloadFn`、事件与 `downloading`（**5A**）

| 项 | 约定 |
|----|------|
| **`downloadFn`** | **`() => Promise<Blob \| { data: Blob; headers?: ... }>`**；由业务封装 **`downloadRequest` / 带 headers 的变体**。 |
| **`success`** | **`emit('success', fileName: string)`**，在浏览器下载链成功触发后（与 **`revokeObjectURL`** 顺序以实现计划为准，须保证 `fileName` 为最终采用的字符串）。 |
| **`error`** | **`emit('error', err: unknown)`**；凡 **`downloadFn` reject**、JSON 伪装 Blob、无可用文件名、创建 URL / 点击链异常等均触发。 |
| **`downloading`** | 内部 **`ref(false)`**；在 **`downloadFn` 调用开始时** 置为 **`true`**，在 **`finally`** 中置为 **`false`**。 |
| **对外同步** | **`v-model:downloading`**（**`downloading` prop + `update:downloading`**），父组件可读写过载态；组件内部开始下载时仍应 **`emit('update:downloading', true)`** 等，与 Vue 3 习惯一致。 |
| **重复点击** | **`downloading === true`** 时点击 **no-op**（不再次调用 **`downloadFn`**）（**5A**）。 |

---

## 6. Blob 校验与 JSON 错误体（**4A**）

- 得到 **`Blob`** 后使用与 **`blobValidate`**（**`@/utils/ruoyi`**）**等价**的判定：**`blob.type !== 'application/json'`** 视为可尝试当文件下载；否则视为错误响应体。
- 若类型为 **`application/json`**：**`await blob.text()`** → **`JSON.parse`**（**try/catch**）→ 优先取后端 **`msg`**，其次与 **`download()`**（**`request.js`**）对齐的错误码表（**`errorCode`**）若可复用则复用；**`notify('error', message)`** + **`emit('error', err)`**；**不**创建 **`objectURL`**、**不**触发下载。
- **`downloadFn` reject**：**`notify('error', message)`**（默认将 **`err`** 转为可读字符串，与现有 axios 错误提示风格协调即可）+ **`emit('error', err)`**。

---

## 7. 下载机制与资源释放

- **`URL.createObjectURL(blob)`** + 内存中创建 **`<a>`**，设置 **`download`** 为 **第 4 节** 解析得到的文件名（若浏览器忽略 `download` 仍可能以 URL 展示，以浏览器行为为准），**程序化 `click()`**。
- **`URL.revokeObjectURL`**：在 **`click()`** 之后使用 **`requestAnimationFrame` 或 `setTimeout(0)`** 延迟释放，避免部分浏览器未读完即 revoke。

---

## 8. UI（**2A**）

- 默认使用 **`ElButton`**；**`:loading="downloading"`**（或与 Element **`loading`** 语义对齐）。
- 常用展示属性（**`type` / `size` / `disabled` / `plain` / `round` / `link` 等**）通过 **`v-bind="$attrs"`** 透传，**`inheritAttrs: false`** 时在根 **`ElButton`** 上合并 **`class` / `style`**（实现计划二选一，避免重复绑定内部事件名即可）。
- **默认插槽**：按钮文案；无插槽时可用默认文案如 **「导出」**（实现计划与 i18n 策略以代码评审为准，设计不强制词条文件）。

---

## 9. `notify` 与默认行为（**3A**）

- **`notify?: (type: 'success' \| 'error' \| 'warning' \| 'info', message: string) => void`**。
- 未传入时，错误路径使用 **`ElMessage`**（**`ElMessage.error` 等**）与现有 **`download()`** 失败提示习惯一致。

---

## 10. 验收与测试建议

- **`downloadFn` 仅返回 `Blob`** + **`fileName` 或 `defaultFileName`** → 成功下载，文件名为预期。
- **`downloadFn` 返回 `{ data, headers }`**，**`Content-Disposition`** 含 **`filename*`** / **`filename=`** → 本地文件名符合 **第 4 节** 优先级。
- **返回 `application/json` 的 Blob** → 不下载，有 **`error` + notify**。
- **`downloading` 为 true** 时再次点击 → **`downloadFn` 调用次数不增加**。

---

## 11. 后续流程

- 实现前：由 **`writing-plans`** 产出分步实现计划（含 **`request.js` 扩展点** 与组件联调）。
- 本文档经用户审阅无修改后，再进入编码阶段。
