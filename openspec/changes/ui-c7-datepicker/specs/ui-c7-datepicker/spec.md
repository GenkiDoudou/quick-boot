# ui-c7-datepicker

## Purpose

为 **quick-ui** 提供 **`C7DatePicker`**：在 **`ElDatePicker`** 上统一 **`type` → 默认 `format` / `valueFormat`**（调用方显式传入则 **覆盖**）、范围 **`rangeMerge`（默认 `false`）** 与 **`mergeDelimiter`（默认 `','`，与 EP `rangeSeparator` 分离）**、**合并串拆分回显**与 **非法串 `console.warn` + 清空**；透传 **`update:modelValue`**、**`change` / `blur` / `focus`**。需求来源：**`原始需求/前端/C7日期选择器.md`**；细化见 **`docs/superpowers/specs/2026-05-07-c7-datepicker-design.md`**。

## ADDED Requirements

### Requirement: 组件与注册位置

**`C7DatePicker`** MUST 位于 **`quick-ui/src/packages/C7DatePicker`**（至少 **`index.vue`**），并通过 **`quick-ui/src/packages/index.js`** 的 **`installPackages(app)`** 注册为全局组件 **`C7DatePicker`**；亦 MAY **`import { C7DatePicker } from '@/packages'`** 按需使用。

#### Scenario: 全局注册可用

- **WHEN** 应用已执行 **`installPackages(app)`**
- **THEN** 模板中 MUST 能使用 **`<C7DatePicker />`** 而无需逐页 **import** 注册

### Requirement: 根节点与透传

组件根节点 MUST 为 **`ElDatePicker`**，且 MUST **`defineOptions({ name: 'C7DatePicker', inheritAttrs: false })`**，在根上使用 **`v-bind="$attrs"`**。除下文 **显式 props** 外，其余属性与 **Element Plus `el-date-picker`** 一致。

#### Scenario: 透传 EP 合法属性

- **WHEN** 父组件传入 **`disabled`**、**`placeholder`**、**`rangeSeparator`** 等 EP 支持属性
- **THEN** 这些属性 MUST 作用于内部 **`ElDatePicker`**（不被 **`inheritAttrs`** 误吞）

### Requirement: 显式 props — `rangeMerge` 与 `mergeDelimiter`

- **`rangeMerge`**：`boolean`，默认 **`false`**。在 **范围类 `type`**（如 **`daterange`**、**`datetimerange`** 等，**MUST** 在 JSDoc 列出）下：为 **`true`** 时对外 **`modelValue`** 为 **单字符串**（两段格式化值以 **`mergeDelimiter`** 连接）；为 **`false`** 时对外与 **EP** 范围 **`modelValue`** 一致（一般为 **二元数组**）。在非范围 **`type`** 下 **`rangeMerge` MUST 无效果**（JSDoc 说明）。
- **`mergeDelimiter`**：`string`，默认 **`','`**。仅用于 **存储合并串** 的拼接与拆分；**MUST NOT** 与 EP **`rangeSeparator`（UI 文案）** 混为同一语义。若传入空字符串，实现 **MUST** 在 JSDoc 声明 **回退 `','`** 或 **拒绝更新** 之一并贯彻。

#### Scenario: 默认对外为数组

- **WHEN** **`type='daterange'`** 且 **未**传 **`rangeMerge`**（或 **`rangeMerge=false`**）
- **THEN** 选择范围后，对外 **`modelValue`** MUST 为 **EP 原生范围形态**（一般为 **数组**），**不得**默认变为单字符串

#### Scenario: 合并为单串

- **WHEN** **`type='daterange'`**、**`rangeMerge=true`**、**`mergeDelimiter='|'`**
- **THEN** 选择 **2024-01-01** 与 **2024-12-31** 后，对外 **`modelValue`** MUST 为 **`2024-01-01|2024-12-31`**（在 **`valueFormat`** 为 **`YYYY-MM-DD`** 的前提下）

### Requirement: 默认 `format` / `valueFormat` 映射

