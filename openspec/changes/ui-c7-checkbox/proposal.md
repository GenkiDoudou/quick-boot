## Why

表单与筛选中大量使用 **`ElCheckboxGroup`**，但「静态/异步字典、全选/半选、最小/最大选中数、数组与逗号字符串对外形态」在各页面重复实现，行为不一致且易与 **`C7Select`** 的异步约定脱节。原始说明见 `原始需求/前端/C7多选框.md`。经探索已确认：**方框勾选**、**对外值一律 string**、**与 C7Select 共用一套数据来源与解析链**、**在无法满足「全选合法」时全选按钮不可用**。

## What Changes

- 在 **quick-ui** 提供 **`C7Checkbox`**（名称与原始需求 **C7多选框 / C7Checkbox** 一致）：基于 **Element Plus `ElCheckboxGroup` + `ElCheckbox`**，统一 **选项来源**（**`dataList` 优先**，否则 **`fetchData(fetchParams)`**）、**`resultKey` / `dataFormatter`**、**`labelKey` / `valueKey`**，与 **`C7Select`** 的 **HTTP → `response.data` → `resultKey` → `dataFormatter`** 解析链对齐。
- **值形态**：内部始终以 **`string[]`** 维护；**`joinValue=true`** 时对外 **`v-model` / `change`** 为 **逗号分隔字符串**（元素均为 **string**）；否则为 **`string[]`**；外部逗号字符串 **解析为 string[]** 回显。**不**对外输出 number 等非 string 标量类型。
- **全选（可选）**：**`showSelectAll=true`** 时展示「全选」行，支持半选态；**`indeterminate` 作为 deprecated 别名** 与 **`showSelectAll`** 同义（便于从旧文档迁移）。当 **可选中且未禁用的选项数量大于 `max`**（或等价地无法通过一次「全选」满足 **`max` 约束**）时，**「全选」控件 MUST 为禁用态**（不采用「选满 max 即停」的静默截断）。
- **约束**：**`min` / `max`** 与 **`disabled`** 作用于可选项/交互；**`checkboxStyle`**：**`default` / `button` / `border`**（兼容旧 **`button`** 单独 prop 的口径在 design 中写清）。
- **事件**：**`update:modelValue`**、**`change(selected: string[])`**（载荷为 **内部选中数组**，与原始需求中「选中数组」一致，便于不依赖 `joinValue` 形态做业务判断；若需与 `v-model` 完全一致可在 design 明确——当前推荐 **`change` 始终 `string[]`**）。
- **导出与注册**：**`packages/index.js`** 全局注册 **`C7Checkbox`**；**`defineExpose`** 暴露 **`loading`**、**`reload()`**（与 **`C7Select`** 可观测性对齐）。

## Capabilities

### New Capabilities

- **`ui-c7-checkbox`**：**`C7Checkbox`** 的选项来源与 **`fetchData` / `fetchParams`** 合并规则、**`resultKey` / `dataFormatter`** 与 **`response.data`** 解析约定、**`joinValue` + 可选分隔符**、**`showSelectAll` / 半选 / 全选禁用条件**、**`min`/`max`/`disabled`**、样式与事件、**`reload()`** 的验收标准。

### Modified Capabilities

- （无）新增前端 packages 能力；不修改已有 **`openspec/specs`** 中其他 capability 的 REQUIREMENTS 文本（与 **`C7Select`** 为行为对齐而非改 **`ui-c7-select`** 规格）。

## Impact

- **代码**：新增 **`quick-ui/src/packages/C7Checkbox`**（至少 **`index.vue`**）；修改 **`quick-ui/src/packages/index.js`**（导出 + 全局注册）；可选新增 Dev 演示路由/页面（见 **tasks**）。
- **文档**：本变更目录下 **proposal / design / tasks** 与 **`specs/ui-c7-checkbox/spec.md`**。
- **依赖**：以现有 **`element-plus`** 为准；**不**新增 npm 依赖（除非实现阶段发现缺口并经评审）。
