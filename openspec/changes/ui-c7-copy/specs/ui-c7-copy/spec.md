# ui-c7-copy

## Purpose

为 **quick-ui** 提供 **`C7Copy`**：统一 **订单号 / 密钥 / 链接** 等纯文本的 **复制交互**、**成功或失败提示** 与 **Clipboard API + `execCommand` 降级**，支持多种 **`mode`**、**`getCopyText`（含 Promise）**、**`beforeCopy` / `afterCopy`** 与 **`copy` / `success` / `error`** 事件。需求来源：`原始需求/前端/C7复制.md`。

## ADDED Requirements

### Requirement: 组件与注册位置

**`C7Copy`** MUST 位于 **`quick-ui/src/packages/C7Copy`**（至少包含 **`index.vue`**），并通过 **`quick-ui/src/packages/index.js`** 的 **`installPackages(app)`** 注册为全局组件 **`C7Copy`**；亦 MAY **`import { C7Copy } from '@/packages'`** 按需使用。

#### Scenario: 全局注册可用

- **WHEN** 应用已执行 **`installPackages(app)`**
- **THEN** 模板中 MUST 能使用 **`<C7Copy />`** 而无需逐页 **import** 注册

### Requirement: `text` 类型与空值安全

组件 MUST 接受 **`text`** 为 **`String`** 或 **`Number`**。在参与 **`getCopyText`** 或复制写入前，**`null`** 与 **`undefined`** MUST 视为 **空字符串 `''`**；**`Number`** MUST 规范为 **`String(number)`**。

#### Scenario: null 与 undefined 不抛错

- **WHEN** **`text`** 为 **`null`** 或 **`undefined`** 且用户触发复制
- **THEN** 组件 MUST **不**因类型抛异常，且写入剪贴板的字符串 MUST 为 **`getCopyText('')` 的解析结果**（若未配置 **`getCopyText`** 则为 **`''`**）

### Requirement: `getCopyText` 与最终复制串

若提供 **`getCopyText`**，组件 MUST 以 **`getCopyText(baseText)`** 的结果作为待写入内容，其中 **`baseText`** 为 **`text`** 按上一条规范后的字符串。若返回 **Promise**，组件 MUST **await** 后再写入。若返回值非字符串，MUST **`String(...)`** 后再写入；**`null`/`undefined`** 解析结果 MUST 视为 **`''`**。

#### Scenario: 异步 getCopyText 成功后复制

- **WHEN** **`getCopyText`** 返回 **Promise**，且 **resolve** 为 **`'dynamic'`**
- **THEN** 剪贴板内容 MUST 为 **`'dynamic'`**，且 MUST **`emit('success', 'dynamic')`**（在写入成功后）

### Requirement: `mode` 与插槽

组件 MUST 支持 **`mode`** 取值：**`button`**、**`icon`**、**`text`**、**`clickable`**。字符串 **`none`** MUST 作为 **`clickable`** 的 **等价别名**（与原始需求兼容）。

- **`button`**：以 **`ElButton`**（或等价 EP 按钮）作为点击载体，文案/类型等由 props 控制。
- **`icon`**：以图标为主点击区，MAY 附带文案。
- **`text`**：可点击文本样式展示。
- **`clickable` / `none`**：不渲染内置装饰，**默认插槽** 为点击区域。

#### Scenario: none 与 clickable 行为一致

- **WHEN** **`mode`** 为 **`'none'`** 或 **`'clickable'`**
- **THEN** 用户点击 **默认插槽** 内可交互区域 MUST 触发与 **`clickable`** 相同的复制流程

### Requirement: 复制执行顺序（Clipboard 优先与降级）

组件 MUST **优先**使用 **`navigator.clipboard.writeText(resolvedText)`**，且仅在 **API 可用且处于允许调用的安全上下文**（实现 MUST 采用与主流浏览器一致的检测方式，例如 **`window.isSecureContext`** 与 **`clipboard`** 存在性组合，并在 JSDoc 说明）时走该路径。

当 **Clipboard 路径不可用** 或 **`writeText` 失败** 时，组件 MUST 使用 **`document.execCommand('copy')`** 配合 **临时可编辑元素选中 `resolvedText`** 的方式完成降级写入。

#### Scenario: 降级仍成功

- **WHEN** 运行环境 **无可用 Clipboard `writeText`**（模拟或不安全上下文）但 **`execCommand('copy')`** 仍可成功
- **THEN** 用户触发复制后剪贴板 MUST 得到 **`resolvedText`**，且 MUST **`emit('success', resolvedText)`**

