## 1. 组件与注册

- [ ] 1.1 在 **`quick-ui/src/packages/C7TreeSelect`** 新增 **`index.vue`**（默认导出 **`C7TreeSelect`**），实现见 **`docs/superpowers/plans/2026-05-07-c7-tree-select.md`** 中完整 SFC；补齐 **JSDoc**（props、emit、**`defineExpose`**、**`load-error`** 与 **`separator`/`valueType`** 边界）

- [ ] 1.2 在 **`quick-ui/src/packages/index.js`** **import / export / `installPackages`** 注册 **`C7TreeSelect`**

## 2. 数据与解析链（对齐 C7Select）

- [ ] 2.1 实现 **`dataList` 优先**（**`dataList !== undefined` 时忽略 `options`**）；否则 **`fetchData({ ...fetchParams })`**（**无 `query`**）+ **`autoLoad`** 挂载拉取；无 **`fetchData` 且 `autoLoad=true`** 时 **no-op + dev `console.warn`**（与 **`C7Select`** 一致）

- [ ] 2.2 实现 **`response.data` → `resultKey` → `dataFormatter`**；失败 **`emit('load-error', err)`** 且 **不覆盖**上次成功树数据；**`inFlightCount`** + **`loading-change`**

- [ ] 2.3 实现 **`mapTree`**（**`labelKey`/`valueKey`/`childrenKey`**）与 **`watch(autoCoerceKind)`** 异步首帧后类型重同步（见 **plan**）

## 3. v-model 与透传

- [ ] 3.1 实现 **`defineModel`** + **`outerToInner`/`innerToOuter`**；多选 **`separator`** 与 **`C7Select`** 逗号/trim/空 **`''`** 规则对齐

- [ ] 3.2 实现 **`valueType`**（**`auto`/`string`/`number`**）对单选与多选元素的出入转换；**`inheritAttrs: false`** + **`forwardedAttrs`**（保留键见 **plan**），保证 **`filterable`**、**`filter-node-method`** 等透传生效

- [ ] 3.3 绑定 **`@visible-change`** 并 **`emit`**；**`reload()`**：异步重新 **`fetchData`**，静态至少 **`outerToInner(modelValue)`**；**`defineExpose({ reload, loading, treeSelectRef })`**

## 4. 文档与原始需求

- [ ] 4.1 更新 **`原始需求/前端/C7树选择.md`**：将 **`rangeMerge`** 改为 **`separator`**，并注明与 **`C7Select`** 一致

- [ ] 4.2 新建 **`docs/docs/frontend/components/通用组件/c7-tree-select.md`**（侧栏已有链接则仅补文件）；文内链接 **`docs/superpowers/specs/2026-05-07-c7-tree-select-design.md`**

## 5. 验证

- [ ] 5.1 在 **`quick-ui`** 执行 **`pnpm build:prod`**，确认无编译错误；（可选）在 **`docs`** 执行 **`pnpm build`** 校验文档站点

- [ ] 5.2 **手动对照** **`openspec/changes/ui-c7-tree-select/specs/ui-c7-tree-select/spec.md`** 中 **ADDED** 场景做冒烟勾选
