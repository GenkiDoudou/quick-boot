# ui-c7-excel-upload

## Purpose

为 **quick-ui** 提供 **`C7ExcelUpload`**：统一 **Excel 文件选择**（**`.xls` / `.xlsx`**）、**大小上限**、**重复数据策略**（**`overwrite` | `ignore`**）、业务 **`uploadFn(file, strategy)`**、导入**统计展示**与**错误明细链接**，以及 **`uploading` / `reset` / 事件 / `notify`** 契约。需求来源：**`原始需求/前端/C7Excel导入.md`**、**`docs/superpowers/specs/2026-05-08-c7-excel-upload-design.md`**。

## Requirements

### Requirement: 组件与注册位置

**`C7ExcelUpload`** MUST 位于 **`quick-ui/src/packages/C7ExcelUpload`**（推荐单文件 **`index.vue`**），并通过 **`quick-ui/src/packages/index.js`** 的 **`installPackages(app)`** 注册为全局组件 **`C7ExcelUpload`**；亦 MAY **`import { C7ExcelUpload } from '@/packages'`** 按需使用。

#### Scenario: 全局注册可用

- **WHEN** 应用已执行 **`installPackages(app)`**  
- **THEN** 模板中 MUST 能使用 **`<C7ExcelUpload />`**（或等价全局名）

### Requirement: uploadFn 与结果类型

组件 MUST 接受 **`uploadFn: (file: File, strategy: 'overwrite' | 'ignore') => Promise<C7ExcelUploadResult>`**，其中 **`C7ExcelUploadResult`** MUST 包含 **`total: number`**、**`successCount: number`**、**`failCount: number`**，且 MAY 包含 **`errorFileUrl?: string`**。

组件 MUST **不**解析后端 **`R`** 包装；业务 MUST 在 **`uploadFn`** 内将接口响应整理为 **`C7ExcelUploadResult`** 后 **resolve**。

若 **`uploadFn` 未提供**，实现 MUST 采取 **no-op** 或 **开发环境 `console.warn`** 之一并在 JSDoc 写明。

#### Scenario: 成功导入后触发 success

- **WHEN** 用户选择合法文件、选择策略并触发导入，且 **`uploadFn` resolve** 为有效 **`C7ExcelUploadResult`**  
- **THEN** 组件 MUST **`emit('success', result)`**，且界面 MUST 展示 **`total` / `successCount` / `failCount`**

### Requirement: 文件选择与 accept

组件 MUST 使用 **隐藏 `input[type=file]`** 与可见触发控件（如 **`ElButton`**）打开文件选择器。

**`accept`** prop 默认 MUST 为 **`.xls,.xlsx`**（或与需求等价的 MIME/扩展名组合）；若允许覆盖，默认仍 MUST 满足「仅 Excel 扩展名」验收语义。

每次 **`change`** 处理完成后，组件 MUST **清空** **`input`** 的 **`value`**，以支持**重复选择同一文件**。

#### Scenario: 重复选择同一文件

- **WHEN** 用户两次选择磁盘上路径相同的文件  
- **THEN** 第二次选择 MUST 仍能触发 **`change`** 处理逻辑（不因 **`input` value 未变** 而失效）

### Requirement: 扩展名与大小校验

组件 MUST 校验所选文件扩展名为 **`.xls` 或 `.xlsx`**（实现 MUST 固定大小写策略并在 JSDoc 说明）。

组件 MUST 接受 **`maxSizeMb: number`**（**必填**），并 MUST 拒绝大于 **`maxSizeMb`**（按 **MB → 字节** 换算）的文件。

当校验失败时，组件 MUST 调用 **`notify('error', message)`**（未配置 **`notify`** 时 MUST 使用 **`ElMessage.error`**）；MUST **不**调用 **`uploadFn`**；MUST **不** **`emit('error')`**。

#### Scenario: 非法扩展名被拦截

- **WHEN** 用户选择扩展名非 **`.xls` / `.xlsx`** 的文件  
- **THEN** **`uploadFn` MUST NOT 被调用**，且 MUST 有错误提示

#### Scenario: 超出大小被拦截

- **WHEN** 用户选择文件大小大于 **`maxSizeMb`**  
- **THEN** **`uploadFn` MUST NOT 被调用**，且 MUST 有错误提示

### Requirement: 重复数据策略与文案

组件 MUST 支持 **`duplicateStrategy`** 为 **`'overwrite' | 'ignore'`**，并 MUST 支持 **`v-model:duplicateStrategy`**（**`update:duplicateStrategy`**）。

