## Context

- **quick-ui**：Vue 3 + Element Plus；**`C7Button` / `C7ButtonGroup`** 已存在于 `src/packages` 并通过 **`installPackages`** 全局注册。
- **原始需求**：`原始需求/前端/C7下拉选择.md`。
- **已确认口径**（对话拍板）：
  1. **`remote` 首次「全量」加载**：发起 **`fetchData`** 时 **不带 `query` 参数**（与「输入触发的远程搜索」区分）。
  2. 异步列表数据从 **HTTP 响应体的 `data`** 起算，再经 **`resultKey` / `dataFormatter`** 得到选项数组。
  3. **多选 + 逗号字符串**：进入 **`ElSelect`** 前 **解析为数组**；若部分 value **与当前 options 对不齐**，**保留 value（选项 A）**：不回写时静默丢弃；展示允许「仅有 value、无对应 label」的常见形态。
  4. **插槽**：**透传**至 **`ElSelect`** 同名具名插槽（**`prefix` / `label` / `option` / `empty`**）。
  5. **不要求**与 **`C7Button`** 的点击流水线 / 事件体系强行一致。

## Goals / Non-Goals

**Goals**

- **静态**：**`dataList`** 与 **`options`**（别名）二选一优先作为选项来源；与异步逻辑优先级在 spec 中写死，避免双源竞态。
- **异步（非 remote）**：**`fetchData(fetchParams)`**；**`autoLoad=true`** 时在挂载后自动拉取一次（与 **`remote=false`** 组合规则见下）。
- **远程搜索**：**`remote=true`** 时启用 **`ElSelect`** 的 **`remote` + `remote-method`**（或等价绑定）；**首次聚焦**触发 **无 `query`** 的 **`fetchData`**；输入时 **`fetchData({ ...fetchParams, query })`**；**`reloadOnClear=true`** 时在用户清空选择后 **再次拉取**（具体是「全量无 query」还是「带空 query」须与 spec 一致：**全量 = 无 query**）。
- **解析链**：默认从 **`response.data`** 读取；**`resultKey`** 为点路径或约定分隔（实现时选一种并写 JSDoc）；**`dataFormatter`** 作为最后整形钩子。
- **多选输出**：**`separator=true`** → 对外 **`v-model` / `change`** 为 **逗号分隔字符串**；否则为 **数组**。
- **可观测性**：**`loading-change`**；**`defineExpose({ loading, reload })`**。

**Non-Goals**

- 不定义通用「业务字典接口」后端契约（仅约定 **`fetchData`** 与 **`request` 习惯**一致：**reject** 表示失败；**resolve** 后进入解析链）。
- 不在本变更内规定全站表单封装（如 **`el-form-item` 联动校验**）的强制用法。

## Architecture Sketch

```
┌─────────────────────────────────────────────────────────────┐
│ C7Select                                                     │
│  props: dataList/options, fetchData, fetchParams,           │
│         resultKey, dataFormatter, autoLoad, remote,         │
│         multiple, separator, reloadOnClear, …                │
├─────────────────────────────────────────────────────────────┤
│  内部 options 状态 ◀──── fetchData / 静态 props               │
│  v-model 适配层 ◀─── 多选 separator 字符串 ↔ 数组            │
├─────────────────────────────────────────────────────────────┤
│  ElSelect（remote / remote-method / 插槽透传）                │
└─────────────────────────────────────────────────────────────┘
```

## Decisions

1. **`fetchData` 签名与合并**  
   - 推荐：**`fetchData(mergedParams)`**，其中 **`mergedParams`** 至少包含调用方 **`fetchParams`**；在远程搜索场景下 **追加 `query`**。  
   - **全量首载**：调用 **`fetchData(mergedParams)`** 且 **`mergedParams` 不含 `query` 键**（避免与「空字符串 query」歧义混用，除非后端明确等同并在注释中声明例外）。

2. **`autoLoad` 与 `remote`**  
   - **`autoLoad=true` 且 `remote=false`**：挂载后 **自动** **`fetchData`** 一次（若无 **`fetchData`** 则 no-op 或开发环境 warn，实现择一并在 JSDoc 写明）。  
   - **`remote=true`**：**首载时机**以 **「首次聚焦下拉」** 为准（与原始需求一致）；**不与**「仅挂载」自动拉取冲突：若同时 **`autoLoad=true`**，推荐 **仍以首次聚焦拉全量** 为准，**避免双请求**（在实现与注释中固定策略）。

3. **竞态与防抖**  
   - **`remote-method`**：**debounce**（延迟与是否可配置在实现中决定，默认如 **300ms**）；**仅最后一次 in-flight 或最后一次完成** 更新 **`options`**（**last-write-wins**），避免慢请求覆盖新关键字结果。

4. **`reload()`**  
   - **语义**：清空内部缓存的选项加载状态后，按当前模式 **重新拉取**（**`remote`** 下若需 `query` 是否保留最后一次输入，实现取 **「以当前 UI 状态为准」** 并在 JSDoc 说明）。

5. **`change` 载荷**  
   - **`change(valueOrString)`** 的载荷形态 **与对外 `v-model` 一致**（**`separator`** 决定字符串或数组），避免调用方二次猜测。

## Risks / Trade-offs

| 风险 | 缓解 |
|------|------|
| **`ElSelect` remote API 与 EP 版本差异** | 以当前 **`quick-ui` 锁定的 element-plus 版本**为准；实现注释标明依赖的 props 名 |
| **双源数据（静态 + 异步）** | spec 写死优先级；代码分支清晰 |
| **逗号分隔 value 含特殊字符** | 若业务 value 本身含逗号，**separator 模式不适用**——在 JSDoc 与 spec **Non-goals** 或 **限制**中声明 |

## Migration Plan

- 新页优先 **`C7Select`**；旧页可逐步把「手写 remote + options 赋值」迁到 **`fetchData`**。

## Open Questions

- **`reloadOnClear`** 为 true 时：除「重新拉取 options」外，是否 **重置最后一次 `query`**（影响下一次聚焦行为）——实现阶段在代码注释与 Dev 示例中固定一种即可。
