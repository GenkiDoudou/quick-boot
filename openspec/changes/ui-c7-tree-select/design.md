## Context

- **quick-ui**：Vue 3 + Element Plus；**`C7Select`**（`quick-ui/src/packages/C7Select/index.vue`）已约定 **`fetchData` → `response.data` → `resultKey` → `dataFormatter`**、**`autoLoad`**、**`separator`** 多选逗号串、**`loading-change`**、**`reload()`**。
- **上游设计**：`docs/superpowers/specs/2026-05-07-c7-tree-select-design.md`；**实现拆解**：`docs/superpowers/plans/2026-05-07-c7-tree-select.md`。
- **原始需求**：`原始需求/前端/C7树选择.md`（实现阶段将 **`rangeMerge`** 更名为 **`separator`** 与代码一致）。

## Goals / Non-Goals

**Goals**

- 单文件 **`C7TreeSelect/index.vue`**：**`ElTreeSelect`** + **`inheritAttrs: false`**，保留键过滤后 **`forwardedAttrs`** 透传（含 **`filterable`**、**`filter-node-method`**、**`multiple`**、**`check-strictly`** 等）。
- **静态**：**`dataList !== undefined` 时仅用 `dataList`**；否则 **`options`**；二者与 **`C7Select`** 别名语义一致。
- **异步整树**：**`executeFetch({ ...fetchParams })`**（**无 `query`**）；**`autoLoad`** 与 **`C7Select` 非 remote** 挂载策略一致；失败 **`emit('load-error', err)`** 且 **不覆盖**上次成功树数据。
- **`mapTree`**：将节点规范为 **`{ label, value, children, disabled? }`** 绑定 **`:data`**。
- **内外值**：**`defineModel`** + **`outerToInner` / `innerToOuter`**；多选 **`separator`** 与 **`C7Select`** 逗号规则一致；**`valueType`** 含 **`auto`**（根首节点 **`value` 类型**）及 **`watch(autoCoerceKind)`** 在异步树到达后重同步（见 plan 代码块）。
- **暴露**：**`reload`**、**`loading`**、**`treeSelectRef`**；事件 **visible-change**、**loading-change** 与 **`C7Select`** 对齐。

**Non-Goals**

- **不**实现 **`lazy` + `load`** 节点懒加载。
- **不**实现类 **`C7Select` remote-method** 的树内关键字搜索。
- **不**在一期抽取与 **`C7Select`** 共享的 composable（避免牵动 **`C7Select`**）。

## Decisions

1. **与 `C7Select` 对齐优先**  
   - **`separator`、空串 `''`、逗号分割/trim 规则** 与 **`C7Select`** 实现逐行对照，减少双栈心智。

2. **`valueType` 与 `auto`**  
   - **`auto`**：**映射后**根列表第一条的 **`value`**，**`typeof === 'number'` 且非 NaN** 则对外 number，否则 string。大整数精度问题在 JSDoc 中声明为 **`Number()`** 固有限制。

3. **loading 合并**  
   - **`mergedLoading = loadingInternal || attrs.loading`**，内部请求态与父级传入 **`loading`** OR；在 JSDoc 写明。

4. **静态场景 `reload()`**  
   - 无网络请求时 **`reload()`** 至少 **重新 `outerToInner(modelValue)`**，满足「重新应用映射/同步」的验收表述。

## Risks / Trade-offs

| 风险 | 缓解 |
|------|------|
| **`ElTreeSelect` 事件名与 EP 版本差异**（如 **visible-change**） | 以 **`quick-ui` 锁定的 element-plus** 为准；若构建报未知监听再查类型定义调整 |
| **与 `C7Select` 解析链漂移** | tasks 要求实现后 **diff 对照** **`executeFetch`** 与 **`response.data`** 起点 |
| **`auto` 首帧树为空** | plan 中 **`watch(autoCoerceKind)`** 已覆盖 |

## Migration Plan

- 新页面直接使用 **`C7TreeSelect`**。  
- 若业务文档仍写 **`rangeMerge`**：改为 **`separator`**，语义与 **`C7Select`** 一致。

## Open Questions

- （无）澄清已在 **`docs/superpowers/specs/2026-05-07-c7-tree-select-design.md`** 闭合。
