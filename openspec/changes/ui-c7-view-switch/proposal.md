## Why

部分业务页需在多个「视图 / 表单步骤」间切换（如列表 → 新增 → 编辑 → 详情），若各页自行拼返回与标题区，行为与动画易分裂；需要**统一容器**管理 `v-model` 视图名、**历史栈返回**与可选 **页头**。依据已定稿 `docs/superpowers/specs/2026-05-08-c7-view-switch-design.md` 与 `原始需求/前端/C7视图切换容器.md`。

## What Changes

- 在 **quick-ui** 新增 **`C7ViewSwitch`**（`quick-ui/src/packages/C7ViewSwitch/index.vue`）：基于 **`views` / `showIndexs`** 与 **`modelValue`** 渲染**具名插槽**；内部维护**仅由 `switchTo` / `goBack` 修改**的历史栈；暴露 **`switchTo`、`goBack`、`currentConfig`、`viewHistory`**。
- **事件**：`update:modelValue`、`change`、`back`、`not-found`、`back-empty`（脚本侧 camelCase 与模板 kebab-case 对齐 Vue 约定）。
- **可选 `ElPageHeader`**、**`transition`（`boolean | string`）**、**`defaultView`**、**空栈回落（`closeIndex` / `defaultView`）**；未匹配视图 **`#empty`** 兜底。
- 在 **`packages/index.js`** 中 **export** 与 **`installPackages`** 注册 **`C7ViewSwitch`**。
- **VitePress**：补充与其它 C7 一致的组件说明页（含「父级直接改 `v-model` 与栈」说明）。

## Capabilities

### New Capabilities

- **`ui-c7-view-switch`**：**`C7ViewSwitch`** 的视图配置与别名优先级、**栈与 `v-model` 语义**（含父级直接改绑定不入栈）、**`switchTo` / `goBack` / `not-found` / `back-empty`**、**首次不 `change`**、**插槽与 scoped 参数**、**过渡三态**、**`ElPageHeader` 与返回**、**`defineExpose`** 及验收标准。

### Modified Capabilities

- （无）新增前端 packages 能力；不修改既有 `openspec/specs/` 下其它能力的需求。

## Impact

- **代码**：新增 `quick-ui/src/packages/C7ViewSwitch/index.vue`；修改 `quick-ui/src/packages/index.js`。
- **文档**：本变更目录下 `proposal` / `design` / `tasks` / `specs/ui-c7-view-switch/spec.md`；`docs/` 下 C7 组件文档页。
- **依赖**：以现有 **Vue 3**、**Element Plus** 为准；不新增 npm 包（除非实现时发现缺口，再于 tasks 中说明）。
