# ui-c7-messagebox

## Purpose

为 **quick-ui** 提供 **C7MessageBox** 函数式工具：在 **Element Plus `ElMessageBox` / `ElLoading`** 之上统一 **默认配置**、**结构化返回值**、**`asyncConfirm` 自动 loading** 与 **`c7Loading`**。需求来源：**`原始需求/前端/C7消息弹窗工具.md`**；设计对齐：**`docs/superpowers/specs/2026-05-08-c7-messagebox-design.md`**。

## ADDED Requirements

### Requirement: 模块位置与导出方式

**C7MessageBox** MUST 位于 **`quick-ui/src/packages/C7MessageBox/`**，且 MUST **不**包含用于 **`installPackages`** 全局注册的 **`.vue`** 组件。

**`quick-ui/src/packages/index.js`** MUST **命名导出**：**`setMessageBoxDefaults`**、**`c7Confirm`**、**`c7Alert`**、**`c7Prompt`**、**`c7DangerConfirm`**、**`c7Loading`**（导出符号名实现阶段与设计一致即可，但 MUST 覆盖上述能力）。

#### Scenario: 可按需 import 使用

- **WHEN** 业务自 **`quick-ui/src/packages/index.js`**（或项目内约定的 **`@/packages`** 别名）**import** 上述任一函数
- **THEN** MUST **不**依赖 **`installPackages`** 注册 **Vue 组件** 即可调用

### Requirement: `setMessageBoxDefaults` 浅合并

**`setMessageBoxDefaults(config)`** MUST 将 **`config`** **浅合并**到模块级默认对象；后一次调用对同名字段的覆盖 MUST 生效，未出现的键 MUST 保留此前默认值。

各 **`c7*`** 调用传给 **Element Plus** 的最终 options MUST 为 **`{ ...moduleDefaults, ...perCallOptions }`**，其中 **`perCallOptions`** 优先级更高。

#### Scenario: 默认按钮文案被单次调用覆盖

- **WHEN** 已 **`setMessageBoxDefaults({ confirmButtonText: '知道了' })`** 且某次 **`c7Alert`** 传入 **`{ confirmButtonText: '好的' }`**
- **THEN** 该次弹窗的确定按钮文案 MUST 为 **`好的`**

### Requirement: 对话框类 API 统一返回值且不 reject 用户关闭

**`c7Confirm`**、**`c7Alert`**、**`c7Prompt`**、**`c7DangerConfirm`** MUST 均返回 **`Promise`**，且对用户 **取消**、**点击遮罩**、**ESC** 等导致 **Element Plus** 常见 **reject** 的路径，MUST **resolve** 为 **`{ action, value? }`**，其中 **`action`** MUST 为 **`'confirm'`**、**`'cancel'`** 或 **`'close'`**，且语义 MUST 与当前 **Element Plus** 版本下 **`MessageBoxData.action`** 的约定一致（**不**将 **`close`** 与 **`cancel`** 无文档地合并为单一值）。

**`value`**：在 **`c7Prompt`** 成功确认时 MUST 携带用户输入字符串（与 **EP** 返回形态对齐）；其它 API 在无输入语义时 **`value`** MAY 为 **`undefined`**。

#### Scenario: 用户点取消得到结构化结果而非异常

- **WHEN** 用户触发 **`c7Confirm`** 的取消侧关闭（与 **EP** 行为一致）
- **THEN** 返回的 **Promise** MUST **resolve**，且 **`action`** MUST 为 **`'cancel'`** 或 **`'close'`**（与 **EP** 该操作对应），且 MUST **不**因该路径 **reject** 到调用方

### Requirement: `c7Confirm` 与 `asyncConfirm`

当 **`options.asyncConfirm`** 为 **异步函数**（返回 **`Promise`**）时：

