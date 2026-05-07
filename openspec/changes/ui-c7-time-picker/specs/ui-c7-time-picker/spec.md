# ui-c7-time-picker

## Purpose

为 **quick-ui** 提供 **`C7TimePicker`**：在 **`ElTimePicker`** 上统一 **默认 `HH:mm:ss`**，并在 **范围模式** 下支持 **`rangeMerge` + `mergeDelimiter`** 的合并字符串与 **EP 原生数组** 双向形态，**`change`** 与 **`update:modelValue`** 外向形态一致。需求来源：**`原始需求/前端/C7时间选择器.md`**；设计对齐：**`docs/superpowers/specs/2026-05-07-c7-time-picker-design.md`**。与 **`C7DatePicker`**：**单数据流、`mergeDelimiter` 语义、`innerToOuter` 合并策略** 等对齐；**范围 + `rangeMerge=false` 时是否解析外向 `string`** 与 **`C7DatePicker` 实现** **不同**（见 **`outerToInner`** Requirement 与已定稿设计第 8 节）。

## ADDED Requirements

### Requirement: 组件位置与全局注册

**`C7TimePicker`** MUST 实现于 **`quick-ui/src/packages/C7TimePicker/index.vue`**（或同目录下经 **`index.vue`** 导出的单文件入口），并 MUST 通过 **`quick-ui/src/packages/index.js`** 完成 **`export`** 与 **`app.component('C7TimePicker', …)`**（或与现有 C7 包一致的 **`installPackages`** 注册方式），使应用可在模板中使用 **`<C7TimePicker />`**。

#### Scenario: 安装后可全局使用

- **WHEN** 应用已执行与现有 C7 组件相同的 **packages 安装/注册**流程
- **THEN** 模板中 MUST 能使用 **`<C7TimePicker />`** 而无需业务侧再次 **`app.component`**

### Requirement: 根节点、inheritAttrs 与独占 props

组件根节点 MUST 为 **`ElTimePicker`**。组件 MUST **`defineOptions({ name: 'C7TimePicker', inheritAttrs: false })`**。组件 MUST **不**将 **`modelValue`、** **`rangeMerge`、** **`mergeDelimiter`** 透传给 **`ElTimePicker`**；**`model-value`** 与 **`@update:model-value`** MUST 由包装层绑定与处理。

#### Scenario: 独占 props 不污染 EP attrs

- **WHEN** 父级传入 **`rangeMerge`** 与 **`mergeDelimiter`**
- **THEN** 渲染出的 **`ElTimePicker`** 上 MUST **不**出现同名 **props** 作为未知属性（避免 EP 告警或无效属性）

### Requirement: 默认 format 与 valueFormat

当父级通过 **attrs** 传入的 **`format`** 为 **`undefined`**，且 **`valueFormat`** 与 **`value-format`** 均为 **`undefined`** 时，组件 MUST 向 **`ElTimePicker`** 注入 **`format: 'HH:mm:ss'`** 与 **`valueFormat: 'HH:mm:ss'`**（占位符与 **Element Plus / dayjs** 约定一致）。

若 **`format`**、**`valueFormat`**、**`value-format`** 中 **任一已定义**（含父级显式传入 **`null`** 以外的值由实现与 Vue 语义对齐并在 JSDoc 说明），组件 MUST **不**覆盖该项，行为与裸 **`ElTimePicker`** 一致。

#### Scenario: 未传格式时使用 HH:mm:ss

- **WHEN** 父级 **未**设置 **`format` / `value-format` / `valueFormat`**
- **THEN** 内部绑定到 **`ElTimePicker`** 的格式相关 props MUST 包含 **`HH:mm:ss`** 默认值

#### Scenario: 已传 format 时不被覆盖

- **WHEN** 父级设置 **`format` 为 `'HH:mm'`**
- **THEN** 内部传给 **`ElTimePicker`** 的 **`format`** MUST 仍为 **`'HH:mm'`**

### Requirement: 范围模式判定

仅当 **`is-range` 或 `isRange`** 为 **boolean `true`**（或 Vue 布尔绑定等价为 true）时，组件 MUST 将当前模式视为 **范围模式**。否则 MUST 视为 **非范围模式**。

#### Scenario: is-range 为 true 时启用范围逻辑

- **WHEN** **`is-range`** 为 **`true`** 且 **`rangeMerge`** 为 **`true`**
- **THEN** 组件 MUST 对 **`modelValue`** 应用合并串拆分/拼接规则（见后续 Requirement）

### Requirement: rangeMerge 与 mergeDelimiter 默认值

组件 MUST 提供 **`rangeMerge`** prop，类型为 **boolean**，默认值为 **`false`**。组件 MUST 提供 **`mergeDelimiter`** prop，类型为 **string**，默认值为 **`',''`**。

当 **`mergeDelimiter`** 为 **`null`**、**`undefined`** 或 **空字符串** 时，用于拆拼存储串的 **有效分隔符** MUST 回退为 **`','`**。

**`mergeDelimiter`** MUST **仅**用于 **`v-model`** 合并字符串的拼接与拆分；MUST **与** **`ElTimePicker`** 的 **`range-separator`**（面板展示）语义分离。

#### Scenario: 非范围时 merge 无效果

- **WHEN** **非范围模式** 且 **`rangeMerge=true`**
- **THEN** 外向 **`v-model`** 形态 MUST 与 **非范围 **`ElTimePicker`** 原生行为一致**（**不**输出合并时间串）

### Requirement: outerToInner（外向到 EP）

在非范围模式下，**`outerToInner(outer)`** MUST 返回 **`outer`**（不对 **`''`** 做额外归一化）。

