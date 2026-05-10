## Why

详情页普遍需要以「描述列表」展示对象字段，并混用字典标签、图片、外链、一键复制等形态；各页面手写 **`ElDescriptions` + `ElDescriptionsItem`** 重复多、易与 **`C7DictTag` / `C7Copy`** 等既有组件脱节。在已定稿的 **`docs/superpowers/specs/2026-05-07-c7-descriptions-design.md`** 与原始需求 **`原始需求/前端/C7描述列表.md`** 指导下，以配置驱动收敛实现与验收口径。

## What Changes

- 在 **quick-ui** 新增 **`C7Descriptions`**：根 **`ElDescriptions`**，**`inheritAttrs: false`** + 根 **`v-bind="$attrs"`**；显式 **`data` / `items` / `defaultEmptyText`**（默认 **`暂无`**，且**仅作用于非 `tag` 列**）。
- **`items[]`** 支持 **`prop` 点路径**、**`columnType`**（**`tag`** 内嵌 **`C7DictTag`**、**`image`**、**`link`**、**`copy`/`copyable`**、默认文本）、**`formatter`**、**`emptyText`**、**`slotName`**；**`el-descriptions-item`** 合法字段建议 **`v-bind`** 到项根。
- **`tag` 列**：**`options`** 等与 **`C7DictTag`** 对齐；**不使用** `defaultEmptyText` / `item.emptyText`；**不以 `dictList` 为正式 API**。
- **插槽**：**`title` / `extra`** 透传；**`item.slotName`** 对应具名插槽，作用域 **`{ row, value, item }`**（**`row` 与 `data` 同引用**）。
- **`copyProps` / `imageAttrs`** 分别透传 **`C7Copy` / `ElImage`** 的扩展配置。
- 在 **`quick-ui/src/packages/index.js`** 导出并全局注册 **`C7Descriptions`**。

## Capabilities

### New Capabilities

- **`ui-c7-descriptions`**：**`C7Descriptions`** 的 props/attrs 边界、**`items`** 与 **`columnType`** 行为、**空值与「暂无」**（非 `tag`）、**`tag` 与 `C7DictTag` 一致性**、**插槽与作用域**、**点路径与未知类型降级**、验收标准。

### Modified Capabilities

- （无）不修改 `openspec/specs/` 下既有主规格行为；本变更为新增 UI 包能力。

## Impact

- **代码**：新增 `quick-ui/src/packages/C7Descriptions/index.vue`；修改 `quick-ui/src/packages/index.js`。
- **依赖**：依赖已有 **`element-plus`**、**`lodash-es`**（若项目已用于表格取值则复用路径工具）、**`C7DictTag`**、**`C7Copy`**；不修改 **`C7DictTag`** 实现。
- **文档**：本变更目录下 **`proposal.md` / `design.md` / `tasks.md` / `specs/ui-c7-descriptions/spec.md`**；设计依据见 **`docs/superpowers/specs/2026-05-07-c7-descriptions-design.md`**。
