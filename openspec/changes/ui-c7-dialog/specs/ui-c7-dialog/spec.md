# ui-c7-dialog

## Purpose

为 **quick-ui** 提供 **`C7Dialog`**：在 **`ElDialog` / `ElDrawer`** 之上统一 **footer、异步确定、双 v-model、插槽与透传**；需求来源 **`原始需求/前端/C7弹窗抽屉.md`**，设计决策见同变更 **`design.md`**（**modelValue 主值 + dev warn、`confirmLoading` 覆盖、卸载仅双 emit、透传优先、EP 事件转发**）。

## ADDED Requirements

### Requirement: 组件与注册位置

**`C7Dialog`** MUST 位于 **`quick-ui/src/packages/C7Dialog`**（至少 **`index.vue`**），并通过 **`quick-ui/src/packages/index.js`** 的 **`installPackages(app)`** 注册为全局组件 **`C7Dialog`**；亦 MAY **`import { C7Dialog } from '@/packages'`** 按需使用。

#### Scenario: 全局注册可用

- **WHEN** 应用已执行 **`installPackages(app)`**
- **THEN** 模板中 MUST 能使用 **`<C7Dialog />`**

### Requirement: `mode` 与底层组件

**`mode`** MUST 支持 **`'dialog'`** 与 **`'drawer'`**。当 **`mode='dialog'`** 时 MUST 使用 **`ElDialog`** 作为壳；当 **`mode='drawer'`** 时 MUST 使用 **`ElDrawer`** 作为壳。

#### Scenario: 两种模式均可打开关闭

- **WHEN** **`modelValue` 为 `true`** 且 **`mode`** 分别为 **`dialog`** 与 **`drawer`**
- **THEN** 用户 MUST 能看到对应 **EP** 组件呈现，且能通过 **关闭路径** 将 **`modelValue`** 置为 **`false`**

### Requirement: 显隐与双 v-model

组件 MUST 支持 **`modelValue`** 与 **`visible`** 作为 **受控显隐**（**`v-model:modelValue` / `v-model:visible`**）。

**关闭**（含 **取消、点遮罩/关闭图标、确定成功自动关** 等所有将弹层收起的路径）时，组件 MUST **`emit('update:modelValue', false)`** 且 **`emit('update:visible', false)`**。

**主值规则**：内部以 **`modelValue`** 作为显隐 **主绑定**；若 **`modelValue` 与 `visible` 在同一渲染周期均为显式传入** 且 **布尔值不一致**，在 **开发环境** MUST **`console.warn`**（**同一打开周期至多一次** 可接受），并 **以 `modelValue` 为准** 驱动显隐。

#### Scenario: 关闭双 emit

- **WHEN** 用户触发 **关闭**
- **THEN** 父级若同时监听 **`update:modelValue`** 与 **`update:visible`**，MUST **均收到 `false`**

### Requirement: `footer` 与插槽

当 **`footer=true`**（或 design 约定的默认开启策略）且 **未**使用 **`footer` 具名插槽** 覆盖时，组件 MUST 渲染 **默认 footer**：**左侧 `extra` 插槽**（可空）、**右侧 取消 + 确定**。

当调用方提供 **`footer` 具名插槽** 时，组件 MUST **仅渲染该插槽内容** 作为底部区域，**不**再渲染默认 **取消/确定**（除非插槽内自行放置）。

#### Scenario: extra 在左

- **WHEN** **`footer=true`**、无 **`footer` 插槽**、且 **`extra` 插槽** 有内容
- **THEN** 默认 footer 布局 MUST **将 `extra` 置于左侧**、**将取消/确定置于右侧**

### Requirement: `onConfirm` 与确定钮 loading

若 **`onConfirm`** 为函数，用户点击 **确定** 时组件 MUST **调用 `onConfirm()`**（若返回 **Promise** 则 **await**）。

确定钮 **`loading`** 的有效值 MUST 为：**若 `confirmLoading` prop 不为 `undefined`**，则 **以 `confirmLoading` 为准**；**否则** 以组件内部 **`internalConfirmLoading`** 为准（**`onConfirm` pending** 期间为 **`true`**，结束后为 **`false`**）。

**`onConfirm`** 调用 **成功 resolve** 后，组件 MUST **关闭**（触发与 **「显隐与双 v-model」** 相同的 **双 emit false**）。**`onConfirm`** **reject** 或 **throw** 时，组件 MUST **不关闭**；**MUST NOT** 强制 **`ElMessage`**（错误提示由业务在 **`onConfirm`** 或 **`catch`** 中处理）。