- 用户点击确定后，MUST 进入 **处理中** 状态；默认提示文案 MUST 为 **`处理中...`**，且 MUST 允许通过 **`options`** 覆盖该文案（字段名实现与 JSDoc 一致即可）。
- **Loading** 呈现 MUST **优先**使用 **Element Plus** 支持的 **`confirmButtonLoading`**（或当前版本等价能力）；若该能力不可用或不足以满足验收，MUST **回退**为 **`ElLoading.service`**，且实现代码 MUST 用注释说明回退触发条件。
- **`asyncConfirm` resolve** 后 MUST **关闭弹窗** 且返回的 **Promise** MUST **resolve** 为 **`{ action: 'confirm' }`**（无 **`prompt`** 输入时无 **`value`** 要求）。
- **`asyncConfirm` reject 或 throw** 时：弹窗 MUST **保持打开**；**loading** MUST 结束；若提供 **`options.errorNotify`**，MUST 调用 **`errorNotify(err)`**，其中 **`err`** 为失败原因（实现 MAY 规范为 **`unknown`**）；工具 MUST **不**默认调用 **`ElMessage.error`**。

第一版 MUST **仅**将 **`asyncConfirm`** 作为异步确认路径；MUST **不**与未在设计中约定的其它异步入口组合。

#### Scenario: asyncConfirm 成功关闭

- **WHEN** **`asyncConfirm`** 返回的 **Promise** **resolve**
- **THEN** 弹窗 MUST 关闭，且外层 **`c7Confirm`** 的 **Promise** MUST **resolve** 为 **`{ action: 'confirm' }`**

#### Scenario: asyncConfirm 失败触发 errorNotify 且弹窗仍开

- **WHEN** **`asyncConfirm`** **reject/throw** 且 **`errorNotify`** 已传入
- **THEN** **`errorNotify`** MUST 被调用，且弹窗 MUST 仍保持可交互的打开状态（允许用户再次确定或取消）

### Requirement: `c7Alert` 与 `c7Prompt`

**`c7Alert`** MUST 基于 **`ElMessageBox.alert`**，并应用与本规范一致的 **defaults** 与 **返回值包装**。

**`c7Prompt`** MUST 基于 **`ElMessageBox.prompt`**；输入校验 MUST **仅透传** **EP** 已有字段（例如 **`inputPattern`**、**`inputValidator`**、**`inputErrorMessage`**），MUST **不**新增包内 **`rules`/zod** 校验层。

#### Scenario: prompt 成功携带 value

- **WHEN** 用户在 **`c7Prompt`** 中输入 **`hello`** 并确认，且 **EP** 成功路径返回输入值
- **THEN** **Promise** MUST **resolve** 且 **`action`** 为 **`'confirm'`**，**`value`** MUST 为 **`'hello'`**

### Requirement: `c7DangerConfirm`

**`c7DangerConfirm`** MUST 为危险操作确认语义，且在 **`c7Confirm`** 之上叠加预设（至少包含 **`type: 'warning'`** 与确认按钮 **危险** 样式 class，具体 class 以实现时对照 **Element Plus** 版本为准），并继承相同的 **defaults** 与 **返回值** 规则。

#### Scenario: 危险确认仍走统一返回

- **WHEN** 用户关闭 **`c7DangerConfirm`** 的取消路径
- **THEN** 行为 MUST 符合「对话框类 API 统一返回值且不 reject 用户关闭」条款

### Requirement: `c7Loading`

**`c7Loading(text?, options?)`** MUST 调用 **`ElLoading.service`**（或项目内与 **EP** 对齐的等价入口），并将 **`text`** 映射到服务支持的文案参数；返回值 MUST 为 **`{ close() }`**，**`close`** MUST 结束该次 loading。

#### Scenario: close 结束 loading

- **WHEN** 调用方执行 **`c7Loading`** 返回对象上的 **`close()`**
- **THEN** 对应的全屏/局部 loading MUST 消失

### Requirement: 可维护注释

**`C7MessageBox`** 模块内导出的函数与关键内部辅助逻辑 MUST 具备 **`/** ... */` JSDoc**（或等价的 **TS** 文档注释），说明职责、入参/返回值、与 **Element Plus** 的映射及 **`asyncConfirm`/`errorNotify`** 契约；复杂分支（含 **reject → resolve** 映射、**loading** 回退）MUST 有简体中文行内注释。

#### Scenario: 新维护者可从注释理解不 reject 的原因

- **WHEN** 阅读 **`C7MessageBox`** 源码中包装 **`ElMessageBox`** 的入口函数
- **THEN** MUST 能从注释理解 **「用户取消类路径 resolve 为 `{ action }`」** 的设计意图
