## Why

表单与列表筛选中大量使用 **`ElSelect`**，但「静态 options / 挂载后异步拉取 / 远程关键字搜索」与「多选时 v-model 要数组还是逗号分隔字符串」在各页面重复实现，行为不一致、易出错。原始说明见 `原始需求/前端/C7下拉选择.md`。

## What Changes

- 在 **quick-ui** 提供 **`C7Select`**：基于 **Element Plus `ElSelect`**，统一 **数据来源**（`dataList` / `options` 别名、**`fetchData`**、**`resultKey` / `dataFormatter`**）、**`autoLoad`** 与 **`remote`** 下的请求时序，以及 **多选 + `separator`** 的内外格式转换。
- **`remote=true`**：**首次聚焦**触发一次 **不带 `query`** 的全量加载；输入关键字时 **`fetchData({ ...fetchParams, query })`**；**`reloadOnClear`** 控制清空后是否再次加载（见 design / spec）。
- **多选**：内部始终以数组驱动 **`ElSelect`**；**`separator=true`** 时对外 **`v-model` / `change`** 为 **逗号分隔字符串**，否则为 **数组**；外部逗号字符串 **解析为数组** 再传入 **`ElSelect`**；**与当前 options 对不齐的 value 仍保留**（不以静默清空方式「修正」）。
- **插槽**：**`prefix` / `label` / `option` / `empty`** 作为 **`ElSelect` 具名插槽透传**。
- **事件**：**`update:modelValue`**、**`change(valueOrString)`**、**`visible-change(open)`**、**`loading-change(loading)`**。
- **对外**：**`defineExpose`** 暴露 **`loading`** 与 **`reload()`**；在 **`packages/index.js`** 中随 **`installPackages`** 全局注册 **`C7Select`**。

## Capabilities

### New Capabilities

- **`ui-c7-select`**：**`C7Select`** 的数据来源与 **`remote`** 语义、**`fetchData` / `fetchParams`** 与 **`query`** 的合并规则、**`resultKey` / `dataFormatter`** 与 **`response.data`** 的解析约定、**多选 + `separator`** 与 **value 保留策略**、插槽透传、事件与 **`reload()`** 的验收标准。

### Modified Capabilities

- （无）新增前端 packages 能力；不修改后端规格。

## Impact

- **代码**：新增 `quick-ui/src/packages/C7Select/index.vue`；修改 `quick-ui/src/packages/index.js`（导出 + 全局注册）。
- **文档**：本变更目录下 `proposal` / `design` / `tasks` / `specs/ui-c7-select/spec.md`；实现阶段可增补 Dev 演示页或测试清单（见 tasks，非强制与 C7Button 同页）。
- **依赖**：以现有 **`element-plus`** 为准；**不**要求与 **`C7Button`** 流水线风格对齐（已确认）。
