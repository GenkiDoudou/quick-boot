# ui-c7-button

## Purpose

为 **quick-ui** 提供 **`C7Button`**：在 **Element Plus `ElButton`** 之上统一常见业务按钮的 **预设外观** 与 **点击流水线**（防抖、防重入、可选校验与确认、异步 loading、成功/失败提示、标准化事件），减少页面重复逻辑。需求来源：`原始需求/前端/C7按钮.md`。

## Requirements

### Requirement: 预设类型

系统 MUST 支持通过 **`btnType`** 使用预设：`add`、`edit`、`delete`、`query`、`refresh`、`upload`、`download`、`submit`、`cancel`。每种预设 MUST 提供默认 **文案（label）**、**Element 按钮 type**、**plain** 与 **图标（Element Plus Icons）**。显式传入的 **`label`、`type`、`plain`、`size`** MUST 覆盖对应预设字段。

### Requirement: 点击流水线顺序

一次有效点击触发的逻辑顺序 MUST 为：

1. 触发 **`before-click`** emit（无载荷）。
2. 若配置了 **`beforeClick`** 函数：调用其结果；若为 **`false`**（同步或异步解析后），MUST 中止流水线，触发 **`after-click(false)`**，MUST NOT 弹出错误类 toast。
3. 若 **`validate=true`** 且提供了 **`validateRef`**：对 **`unref(validateRef)`** 调用 **`validate()`**。若校验失败，MUST 中止流水线，触发 **`after-click(false)`**，MUST NOT 弹出错误类 toast。
4. 若 **`confirm=true`**：若存在 **`confirmFn`**，MUST 以其返回值决定是否继续；否则 MUST 使用 **`ElMessageBox.confirm`**，文案默认为 **`confirmMessage`**。用户取消或 **`confirmFn`** 返回 false MUST 中止流水线，触发 **`after-click(false)`**，MUST NOT 弹出错误类 toast。
5. 执行 **`clickFunction()`**；其 MUST 支持 **Promise**。执行期间按钮 MUST 处于 **loading**（仅本阶段）。
6. 若 **`clickFunction`** resolve：调用 **`checkSuccess(result)`**；若为 false，MUST 视为失败：触发 **`error`** emit，且在 **`showErrorToast=true`** 时展示失败提示；然后 **`after-click(false)`**。
7. 若 **`clickFunction`** resolve 且 **`checkSuccess`** 为 true：触发 **`success`** emit（载荷为 result）；按 **`successMessage` / `successNotify`** 规则展示成功提示（可无提示）；然后 **`after-click(true)`**。
8. 若 **`clickFunction`** reject：触发 **`error`** emit；在 **`showErrorToast=true`** 时展示失败提示；然后 **`after-click(false)`**。

### Requirement: 防抖与并发

系统 MUST 使用 **`debounceDelay`**（默认 **300ms**）对点击入口防抖，语义为 **leading 触发、trailing 不补发**（同窗口内重复点击不重复开启流水线）。在整个流水线执行期间（含校验与确认等待），系统 MUST 通过内部 **`busy`** 状态阻止重入。**`internalLoading`** MUST 仅在 **`clickFunction`** 执行期为 true。

### Requirement: 与 axios 封装一致

**`clickFunction`** 的契约 MUST 与项目 **`request`** 习惯一致：**HTTP/封装层失败**通过 **Promise reject** 传递；**resolve** 表示可进入 **`checkSuccess`** 判定。**`checkSuccess`** 默认 MUST 为恒 true（即 resolve 即业务成功，除非调用方覆盖）。

### Requirement: 事件与命名

组件 MUST 使用 Vue **`emit`** 暴露：**`before-click`**、**`success`**、**`error`**、**`after-click(success: boolean)`**。MUST NOT 要求调用方使用 **`successCallback`/`errorCallback` props** 作为主要扩展点（与 Vue 惯例及当前实现一致）。

### Requirement: 全局可用性

**`C7Button`** MUST 位于 **`quick-ui/src/packages/C7Button`**，并通过 **`packages/index.js`**（如 **`installPackages(app)`**）在应用入口（**`main.js`**）注册为全局组件，名称 **`C7Button`**，以便业务模板直接使用；亦 MAY **`import { C7Button } from '@/packages'`** 按需使用。

### Requirement: 验收场景

- **`btnType=delete`** 且 **`confirm=true`**：MUST 先完成确认，再执行 **`clickFunction`**。
- **`clickFunction`** 对应的 Promise 未完成前：按钮 MUST 显示 **loading**，且 MUST NOT 重复执行该异步逻辑。
- **`validate=true`** 且校验失败：MUST NOT 进入确认与 **`clickFunction`**。
