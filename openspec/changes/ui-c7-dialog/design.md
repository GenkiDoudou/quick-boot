## Context

- **quick-ui**：Vue 3 + Element Plus；各页混用 **`ElDialog` / `ElDrawer`**，footer 与异步提交行为不一致。
- **原始需求**：**`原始需求/前端/C7弹窗抽屉.md`**。
- **Stakeholder 决策**（2026-05-07）：显隐双绑、loading、卸载、透传冲突、生命周期事件五条口径已确认，本文档固化。

## Goals / Non-Goals

**Goals**

- **单组件双壳**：**`mode=dialog`** → **`ElDialog`**；**`mode=drawer`** → **`ElDrawer`**；共享 **标题 / 正文插槽 / footer 策略**。
- **默认 footer**：**`footer=true`** 时 **左侧 `extra` 插槽** + **右侧 取消/确定**；**`footer` 插槽** 存在时 **完全自定义**（与原始需求一致）。
- **异步确定**：**`onConfirm`** 存在时 **确定钮 loading + 成功自动关 + 失败不关**；与 **`C7Button`** 的 **「reject=失败」** 心智对齐（**resolve** 表示可关窗的成功路径），**不在本组件内**包一层 **`checkSuccess`** 或全局 toast。
- **双 v-model**：关闭时 **两个 `update:*` 均发 `false`**。

**Non-Goals**

- 不复制 **`C7Button`** 全流水线（**`validateRef`、MessageBox 二次确认、debounce** 等）；若需要由 **footer 内放 `C7Button`** 或业务自行组合。
- 不解决 **跨应用多实例 z-index 全局仲裁**（沿用 EP **teleport / z-index** 策略即可）。

## Decisions

### 1. `modelValue` 与 `visible` 同时存在（按评审：主值 + 开发期提示）

- **受控主值**：以 **`modelValue`** 作为 **内部显隐逻辑的主绑定**（**优先**与 **`v-model:modelValue`** 对齐）。
- **兼容**：仍支持 **`v-model:visible`**；关闭时 **必须** **`emit('update:modelValue', false)`** 与 **`emit('update:visible', false)`**。
- **开发环境**：若 **同一时刻** **`modelValue` 与 `visible` 均为显式传入** 且 **布尔值不一致**，**`console.warn` 一次**（或 **同一打开周期内至多一次**），文案说明 **以 `modelValue` 为准**，避免静默双源。

### 2. 确定钮 `loading`：内部异步 + **允许外部 `confirmLoading` 覆盖**

- 组件内部维护 **`internalConfirmLoading`**（**`onConfirm` pending** 期间为 **`true`**）。
- **确定钮** 绑定 EP **`loading`** 的有效值为：**若调用方传入的 `confirmLoading` 不为 `undefined`**，则 **以 `confirmLoading` 为准**（**含 `false`**，即外部可强制关掉 loading）；**否则** 使用 **`internalConfirmLoading`**。
- **理由**：父级可在 **联动其它请求** 或 **强制复位** 时覆盖按钮态，而不与内部状态打架。

### 3. 卸载时仍打开：**仅同步 v-model 为 `false`**

- 在 **`onBeforeUnmount`**（或等价钩子）中，若当前判定为 **打开态**，**依次 `emit('update:modelValue', false)`** 与 **`emit('update:visible', false)`**（与关闭路径一致），使 **父级受控状态** 回落。
- **不**要求调用 **`ElDialog` / `ElDrawer` 实例方法** 做 **指令式 teardown**；依赖 Vue 卸载子树与 EP 默认行为清理 DOM。

### 4. 透传与内部默认 **冲突**时：**透传（调用方）优先**

- 对 **`ElDialog` / `ElDrawer`** 的 **属性/监听器** 透传（含 **`v-bind="modalProps"`** 等约定）时：若与组件 **内置** 的 **`modelValue`、footer 相关、title 相关** 等 **同名**，**以调用方传入为准**（实现上 **后合并覆盖** 或 **显式拆分「仅内部使用」字段**），并在 **JSDoc** 列出 **不建议覆盖** 的项及后果。

### 5. 生命周期与 **open / close** 等：**与 Element Plus 一致并转发**

- **直接转发**（或等价透传）底层 **`ElDialog` / `ElDrawer`** 的官方事件，例如 **`open`、`opened`、`close`、`closed`**（以 **当前项目锁定的 EP 版本文档** 为准）；与现有业务页对 **`el-dialog` 的 `@close`** 等习惯 **兼容**。
- **不**自造一套与 EP 不同名的「简化生命周期」；若需 **额外** 业务事件再在 **spec** 中单列。

## Risks / Trade-offs

| 风险 | 缓解 |
|------|------|
| **透传覆盖破坏内部 footer** | JSDoc 标明 **覆盖后果**；默认示例 **用 `modalProps` 只调尺寸/遮罩** |
| **双 v-model 父级只接一端** | 关闭仍 **双 emit**；主值规则见 **Decision 1** |

## Migration Plan

- 新弹窗/抽屉 **优先 `C7Dialog`**；旧页 **渐进替换**，无数据迁移。

## Open Questions

- （无）**`width` / `size(drawer)`** 命名与 **EP 对齐** 在 **spec** 的 props 表固化。
