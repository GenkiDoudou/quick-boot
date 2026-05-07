## Why

业务表单与筛选中大量使用 **`ElTimePicker`**，但 **`format` / `value-format`** 默认值不统一；范围模式下项目常需用 **单字符串**（如 **`"08:00:00,18:00:00"`**）落库，又需可选 **数组** 形态与 Element Plus 原生一致。与 **`C7DatePicker`** 缺少对称封装时，合并串拆拼、分隔符与面板 **`range-separator`** 混用等问题会在各页面重复出现。原始说明见 **`原始需求/前端/C7时间选择器.md`**；详细设计见 **`docs/superpowers/specs/2026-05-07-c7-time-picker-design.md`**。

## What Changes

- 在 **quick-ui** 新增 **`C7TimePicker`**：基于 **`ElTimePicker`**，**未传 `format` / `valueFormat`（含 `value-format`）** 时注入默认 **`HH:mm:ss`**。
- **`is-range` / `isRange`** 为真时支持 **`rangeMerge`**：**`true`** 时 **`v-model`** 对外为 **单字符串**（两段由 **`mergeDelimiter`** 拼接，默认 **逗号**，与 EP **`range-separator`** 语义分离）；**`false`** 时对外为 **EP 原生范围值**（一般为二元数组）。
- **`outerToInner` / `innerToOuter`**：单数据流、**`mergeDelimiter` 回退**、**`innerToOuter` 范围合并** 等与 **`C7DatePicker`** 同构；**范围 + `rangeMerge=false` 时外向 `string` 不拆分** 与 **`C7DatePicker`（范围 `type` 下外向 `string` 始终尝试拆分）** **有意不同**，见 **`docs/superpowers/specs/2026-05-07-c7-time-picker-design.md`** 第 8 节与第 14 节。非法合并串、非法数组等：**内向 `null` + `console.warn`（`[C7TimePicker]` 前缀 + 去重）**。
- **事件**：**`update:modelValue`**、**`change`**（载荷与 **`update:modelValue`** 外向形态一致）、**`blur` / `focus`**；其它 EP 事件通过 **attrs / 模板监听** 即可，不要求在 **`defineEmits`** 中穷举。
- **导出与注册**：在 **`quick-ui/src/packages/index.js`** 中 **`export` 与 `app.component('C7TimePicker', …)`**。

**BREAKING**：无（新增组件）。

## Capabilities

### New Capabilities

- **`ui-c7-time-picker`**：**`C7TimePicker`** 的默认格式注入条件、**`rangeMerge` / `mergeDelimiter`** 与范围判定、**`outerToInner` / `innerToOuter`** 与非法值处理、事件载荷形态；验收与 **`docs/superpowers/specs/2026-05-07-c7-time-picker-design.md`** 一致，并 **显式记录** 与 **`C7DatePicker`** 在 **范围外向 `string` 解析** 上的差异。

### Modified Capabilities

- （无）不修改 **`openspec/specs`** 下已有 capability 的 REQUIREMENTS 文本。

## Impact

- **代码**：新增 **`quick-ui/src/packages/C7TimePicker/index.vue`**；修改 **`quick-ui/src/packages/index.js`**。
- **文档**：本变更目录下 **proposal / design / tasks** 与 **`specs/ui-c7-time-picker/spec.md`**；实现完成后可按仓库惯例补充 VitePress 组件文档（非本 proposal 必选阻塞项）。
- **依赖**：以现有 **element-plus** 为准；不新增 npm 依赖（除非实现阶段发现缺口并经评审）。
