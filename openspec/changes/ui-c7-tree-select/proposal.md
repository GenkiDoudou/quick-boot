## Why

部门、分类等层级数据在表单与筛选中常用树形选择，但各页面对 **`ElTreeSelect`** 的 **静态/异步加载、字段映射、多选对外格式（数组 vs 逗号串）、值类型** 重复封装，与 **`C7Select`** 的契约不一致。原始说明见 `原始需求/前端/C7树选择.md`；详细设计见 `docs/superpowers/specs/2026-05-07-c7-tree-select-design.md`；实现任务拆解见 `docs/superpowers/plans/2026-05-07-c7-tree-select.md`。

## What Changes

- 在 **quick-ui** 新增 **`C7TreeSelect`**（基于 **`ElTreeSelect`**）：**`dataList` 优先**于 **`options`**；否则 **`fetchData` + `fetchParams` + `resultKey` + 可选 `dataFormatter`** 拉取**整棵树**；**`autoLoad`** 在无静态绑定时挂载触发首次请求（无 **`query`** 键）。
- **字段映射**：**`labelKey` / `valueKey` / `childrenKey`**（默认 `label`/`value`/`children`），内部 **`mapTree`** 规范为 EP 所需节点形态。
- **多选**：**`separator=true`** 时对外 **`v-model` / `change`** 为**英文逗号分隔字符串**，空为 **`''`**；否则为**数组**；与 **`C7Select`** 的 **`separator`** 语义一致（原始需求中的 **`rangeMerge`** 在文档中更名为 **`separator`**）。
- **`valueType`**：**`auto` | `string` | `number`**；**`auto`** 时以**映射后根列表首节点**的 **`value` 的 `typeof`** 为准（仅 **`number`** 且非 NaN 时按 number，否则 string）。
- **事件**：**`update:modelValue`**、**`change`**、**`load-error`**；与 **`C7Select`** 对齐可选透传 **`visible-change`**、**`loading-change`**。
- **暴露**：**`reload()`**、**`loading`**、**`treeSelectRef`**。
- **透传**：**`filterable`**、**`filter-node-method`** 及未占用 attrs 透传至 **`ElTreeSelect`**。
- **一期不做**：节点 **`lazy`** 懒加载、与 **`C7Select`** 的远程关键字搜索对齐。
- **非 BREAKING**：新增组件与文档；不修改已有业务 API。

## Capabilities

### New Capabilities

- **`ui-c7-tree-select`**：**`C7TreeSelect`** 的数据来源优先级、**`fetchData` / `resultKey` / `dataFormatter` / `autoLoad`**、**`mapTree`** 与 **`separator` / `valueType`**、**`load-error`** 与失败保留树数据、**`reload()`**、attrs 透传与验收标准。

### Modified Capabilities

- （无）不修改 **`openspec/specs`** 下已有 capability 的 REQUIREMENTS；与 **`C7Select`** 为行为对齐，不要求改 **`ui-c7-select`** 主规格文本。

## Impact

- **代码**：新建 **`quick-ui/src/packages/C7TreeSelect/index.vue`**；修改 **`quick-ui/src/packages/index.js`**。
- **文档**：更新 **`原始需求/前端/C7树选择.md`**（**`separator`** 用词）；新建 **`docs/docs/frontend/components/通用组件/c7-tree-select.md`**（侧栏若已有链接则仅补文件）。
- **依赖**：沿用 **`element-plus`**、**`lodash/get`**；不新增 npm 依赖（除非实现阶段发现缺口并经评审）。