在范围模式且 **`rangeMerge` 为 `true`** 时：

- **`outer` 为 `null`、`undefined` 或 `''`**：MUST 返回 **`null`**。
- **`outer` 为 `string`**：若不包含 **有效分隔符**，MUST 返回 **`null`** 并 **`console.warn`**（前缀 **`[C7TimePicker]`**，且 MUST 对重复相同错误做去重以避免刷屏）。若包含分隔符，MUST **`split`** 后 **`trim`** 得到两段；仅当 **恰好两段且均非空** 时 MUST 返回 **`[start, end]`** 数组；否则 MUST 返回 **`null`** 并 **`console.warn`**（去重策略同上）。
- **`outer` 为 `Array`**：仅当 **长度为 2** 时 MUST 原样返回；否则 MUST 返回 **`null`** 并 **`console.warn`**（去重同上）。
- **其它类型**：MUST 返回 **`null`** 并 **`console.warn`**（去重同上）。

在范围模式且 **`rangeMerge` 为 `false`** 时：

- **`outer` 为 `Array`**：仅当 **长度为 2** 时 MUST 原样返回；否则 MUST 返回 **`null`** 并 **`console.warn`**。
- **其它类型**（含 **`string`**）：MUST 返回 **`null`** 并 **`console.warn`**。**MUST NOT** 将 **`string`** 按 **`mergeDelimiter`** 拆成两段回显。**说明**：此行为 **不**等同于 **`C7DatePicker`** 的实现——后者在 **范围 `type`** 下对 **`outer` 为 `string`** **一律**尝试拆分（与 **`rangeMerge` 无关**；**`rangeMerge` 仅影响外向是否合并为单串**）。业务若需从合并时间串回显，**MUST** 使用 **`rangeMerge=true`** 或在父级先拆为 **长度 2 的数组**。

#### Scenario: rangeMerge 下合并串回显

- **WHEN** 范围模式、**`rangeMerge=true`**、**`mergeDelimiter` 为默认逗号**，且 **`modelValue` 为 `'08:00:00,18:00:00'`**
- **THEN** 传给 **`ElTimePicker`** 的 **`model-value`** MUST 为 **二元数组**，且用户可见选择与该两端时间一致（与 **`valueFormat`** 一致）

#### Scenario: 非法合并串清空并警告

- **WHEN** 范围模式、**`rangeMerge=true`**，且 **`modelValue` 为 `'08:00:00'`**（缺少分隔符与第二段）
- **THEN** 传给 **`ElTimePicker`** 的 **`model-value`** MUST 为 **`null`**（或 EP 接受的等价清空态，与实现选定一致并在 JSDoc 固定），且 MUST 产生 **至多一次**（或去重策略下有限次）**`console.warn`**

### Requirement: innerToOuter（EP 到外向）

在非范围模式下，**`innerToOuter(inner)`** MUST 返回 **`inner`**。

在范围模式下，若 **`inner` 为 `null` 或 `undefined`**，MUST 返回 **`null`**。若 **`inner` 非数组** 或 **数组长度不为 2**，MUST 返回 **`null`**（**不**要求额外 **`console.warn`**）。

在范围模式且 **`rangeMerge` 为 `true`** 时，设两段为 **`a`、`b`**：若 **`a` 与 `b` 均为 `null` 或 `undefined`**，MUST 返回 **`null`**；否则 MUST 返回 **`String(a 或空串) + 有效分隔符 + String(b 或空串)`**（**`null` 段以空串参与拼接**，与 **`C7DatePicker`** 一致）。

在范围模式且 **`rangeMerge` 为 `false`** 时，MUST 返回 **`inner`** 数组本身。

#### Scenario: change 与 v-model 形态一致

- **WHEN** 用户在范围 **`rangeMerge=true`** 下修改结束时间
- **THEN** **`update:modelValue`** 与 **`change`** 的载荷 MUST **均为** 合并后的 **单字符串**（或 **`null`** 清空），且二者形态 MUST 一致

### Requirement: 事件

组件 MUST **`emit('update:modelValue', innerToOuter(val))`** 响应 EP 更新。组件 MUST **`emit('change', innerToOuter(val))`**，且 **`change`** 载荷 MUST 与 **`update:modelValue`** 的外向形态 **一致**（含清空为 **`null`** 的语义，与 **`C7DatePicker`** 对齐）。

组件 MUST **`emit('blur', …)`** 与 **`emit('focus', …)`**，参数签名与 **`ElTimePicker`** 一致。

组件 **不** MUST 在 **`defineEmits`** 中声明 **`visible-change`** 等其它 EP 事件；父级 MAY 通过 **模板监听** 或 **attrs** 使用。

#### Scenario: blur 可透传

- **WHEN** 父级在 **`C7TimePicker`** 上监听 **`@blur`**
- **THEN** 失焦时父级 MUST 能收到与 EP 一致的回调参数

### Requirement: JSDoc 与可维护注释

默认导出组件 MUST 具备顶层 **`/** … */` JSDoc**：说明 **`rangeMerge` / `mergeDelimiter`** 与 **`is-range`** 的交互、**`outerToInner` / `innerToOuter`** 边界、**非法值** 与 **warn** 行为，以及 **非 merge 误传合并串** 的文档化后果。

#### Scenario: 新开发者可从注释理解 v-model 形态

- **WHEN** 阅读 **`index.vue`** 顶部 JSDoc
- **THEN** 应能判断 **范围 + `rangeMerge`** 下 **`v-model`** 为 **`string | null`** 而非数组
