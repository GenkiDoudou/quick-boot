## Why

树形数据（地区、部门、分类等）在表单与筛选中需要 **级联选择**；若直接使用 **`ElCascader`**，整树加载、按层懒加载、响应解析与 **`C7Select` / `C7TreeSelect`** 已统一的 **`separator` / `valueType`** 等仍会在各页面重复实现，行为易分叉。定稿设计见 **`docs/superpowers/specs/2026-05-08-c7-cascader-design.md`**；原始说明见 **`原始需求/前端/C7级联选择器.md`**。

## What Changes

- 在 **quick-ui** 提供 **`C7Cascader`**：基于 **`ElCascader`**，统一 **静态 `dataList` / `options`（`dataList` 优先）**、**整树 `fetchData({ ...fetchParams })`**、**懒加载 `fetchData({ parentId, ...fetchParams })`**（根层 **`parentId === rootParentId`**）、**`resultKey` / `dataFormatter`**、**`labelKey` / `valueKey` / `childrenKey`** 映射。
- **懒加载**：子接口返回 **当前层扁平子节点列表**；由组件挂到父节点 **`children`**；**`load-error` / `loading-change`** 与 **`C7TreeSelect`** 对齐；**`fetchGeneration`** 等忽略过期写入。
- **多选 + `separator`**：与 C7 系列一致；当 **`emitPath === true`** 或值为 **非一维标量列表** 时 **`separator` 无效**，对外保持 EP 数组形态，**DEV** **`console.warn`**（见 design / spec）。
- **`valueType`**（`auto` / `string` / `number`）：语义对齐 **`C7TreeSelect`**；**不**改变 **`emitPath` / `checkStrictly`** 下 EP 的值结构语义。
- **事件**：**`update:modelValue`**、**`change`**、**`visible-change`**、**`loading-change`**、**`load-error`**；**`inheritAttrs: false`**，保留字外透传 EP。
- **注册**：在 **`quick-ui/src/packages/index.js`** 导出并 **`installPackages`** 注册 **`C7Cascader`**。
- **文档**：VitePress **`c7-cascader.md`**（路径与现有通用组件文档一致）；可选 Dev 演示页（见 tasks）。

## Capabilities

### New Capabilities

- **`ui-c7-cascader`**：**`C7Cascader`** 的数据来源（静态 / 整树异步 / 懒加载）、**`fetchData` 入参约定**、**`resultKey` / `dataFormatter` 解析链**、**字段映射**、**`separator` 与 `emitPath` 边界**、**`valueType`**、事件与 **`autoLoad` / `rootParentId`** 的验收标准。

### Modified Capabilities

- （无）新增前端 packages 能力；不修改既有主规格中的其他能力条目。

## Impact

- **代码**：新增 **`quick-ui/src/packages/C7Cascader/index.vue`**；修改 **`quick-ui/src/packages/index.js`**。
- **文档**：本变更目录下 **`proposal` / `design` / `tasks` / `specs/ui-c7-cascader/spec.md`**；实现阶段增补 **`docs/docs/frontend/components/通用组件/c7-cascader.md`** 及侧栏（若项目要求）。
- **依赖**：以现有 **`element-plus`**、**`lodash/get`**（与树选一致）为准；**不**新增 npm 包（除非实现时发现 EP API 强制缺口，再于 tasks 中说明）。