**`duplicateStrategy`** 默认值 MUST 为 **`'ignore'`**。

组件 MAY 接受 **`overwriteLabel`**、**`ignoreLabel`**；未提供时 MUST 使用中文默认文案（如 **「覆盖」/「忽略」**），并在 JSDoc 写明。

#### Scenario: 导入时传入 strategy

- **WHEN** 当前 **`duplicateStrategy` 为 `overwrite`** 且用户触发导入  
- **THEN** **`uploadFn` 的第二个参数 MUST 为 `'overwrite'`**

### Requirement: uploading 与防重复

组件 MUST 维护内部 **`uploading`**：**`uploadFn` 开始时** 为 **`true`**，在 **`finally`** 中为 **`false`**。

组件 MUST 支持 **`v-model:uploading`**（**`uploading` prop** 与 **`update:uploading`**）。

当 **`uploading === true`** 时，用户点击导入 MUST **不再次调用** **`uploadFn`**（**no-op**）。

#### Scenario: 上传中重复点击不重入

- **WHEN** **`uploading` 为 true** 且用户再次点击导入  
- **THEN** **`uploadFn` 调用次数 MUST NOT 增加**（相对单次导入流程）

### Requirement: reset 语义

组件 MUST **`defineExpose`** 暴露 **`reset`** 方法，且暴露的 **`uploading`** MUST 与内部状态一致（或文档化只读语义）。

**`reset()`** MUST：清空已选文件展示；将 **`input.value`** 置空；清空上次导入结果展示；将 **`duplicateStrategy`** 复位为 **`'ignore'`**。

#### Scenario: reset 后可再次选同一文件

- **WHEN** 用户已选择某文件后调用 **`reset()`**，再次选择同一文件  
- **THEN** 选择流程 MUST 仍生效（与 **input value 清空** 语义一致）

### Requirement: 导入结果与错误明细链接

当存在最近一次成功的 **`C7ExcelUploadResult`** 时，组件 MUST 展示 **`total` / `successCount` / `failCount`**。

当 **`failCount > 0`** 且 **`errorFileUrl`** trim 后非空时，组件 MUST 展示可点击的错误明细入口（如 **`<a :href="errorFileUrl" target="_blank" rel="noopener noreferrer">`**）；**不**要求使用 **`C7ExcelDownload`** 或 Blob 文件名解析链。

#### Scenario: 有失败且有 URL 时展示链接

- **WHEN** 最近一次结果为 **`failCount > 0`** 且 **`errorFileUrl`** 为非空字符串  
- **THEN** 用户 MUST 能看到指向 **`errorFileUrl`** 的入口

### Requirement: uploadFn 失败与 error 事件

当 **`uploadFn` reject**（或组件将其视为等价的异步失败）时，组件 MUST **`emit('error', err)`**，且 MUST **`notify('error', message)`**（未配置 **`notify`** 时 MUST 使用 **`ElMessage.error`**）。

#### Scenario: 接口失败有 error 与提示

- **WHEN** **`uploadFn` reject**  
- **THEN** **`emit('error')` MUST 发生**，且 MUST 有错误 **`notify` / `ElMessage`**

### Requirement: UI 与 attrs 透传

导入触发按钮（及可选的选文件按钮）MUST 使用 **`ElButton`**（或与设计一致的 Element 控件）；常用展示属性 SHOULD 通过 **`$attrs` 透传**至主要按钮，且实现 MUST 避免与内部事件名冲突（**`inheritAttrs: false`** 时在根按钮合并 **`class` / `style`**）。

组件 MUST **不**内置 **`ElDialog` / `ElCard`** 作为强制外壳。

### Requirement: notify

组件 MAY 接受 **`notify(type, message)`**，其中 **`type`** 至少覆盖 **`'success' | 'error' | 'warning' | 'info'`**。

当 **`notify` 未传入**时，需要用户可见提示的路径 MUST 使用 **`ElMessage`**（与 **`spec` 中各条「未配置 notify」** 一致）。

### Requirement: 验收对齐（与设计一致）

- **WHEN** 非法扩展名或超出 **`maxSizeMb`**  
- **THEN** **`uploadFn` MUST NOT 被调用`**，MUST 有提示，MUST **不** **`emit('error')`**

- **WHEN** 合法文件导入且 **`uploadFn` resolve`**  
- **THEN** 统计 MUST 正确展示；若 **`failCount > 0` 且有 `errorFileUrl`**，错误明细入口 MUST 可见
