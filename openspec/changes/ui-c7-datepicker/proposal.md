## Why

列表与表单中日期/时间字段需在 **`ElDatePicker`** 上重复约定 **`format` / `value-format`**，范围场景还需在 **逗号字符串** 与 **数组** 间手写转换。原始说明见 **`原始需求/前端/C7日期选择器.md`**；需在 **quick-ui** 内提供统一的 **`C7DatePicker`**。

## What Changes

- 新增 **`C7DatePicker`**：基于 **`ElDatePicker`**；**`type` → 默认 `format` / `valueFormat`** 映射（调用方显式传入则覆盖）；范围 **`rangeMerge`**（默认 **`false`**）与 **`mergeDelimiter`**（默认 **`','`**，**与 EP `rangeSeparator` 分离**）；**`update:modelValue`**、**`change` / `blur` / `focus`** 透传；非法合并串 **清空 + `console.warn`**。
- **集成**：在 **`quick-ui/src/packages/index.js`** 中导出并 **`installPackages`** 全局注册 **`C7DatePicker`**。

## Capabilities

### New Capabilities

- **`ui-c7-datepicker`**：**`C7DatePicker`** 的 props、映射表范围、范围合并/拆分、脏数据行为、事件与验收（对齐 **`原始需求/前端/C7日期选择器.md`** 与 **`docs/superpowers/specs/2026-05-07-c7-datepicker-design.md`**）。

### Modified Capabilities

- （无）新增前端 packages 能力。

## Impact

- **代码**：新增 **`quick-ui/src/packages/C7DatePicker/`**；修改 **`quick-ui/src/packages/index.js`**。
- **文档**：本变更目录下 **proposal / design / tasks** 与 **`specs/ui-c7-datepicker/spec.md`**；**`docs/superpowers/specs/2026-05-07-c7-datepicker-design.md`**；实现阶段可补 **VitePress** 与 Dev 页。