### Requirement: `beforeCopy` 阻止语义

若提供 **`beforeCopy`**，组件 MUST 在用户确认复制后、解析 **`getCopyText`** **之前**调用 **`beforeCopy()`**。仅当返回值为 **严格 `false`** 时，组件 MUST **中止复制**；此时 MUST **不**调用 **`getCopyText`**、**不**写入剪贴板、**不** **`emit('copy'|'success'|'error')`**、**不**调用 **`afterCopy`**。

#### Scenario: beforeCopy 返回 false 无 success

- **WHEN** **`beforeCopy`** 返回 **`false`**
- **THEN** 组件 MUST **不** **`emit('success')`** 且 **不** **`emit('error')`**

### Requirement: `disabled`

当 **`disabled=true`** 时，组件 MUST **不**响应复制触发、**不**执行 **`getCopyText`**、**不**写入剪贴板、**不**触发 **`copy` / `success` / `error`**、**不**调用 **`afterCopy`**、**不**展示因复制产生的默认提示。

#### Scenario: disabled 不 emit

- **WHEN** **`disabled=true`** 且用户点击复制区域
- **THEN** 组件 MUST **不** **`emit('copy')`**

### Requirement: 事件载荷与时序

组件 MUST **`emit('copy', resolvedText)`**，其中 **`resolvedText`** 为 **已完成 `getCopyText` 解析**、**尚未写入剪贴板** 的最终字符串。

写入成功后，组件 MUST **`emit('success', resolvedText)`**；写入失败 MUST **`emit('error', err)`**，其中 **`err`** SHOULD 为 **`Error` 实例**（或实现将失败原因规范为 **`Error`** 并在 JSDoc 说明）。

若提供 **`afterCopy`**，组件 MUST 在 **剪贴板写入成功之后**、**默认成功提示之前**调用 **`afterCopy(resolvedText)`**。

#### Scenario: 成功链路顺序

- **WHEN** 复制成功且 **`beforeCopy`** 未阻止
- **THEN** **`emit('copy')`** MUST 早于 **`emit('success')`**；**`afterCopy`**（若存在）MUST 在 **`emit('success')`** **之前**执行

### Requirement: `showMessage` 与 `notify`

若传入 **`notify(type, message)`**，组件 MUST **仅**通过 **`notify`** 报告成功/失败（**不**再调用 **`ElMessage`**）。

若 **未**传入 **`notify`** 且 **`showMessage` 为 true**，组件 MUST 使用 **`ElMessage`**（或项目内等价封装）展示成功/失败。

若 **未**传入 **`notify`** 且 **`showMessage` 为 false**，组件 MUST **不**展示默认 **`ElMessage`** 提示。

#### Scenario: 自定义 notify 替代 ElMessage

- **WHEN** 传入 **`notify`** 且复制成功
- **THEN** 组件 MUST 调用 **`notify('success', ...)`**（或 design 中约定的 **`type` 取值**）且 MUST **不**调用 **`ElMessage.success`**

### Requirement: 异步复制重入

在一次复制流程 **未完成**（**`getCopyText` 的 Promise pending** 或 **剪贴板写入尚未结束**）期间，组件 MUST **忽略**新的复制触发（**不**并发执行第二次复制链），直至当前流程结束。

#### Scenario: 快速连点不产生双写

- **WHEN** **`getCopyText`** 为 **延迟 resolve 的 Promise** 且用户在 resolve 前再次点击
- **THEN** 组件 MUST **不**启动第二条并行的写入流程（以 **第一次** 触发为准直至结束）

### Requirement: 验收场景（与原始需求对齐）

在现代浏览器、**允许 Clipboard** 的环境下，**`writeText` 成功**时，组件 MUST **`emit('success')`**，且当 **`showMessage=true`** 且未自定义 **`notify`** 时 MUST 展示成功提示。

在 **Clipboard 不可用** 但 **`execCommand` 降级成功** 的环境下，组件 MUST 仍 **`emit('success')`**（且提示策略符合 **`showMessage` / `notify`** 规则）。

#### Scenario: 与安全上下文相关的最小验收

- **WHEN** 使用 **`https` 或 `localhost`** 等 **安全上下文** 且浏览器支持 **`clipboard.writeText`**
- **THEN** 一次用户点击复制 MUST 使剪贴板文本等于 **`resolvedText`** 且 MUST **`emit('success', resolvedText)`**
