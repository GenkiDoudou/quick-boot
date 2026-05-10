# C7Cascader 级联选择器

在 **`ElCascader`** 上封装 **静态 / 整树异步 / 懒加载**、**`fetchData` + `resultKey` + `dataFormatter`**、**`labelKey` / `valueKey` / `childrenKey`** 映射，以及 **`multiple` + `separator`**、**`valueType`**、**`load-error` / `loading-change`**，契约对齐 **`C7TreeSelect`** / **`C7Select`**。

**源码**：`quick-ui/src/packages/C7Cascader/index.vue`

## 功能概要

- **数据**
  - **静态**：`dataList` 优先于 `options`（与树选一致）。
  - **整树异步**：无静态、**未启用懒加载**、**`autoLoad=true`** 时，挂载后 **`fetchData({ ...fetchParams })`**（无 `parentId`）。
  - **懒加载**：**`lazy=true`** 且提供 **`fetchData`** 时，通过 EP **`props.lazy` + `props.lazyLoad`** 按层请求 **`fetchData({ parentId, ...fetchParams })`**；根层 **`parentId === rootParentId`**（默认 **`null`**），子层为父节点映射后的 **`value`**；接口返回 **当前层扁平子列表**。
- **解析**：自 **`response.data`** 起，**`resultKey`**（lodash 点路径）取数组，再经 **`dataFormatter`**；**`resultKey` 不表示子节点字段名**，子节点字段为 **`childrenKey`**。
- **多选 + `separator`**：仅当 **`emit-path="false"`** 且多选值为 **一维标量** 时，对外为 **英文逗号串**（空为 **`''`**）。**Element Plus 默认 `emitPath` 为 `true`**，此时 **`separator` 无效**，开发环境会 **`console.warn`**。
- **`valueType`**：`auto` | `string` | `number`；**`emitPath=true`** 时对路径上 **各层标量** 做类型转换。
- **透传**：未占用键通过 attrs 透传至 **`ElCascader`**；**`props`**（级联面板配置）会与内部的 **`lazy` / `lazyLoad`** 浅合并（启用 C7 懒加载时由组件注入 **`lazyLoad`**）。
- **事件**：`update:modelValue`、`change`、`visible-change`、`load-error`、`loading-change`。
- **方法**：`reload()`（静态时重同步内外值；整树异步重新请求；懒加载通过 **内部 key** 重置面板）。

## 与 C7TreeSelect 的选型（建议）

- **级联多步、层级深、每步选项较少**：优先 **C7Cascader**。
- **树内搜索、勾选、复杂节点展示**：优先 **C7TreeSelect**。

## 与全局注册

`main.js` 调用 `installPackages(app)` 后，可使用 `<c7-cascader />` / `<C7Cascader />`。

## Dev 演示

路由：`/dev/c7-cascader-e2e`（`quick-ui/src/views/dev/C7CascaderE2E.vue`）。

## 限制

- 多选 value 含英文逗号时，**勿**使用 **`separator`**。
- **`emitPath=true`**（含默认）或值为 **路径嵌套数组** 时，**勿**依赖 **`separator`** 逗号串。

## 相关规格

- 设计说明：`docs/superpowers/specs/2026-05-08-c7-cascader-design.md`
- OpenSpec：`openspec/changes/ui-c7-cascader/specs/ui-c7-cascader/spec.md`
