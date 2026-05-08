# C7MessageBox（C7 消息弹窗工具）设计说明

**日期**：2026-05-08  
**状态**：已定稿（经 brainstorming 澄清与「设计批准」）  
**依据**：`原始需求/前端/C7消息弹窗工具.md` + Q&A 结论（1A、2A、3A、4A、5A、6A、7A）  
**方案选型**：混合薄封装（在 Element Plus 之上统一默认值、返回值与 `asyncConfirm` 行为）

---

## 1. 背景与目标

- **背景**：页面中 `confirm` / `alert` / `prompt`、危险确认、全局 loading 等调用形式不统一，易出现交互不一致与重复代码；部分组件（如 `C7Button`、`C7Switch`）直接使用 `ElMessageBox`，标题等硬编码分散。
- **目标**：基于 Element Plus **`ElMessageBox`** / **`ElLoading`** 提供**函数式**工具：统一结构化结果、支持异步确认自动 loading、支持全局默认配置。
- **非目标（第一版）**：不封装 `ElMessage` / `ElNotification`；不批量迁移存量组件内调用（见第 10 节）。

---

## 2. 模块边界与落点

| 项 | 约定 |
|----|------|
| **目录** | `quick-ui/src/packages/C7MessageBox/` |
| **形态** | **无 `.vue`**；导出纯函数与类型（若工程为 JSDoc 则类型写在注释中）。 |
| **全局注册** | **不**通过 `installPackages` 注册组件；仅在 `quick-ui/src/packages/index.js` **增加命名导出**，供业务 `import { c7Confirm, … } from '…/packages'`。 |
| **应用初始化** | 在 **`main.ts`**（或应用等价入口）**可选**调用一次 **`setMessageBoxDefaults(config)`** 写入全局默认项。 |

---

## 3. 技术选型

- **对话框**：`ElMessageBox.confirm` / `alert` / `prompt`（与 EP 版本对齐的 import 路径以工程为准）。
- **Loading**：`ElLoading.service`。
- **默认值合并**：**浅合并** — 每次调用 `merged = { ...moduleDefaults, ...perCallOptions }`（`perCall` 覆盖同名字段）。

---

## 4. 全局默认配置

### 4.1 `setMessageBoxDefaults(config)`

- **语义**：合并到模块级可变「默认 options」对象（**浅合并**；后一次调用覆盖前一次同名字段，未传的键保留上一版本默认值）。
- **`config` 范围**：以 EP **`MessageBoxOptions`**（及团队文档中列举的常用项：按钮文案、`draggable`、`closeOnClickModal` 等）为主；**未在类型/JSDoc 中列出的键仍允许透传**至 EP，只要 EP 支持。

### 4.2 与单次调用的关系

- 各 `c7*` 最终传给 EP 的 options = **`{ ...defaults, ...fnOptions }`**（函数第三个参数或等价位置），后者优先。

---

## 5. 统一返回值与不抛异常

### 5.1 对外类型（概念）

所有 `c7Confirm` / `c7Alert` / `c7Prompt` / `c7DangerConfirm` 均返回：

```ts
Promise<{ action: 'confirm' | 'cancel' | 'close'; value?: string }>
```

- **`action`**：与 **Element Plus `MessageBoxData.action` 语义对齐**（用户取消、点击遮罩、ESC 等与 EP 文档一致时映射为 `cancel` / `close`）。
- **`value`**：主要在 **`c7Prompt`** 成功确认时携带输入字符串；其余场景可为 `undefined`。
- **约束**：**不向调用方 reject「用户取消类」路径** — 内部捕获 EP 的 reject，转换为上述结构的 **`resolve`**。业务侧可用 `await` + 判断 `action`，无需 `try/catch` 处理取消。

### 5.2 实现注意

- 需对照当前项目使用的 **Element Plus 大版本**文档，确认 `MessageBoxData` 的 `action`、`value` 形态；若 EP 将某操作标为 `close` 而非 `cancel`，本工具**原样透出**，不强行合并为单一 `dismiss`。

---

## 6. 对外 API 一览

### 6.1 `c7Confirm(message, title?, options?)`

