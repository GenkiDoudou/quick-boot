# ui-c7-excel-download

## Purpose

为 **quick-ui** 提供 **`C7ExcelDownload`**：在 **Element Plus `ElButton`** 之上统一 **Blob 导出下载**（**`downloadFn`**、**`Content-Disposition` 文件名解析**、**`objectURL` 触发下载**、**`downloading`**、**JSON 错误 Blob** 处理与 **提示 / 事件**），并与 **`request.js`** 可选返回 **响应头** 的能力配合。需求来源：**`原始需求/前端/C7Excel下载.md`**、**`docs/superpowers/specs/2026-05-08-c7-excel-download-design.md`**。

## Requirements

### Requirement: 组件与注册位置

**`C7ExcelDownload`** MUST 位于 **`quick-ui/src/packages/C7ExcelDownload`**（推荐单文件 **`index.vue`**），并通过 **`quick-ui/src/packages/index.js`** 的 **`installPackages(app)`** 注册为全局组件 **`C7ExcelDownload`**；亦 MAY **`import { C7ExcelDownload } from '@/packages'`** 按需使用。

### Requirement: downloadFn 与返回值归一

组件 MUST 接受 **`downloadFn: () => Promise<Blob | { data: Blob; headers?: Record<string, unknown> }>`**（**`headers`** 类型 MAY 与 axios 头类型对齐）。

- 当 resolve 值为 **`Blob`** 时，组件 MUST 将其作为唯一数据体进入后续 **Blob 校验** 与 **文件名解析**（**不从**响应头解析文件名，除非业务另行通过 **`fileName` prop** 或 **`defaultFileName`** 提供）。
- 当 resolve 值为 **`{ data, headers }`** 时，组件 MUST 使用 **`data`** 作为 **Blob**；**`headers`** MAY 用于 **文件名解析**（见下条）。

若 **`downloadFn` 未提供**，实现 MUST 采取 **no-op** 或 **开发环境 `console.warn`** 之一并在 JSDoc 写明。

### Requirement: request 层可选携带响应头

**`quick-ui/src/utils/request.js`**（或同仓库内与之配套的薄封装）MUST 提供一种**向后兼容**机制，使调用方在 **`responseType: 'blob'`** 场景下**可选**获得 **`{ data: Blob; headers }`**，而**不改变**未启用该机制时「仅返回 **`Blob`**」的行为。

#### Scenario: 默认 downloadRequest 行为不变

- **WHEN** 调用方使用与现状等价的 **`downloadRequest`** 且**未**启用「带 headers」机制  
- **THEN** 解析结果 MUST 仍为 **`Blob`**（或与变更前一致的类型），**MUST NOT** 强制所有调用方适配新返回形态

#### Scenario: 启用带 headers 时可解析 Content-Disposition

- **WHEN** 调用方启用该机制并成功收到导出响应  
- **THEN** 调用方 MUST 能读取 **`headers`**（至少包含 **`content-disposition`** 的可用形态），以便 **`downloadFn`** 返回 **`{ data, headers }`**

### Requirement: 文件名解析优先级

系统 MUST 按以下**严格优先级**确定用于 **`HTMLAnchorElement.download`** 的文件名（及 **`success`** 事件载荷）：

1. **`fileName` prop**：trim 后非空则 **直接使用**，**MUST NOT** 再解析 **`Content-Disposition`**。
2. **`Content-Disposition`** 中的 **`filename*=UTF-8''...`**（RFC 5987）：对 **`''`** 后片段执行 **`decodeURIComponent`**；解析失败则 **顺延**下一项。
3. **`filename="..."`** 或 **`filename=...`**（无引号 token 边界在实现中固定并在 JSDoc 说明）。
4. **`defaultFileName`**：当 **1～3** 均未得到有效文件名时使用。

若 **1～4** 均无法得到有效文件名，系统 MUST **不**触发文件下载，MUST **`emit('error', err)`**，MUST 通过 **`notify('error', message)`**（未配置 **`notify`** 时 MUST 使用 **`ElMessage.error`**）提示用户。

#### Scenario: fileName 优先于响应头

- **WHEN** **`fileName` prop** 为非空字符串，且 **`headers`** 中存在 **`Content-Disposition`**  
- **THEN** 最终文件名 MUST 为 **`fileName`** 所指字符串

#### Scenario: filename-star 优先于 filename

