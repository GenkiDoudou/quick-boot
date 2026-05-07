# C7TreeSelect 树形选择器

在 **`ElTreeSelect`** 上封装 **静态/异步整树**、**字段映射**、**多选 `separator` 逗号串** 与 **`valueType`**，契约对齐 **`C7Select`**（`dataList`/`options`、`fetchData`、`resultKey`、`dataFormatter`、`autoLoad`）。

**源码**：`quick-ui/src/packages/C7TreeSelect/index.vue`

## 功能概要

- **数据**：`dataList` 优先于 `options`；无静态绑定时 `fetchData` + `resultKey`（可选 `dataFormatter`）拉取整树。
- **映射**：`labelKey` / `valueKey` / `childrenKey`（默认 `label`/`value`/`children`），内部规范为 EP 树节点。
- **多选**：`multiple`；`separator` 为 true 时对外为逗号字符串，空为 `''`。
- **`valueType`**：`auto` | `string` | `number`；`auto` 由**映射后根列表首节点**的 `value` 类型推断。
- **透传**：`filterable`、`filter-node-method` 等未占用键通过 attrs 透传至 `ElTreeSelect`。
- **事件**：`update:modelValue`、`change`、`load-error`、`visible-change`、`loading-change`。
- **方法**：`reload()`（静态时重新同步内外值；异步时重新 `fetchData`）。

## 与全局注册

`main.js` 调用 `installPackages(app)` 后，可使用 `<c7-tree-select />` / `<C7TreeSelect />`。

## 限制

- 若多选 value 本身含英文逗号，**勿**使用 `separator` 模式（与 `C7Select` 相同）。
- 一期不支持节点 `lazy` 远程懒加载（见设计说明）。

## 相关规格

- 设计说明（仓库内）：`docs/superpowers/specs/2026-05-07-c7-tree-select-design.md`
- OpenSpec 变更：`openspec/changes/ui-c7-tree-select/specs/ui-c7-tree-select/spec.md`