当调用方 **未** 传入 **`format`**（或 **`value-format`**）与 **`valueFormat`** 时，组件对下列 **`type`** MUST 注入 **设计说明 §3** 中的默认值（**`datetime` / `datetimerange`** 的 **`valueFormat`** MUST 含 **`HH:mm:ss`**）：

**`date`**、**`daterange`**、**`datetime`**、**`datetimerange`**、**`month`**、**`monthrange`**、**`year`**、**`yearrange`**、**`week`**

**`week`** 的具体展示串与绑定串 **MUST** 与当前工程 **Element Plus** 版本文档对齐，并在组件 JSDoc **写死**实际格式字符串。

#### Scenario: 显式传入覆盖映射

- **WHEN** **`type='date'`** 且父传入 **`value-format="YYYY/MM/DD"`**
- **THEN** 内部 **`ElDatePicker`** 的 **`value-format`** MUST 为 **`YYYY/MM/DD`**，**不得**再强制 **`YYYY-MM-DD`**

#### Scenario: 未映射 type 不注入

- **WHEN** **`type`** 不在上表内（且 EP 仍支持该 **`type`**）
- **THEN** 组件 **MUST NOT** 自动注入 **`format`/`valueFormat`**（除非父已传入）

### Requirement: 合并串拆分回显（验收）

在 **范围类 `type`** 下，当父传入 **单字符串** 且包含 **`mergeDelimiter`** 作为两段分隔符时，组件 MUST 拆分为 **EP 可编辑的范围值**。

#### Scenario: daterange 与逗号串

- **WHEN** **`type='daterange'`**、**`rangeMerge=false`**、**`mergeDelimiter=','`**（默认），父 **`modelValue`** 为 **`'2024-01-01,2024-12-31'`**
- **THEN** 面板 MUST 正确回显该范围且用户可继续修改；当 **`rangeMerge=true`** 时，在用户选择后对外输出 MUST 再次为 **`mergeDelimiter`** 拼接的 **单字符串**（与设计说明一致）

### Requirement: 非法合并串

当拆分结果 **非法**（无法解析为合法范围、缺一段、多于两段等）时：对外 **`modelValue`** MUST 归一为 **空范围语义**（**`null`** 或 EP 接受的等价清空形态，**JSDoc 写死**）；且 **MUST `console.warn`**；**MUST NOT** 依赖未文档化的 **`emit('error')`** 作为唯一手段。

#### Scenario: 非法串警告并清空

- **WHEN** 父传入 **`'not-a-date,'`** 或 **`'a,b,c'`** 等非法形态
- **THEN** 对外值为空范围且控制台 MUST 出现 **warn**（实现可含摘要）

### Requirement: 事件

组件 MUST 声明并对齐 **`update:modelValue`**、**`change`**、**`blur`**、**`focus`**。**`change`** 载荷形态 MUST 与 **归一化后的对外 `modelValue` 语义**一致（JSDoc 写明与 EP **`change`** 在清空等场景的对齐规则）。

#### Scenario: change 与对外值一致

- **WHEN** **`rangeMerge=true`** 且用户完成一次有效范围选择
- **THEN** **`change`** 回调获得的值 MUST 与随后 **`update:modelValue`** 对外值 **同形态**（单字符串）

## 验收映射（原始需求）

以下 **MUST** 成立，并与 **`原始需求/前端/C7日期选择器.md`** 一致：

- **`type`** 支持常见类型；**`format`/`valueFormat` 未传**时按映射注入；未映射 **不强行注入**。
- **范围**：**`rangeMerge=true`** 对外 **单字符串**；否则 **数组**；存储分隔符为 **`mergeDelimiter`**，**非** EP **`rangeSeparator`** 兼用。
- **事件**：**`update:modelValue`**、**`change` / `blur` / `focus`**。
- **验收**：**daterange** 下 **`"a,b"`** 可回显为可编辑范围，输出随 **`rangeMerge`** 合并或拆分。