若 **未**传入 **`onConfirm`**，用户点击 **确定** 时组件 MUST **`emit('confirm')`**，且 MUST **`emit('submit')`**（**同一动作、同一 tick 内依次 emit**，以满足「兼容 **submit**」）。

#### Scenario: 外部 confirmLoading 覆盖内部

- **WHEN** **`onConfirm`** 为 **延迟 resolve 的 Promise** 且 **`confirmLoading` 显式为 `false`**
- **THEN** 确定钮 **MUST** 不表现 **loading**（以外部为准），直至 **`confirmLoading` 变为 `undefined`** 或 **`true`** 按规则变化

#### Scenario: resolve 后关闭

- **WHEN** **`onConfirm`** **resolve**
- **THEN** **`update:modelValue`** 与 **`update:visible`** MUST **均为 `false`**

### Requirement: 无 `onConfirm` 时的确定

当 **`onConfirm`** **未**提供时，用户点击 **确定** MUST **不**走内部 **`onConfirm` loading** 逻辑；MUST **`emit('confirm')`** 并 **`emit('submit')`**（与上条 **依次 emit** 一致）。

#### Scenario: 仅事件

- **WHEN** 无 **`onConfirm`** 且用户点击 **确定**
- **THEN** MUST **`emit('confirm')`**（且 **submit** 兼容策略满足 **spec**）

### Requirement: 取消与关闭事件

用户点击 **取消**（或等价「放弃」操作）时，组件 MUST **关闭**（**双 emit false**），且 MUST **`emit('cancel')`**。

#### Scenario: cancel 关闭

- **WHEN** 用户点击 **取消**
- **THEN** **`emit('cancel')`** MUST 发生，且 **双 v-model** MUST 为 **`false`**

### Requirement: 透传与冲突（调用方优先）

对 **`ElDialog` / `ElDrawer`** 的 **属性与监听器**（含 **`modalProps`** 等 **design** 列出的入口）MUST 支持透传。当 **透传字段** 与 **组件内部** 为同一壳层设置的 **默认值** 冲突时，MUST **以调用方传入为准**。

#### Scenario: modalProps 覆盖 width

- **WHEN** 调用方 **`modalProps`** 含 **`width: '90%'`** 且与内部默认 **width** 不同
- **THEN** 呈现 **MUST** 为 **`90%`**（透传胜）

### Requirement: Element Plus 生命周期事件转发

组件 MUST **转发**（或等价 **透传监听**）当前 **EP** 版本下 **`ElDialog` / `ElDrawer`** 文档中的 **打开/关闭** 相关事件（至少包括 **`open`、`opened`、`close`、`closed`**，若 **Drawer** 与 **Dialog** 事件名在版本中有差异，以实现所用 **EP 类型定义** 为准），以便业务继续使用 **`@close`** 等习惯。

#### Scenario: close 可监听

- **WHEN** 父模板 **`@close="handler"`**
- **THEN** 在 **EP 触发 close** 时 **`handler`** MUST 被调用

### Requirement: 卸载时同步父状态

当组件 **卸载** 时，若弹层 **仍为打开态**，组件 MUST **`emit('update:modelValue', false)`** 与 **`emit('update:visible', false)`**（与正常关闭一致），**不**要求额外调用底层 **实例** 的 **关闭 API**。

#### Scenario: 路由切走

- **WHEN** **`modelValue` 为 `true`** 且包含 **`C7Dialog`** 的视图被销毁
- **THEN** 父级若仍存活，其 **`modelValue` / `visible`** MUST 能被 **双 emit** 同步到 **`false`**（在卸载前可观测）

### Requirement: Props 与文档化关键项

组件 MUST 支持（命名与 **EP** 对齐处见 **JSDoc**）：**`modelValue`、`visible`、`title`、`mode`、`footer`、`confirmText`、`cancelText`、`confirmLoading`、`onConfirm`**；以及 **宽度/尺寸**（如 **`width`、drawer 用 `size`** 等，与 **原始需求** 一致）；**`modalProps`**（或 **design** 命名的 **dialog/drawer 透传对象**）。

#### Scenario: 验收与原始需求一致

- **WHEN** **`mode`** 在 **dialog/drawer** 间切换且使用 **默认 footer** 与 **`onConfirm`**
- **THEN** **确定钮** MUST 在 **`confirmLoading` 未覆盖时** 于 **pending** 显示 **loading**，且 **resolve** 后 **弹层关闭**