- **WHEN** **`fileName` 未提供**，且 **`Content-Disposition`** 同时包含 **`filename*=UTF-8''`** 与 **`filename="..."`**  
- **THEN** 最终文件名 MUST 来自 **`filename*`** 规则解析结果

### Requirement: Blob 校验与 JSON 错误体

在触发 **`URL.createObjectURL`** 之前，组件 MUST 使用与 **`blobValidate`**（**`@/utils/ruoyi.js`**）**等价**的判定：**`blob.type !== 'application/json'`** 才允许进入**文件下载**路径。

当 **`blob.type === 'application/json'`** 时，组件 MUST **`await blob.text()`** 并尝试 **`JSON.parse`**；MUST 优先展示后端 **`msg`**，并 MAY 复用 **`errorCode`** 映射（与 **`request.js`** 的 **`download`** 行为对齐）；MUST **`emit('error', err)`** 且 **`notify('error', message)`**；MUST **不**创建 **`objectURL`**、**不**触发下载。

#### Scenario: JSON Blob 不触发下载

- **WHEN** **`downloadFn` resolve** 的 **`Blob`** 的 **`type`** 为 **`application/json`**  
- **THEN** 浏览器 MUST NOT 收到本组件发起的文件下载，且 MUST 触发 **错误提示** 与 **`error` 事件**

### Requirement: 下载机制与资源释放

组件 MUST 使用 **`URL.createObjectURL(blob)`** 与程序化 **`HTMLAnchorElement.click()`** 触发下载；在 **`click()`** 之后 MUST 通过 **`requestAnimationFrame` 或 `setTimeout(0)`** 延迟调用 **`URL.revokeObjectURL`**，以降低未读完即释放的风险。

### Requirement: downloading 与重复点击

组件 MUST 维护内部 **`downloading`** 状态：在 **`downloadFn`** 调用开始时为 **`true`**，在 **`finally`** 中为 **`false`**。

组件 MUST 支持 **`v-model:downloading`**（**`downloading` prop** 与 **`update:downloading`**）。

当 **`downloading === true`** 时，用户点击按钮 MUST **不再次调用** **`downloadFn`**（**no-op**）。

#### Scenario: 下载中重复点击不重入

- **WHEN** **`downloading` 为 true** 且用户再次点击  
- **THEN** **`downloadFn` 调用次数 MUST NOT 增加**（相对单次下载流程）

### Requirement: UI 与 attrs 透传

组件 MUST 以 **`ElButton`** 为默认根展示；**`:loading`** MUST 绑定 **`downloading`**（或与 Element **`loading`** 语义等价的单一真值来源）。

常用 **`ElButton`** 展示属性 MUST 通过 **`$attrs` 透传**（实现 MUST 避免 **`@click`** 等内部绑定与 **`$attrs` 冲突**）。

默认插槽 MUST 作为按钮文案；若无插槽，实现 MAY 使用默认文案（如 **「导出」**），并在 JSDoc 写明。

### Requirement: notify 与错误提示

组件 MAY 接受 **`notify(type, message)`**，其中 **`type`** 至少覆盖 **`'success' | 'error' | 'warning' | 'info'`**。

当 **`notify` 未传入**时，错误路径 MUST 使用 **`ElMessage`**（如 **`ElMessage.error`**）展示 **`message`**。

### Requirement: 事件

组件 MUST **`emit('success', fileName: string)`** 在下载链按设计成功完成后触发。

组件 MUST **`emit('error', err: unknown)`** 在 **`downloadFn` reject**、**JSON 错误 Blob**、**无可用文件名**、**`objectURL` / 点击链异常**等失败路径触发。

### Requirement: 验收场景（与原始需求对齐）

- **WHEN** **`downloadFn` 仅返回 `Blob`** 且提供了 **`fileName` 或 `defaultFileName`**  
- **THEN** 浏览器 MUST 能成功保存文件，且文件名 MUST 符合 **文件名解析优先级**

- **WHEN** **`downloadFn` 返回 `{ data, headers }`** 且 **`Content-Disposition`** 含 **`filename*`** 或 **`filename=`**  
- **THEN** 本地文件名 MUST 符合 **文件名解析优先级**

- **WHEN** **`downloading` 为 true** 且用户再次点击  
- **THEN** **`downloadFn` MUST NOT 被再次调用**