- **语义**：确认/取消类对话框；`message` / `title` 与 EP 一致（`title` 可选时与 EP 调用签名对齐）。
- **`options.asyncConfirm`**（可选）：类型为 **`() => Promise<unknown>`**（或文档中写清的等价异步零参函数）。
  - 点击**确定**后：进入异步流程；期间展示**处理中**状态（默认文案 **`处理中...`**，允许通过 options 覆盖）。
  - **Loading 表现**：优先使用 EP 提供的 **`confirmButtonLoading`**（或当前版本等价 API）绑定确定按钮；若某 EP 版本能力不足再退化为 **`ElLoading.service`**，并在实现代码注释中注明触发条件。
  - **成功**：关闭弹窗并 **`resolve({ action: 'confirm' })`**。
  - **失败**：**保持弹窗打开**、结束 loading；调用 **`options.errorNotify?.(err)`**（**`errorNotify: (err: unknown) => void`**），**不在工具内**默认调用 `ElMessage`。
- **二义性约束**：若同时存在多种「确定后异步」入口，**第一版仅支持 `asyncConfirm` 作为唯一异步确认路径**；不与未约定的其它字段组合（避免实现与文档分叉）。

### 6.2 `c7Alert(message, title?, options?)`

- **语义**：仅确定按钮；同样应用 defaults + 统一返回值包装。

### 6.3 `c7Prompt(message, title?, options?)`

- **语义**：带输入框；**输入校验仅透传** EP 的 `inputPattern`、`inputValidator`、`inputErrorMessage` 等，**不**在包内增加 `rules` / zod 层（YAGNI）。
- **成功**：`resolve({ action: 'confirm', value: string })`（具体字段名与 EP `MessageBoxData` 对齐）。

### 6.4 `c7DangerConfirm(message, title?, options?)`

- **语义**：危险操作确认；在 `c7Confirm` 基础上套预设，例如 **`type: 'warning'`** + 确认按钮使用 **危险样式**（具体 class 与 EP 按钮 variant 以实现时对照 EP 版本为准，与现有后台管理危险操作视觉一致即可）。

### 6.5 `c7Loading(text?, options?)`

- **语义**：封装 **`ElLoading.service`**；`text` 映射到 service 支持的文案参数；返回 **`{ close() }`**，`close` 用于结束全屏/局部 loading。

---

## 7. 错误与边界

- **`asyncConfirm` 抛错**：由 **`errorNotify`** 消费；若无 `errorNotify`，失败时仍保持「弹窗不关 + loading 结束」，**不**静默吞错（开发环境可考虑 `console.error`，是否在文档中写死由实现阶段与 ESLint 约定决定）。
- **EP 升级**：类型定义与 `MessageBoxData` 依赖 EP；大版本升级时回归「返回值映射」与 **`asyncConfirm` + `confirmButtonLoading`** 分支。

---

## 8. 验收标准（与原始需求对齐）

- **`asyncConfirm`**：点击确定后出现「处理中...」（或覆盖文案）；执行成功自动关闭；失败保持打开并触发 **`errorNotify`**。
- **`cancel` / `close`**：不抛未处理异常；统一为 **`resolve({ action: 'cancel' | 'close', … })`**。
- **全局默认**：`setMessageBoxDefaults` 后，未在单次调用中覆盖的项生效；浅合并行为可测。

---

## 9. 测试与文档（实现阶段）

- **单测（推荐）**：对 EP **mock** 断言 — reject → 转为对应 `action`；`asyncConfirm` resolve/reject 分支；defaults 浅合并。
- **若仓库暂无单元测试基础设施**：第一版以 **VitePress/组件文档中的示例** + 清单化手工用例为主，并在后续迭代补测。
- **文档**：与现有 C7 文档站点风格一致，补充 API 表、`setMessageBoxDefaults` 与 `asyncConfirm` 示例。

---

## 10. 后续迭代（非第一版范围）

- 可选：将 `C7Button`、`C7Switch` 等内部的 **`ElMessageBox.confirm`** 逐步迁移为 `c7Confirm`，统一标题与按钮文案来源（与 7A「第一版不批量迁移」不冲突）。

---

## 11. 自检记录（定稿时）

- **占位符**：无 TBD。
- **一致性**：封装范围仅 MessageBox + Loading；返回值与 EP `action` 对齐；`asyncConfirm` 与 `errorNotify` 契约与 Q&A 一致。
- **范围**：单模块工具包 + 导出 + 文档；不强制改存量组件。
- **歧义消除**：异步确认仅 `asyncConfirm`；Prompt 不扩展校验封装。
