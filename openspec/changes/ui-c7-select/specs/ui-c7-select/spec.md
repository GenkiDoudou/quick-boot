# ui-c7-select

## Purpose

为 **quick-ui** 提供 **`C7Select`**：在 **Element Plus `ElSelect`** 之上统一 **选项数据来源**（静态、异步、远程搜索）、**`response.data` 解析链**（**`resultKey` / `dataFormatter`**）与 **多选时的对外值格式**（数组或逗号分隔字符串），减少页面重复逻辑。需求来源：`原始需求/前端/C7下拉选择.md`；**不**要求与 **`C7Button`** 流水线风格对齐（独立数据组件）。

## Requirements

### Requirement: 组件与注册位置

**`C7Select`** MUST 位于 **`quick-ui/src/packages/C7Select`**，并通过 **`quick-ui/src/packages/index.js`** 的 **`installPackages(app)`** 注册为全局组件 **`C7Select`**；亦 MAY **`import { C7Select } from '@/packages'`** 按需使用。

### Requirement: 静态数据来源

系统 MUST 支持通过 **`dataList`** 或 **`options`**（**二者为别名，语义相同**）传入静态选项；当二者同时存在时，**优先级 MUST 在实现中固定为一种**（推荐：**`dataList` 优先于 `options`**），并在组件 JSDoc 中写明。

### Requirement: 异步数据来源与解析链

系统 MUST 支持 **`fetchData(mergedParams)`**（名称可为等价包装，但对外契约须一致）从服务端加载选项。

- HTTP 层 MUST 与项目 **`request` / axios 封装**习惯一致：**Promise reject** 表示加载失败；**resolve** 后方可进入解析链。
- 解析起点 MUST 为 **响应体的 `data` 字段**（即 **`response.data`** 所指对象/数组，而非整段 response 的其它顶层字段）。
- **`resultKey`**：MAY 用于从 **`response.data`** 进一步取出数组（例如点路径）；若未配置，则 **`response.data` 本身 MUST 可作为选项数组**（或 **`dataFormatter`** 的输入）。
- **`dataFormatter`**：MAY 在 **`resultKey` 取值之后**对数组做最终映射/过滤；若未配置，则直接使用上一步得到的数组作为内部选项数据源。

### Requirement: autoLoad（非 remote）

当 **`autoLoad=true`** 且 **`remote` 不为 true** 时，组件 MUST 在挂载后 **自动调用一次** **`fetchData(mergedParams)`**（其中 **`mergedParams`** 由 **`fetchParams`** 拷贝/合并而来，**不含 `query`**）。

若 **`fetchData` 未提供** 且 **`autoLoad=true`**，实现 MUST 采取明确策略（推荐：**no-op** 并在开发环境 **`console.warn`**），并在 JSDoc 说明。

### Requirement: remote 与 query 语义

当 **`remote=true`** 时，组件 MUST 启用与 **`ElSelect`** 一致的 **远程搜索模式**（含 **`remote-method`** 或等价绑定）。

1. **首次全量加载（聚焦）**：用户 **首次打开/聚焦** 下拉时，MUST 调用 **`fetchData(mergedParams)`**，且 **`mergedParams` MUST NOT 包含 `query` 键**（「全量」= **不带 query 的一次请求**）。
2. **输入搜索**：用户输入关键字触发远程逻辑时，MUST 调用 **`fetchData({ ...fetchParams, query })`**（或等价浅合并），其中 **`query`** 为当前关键字。
3. **`reloadOnClear`**：当为 true 且用户 **清空已选值** 时，MUST **再次触发加载**；加载参数 MUST 与「全量」语义一致（**无 `query`**），除非在 design 中另行定义并经本 spec 交叉引用（**默认：无 `query`**）。

### Requirement: 竞态与防抖（remote）

在 **`remote=true`** 场景下，对 **`remote-method`** 触发链 MUST 具备 **防抖**（默认延迟由实现选定，建议 **300ms** 量级），且 MUST 采用 **last-write-wins**：仅 **最后一次有效完成** 的结果更新当前 **可见选项列表**（避免慢请求覆盖新关键字对应结果）。

### Requirement: 多选、separator 与 v-model 形态

当 **`multiple=true`** 时，**`ElSelect`** 内部 MUST 始终使用 **数组** 作为值形态。

- 当 **`separator=true`** 时：对外的 **`v-model` / `update:modelValue`** 与 **`change`** 载荷 MUST 为 **逗号分隔字符串**（元素为各选中项的 value 序列化，**单元素无多余逗号**）；空选择 MUST 对应 **空字符串** 或 **`null`/`undefined` 二选一**，实现 MUST 固定一种并在 JSDoc 写明。
- 当 **`separator=false`**（或未启用 **`separator`**）时：对外 MUST 使用 **数组**。

当外部传入的 **`modelValue`** 为 **逗号分隔字符串** 且 **`multiple=true`** 时，组件 MUST **先解析为数组** 再绑定到 **`ElSelect`**（空字符串 MUST 解析为 **空数组**）。

### Requirement: 与 options 对不齐的 value（保留策略）

当 **`multiple=true`**，且当前 **`modelValue`** 所表示的选中集合中存在 **不在当前 `options` 列表中** 的 value 时，组件 MUST **保留这些 value** 于模型中（MUST NOT 因对不齐而静默删除、清空或改写为 **`null`**）。**展示**上允许出现 **仅有 value、无对应 option 文案** 的多选 tag 形态（与 Element Plus 行为一致即可）。

### Requirement: 插槽透传

组件 MUST 将以下具名插槽 **原样透传** 至 **`ElSelect`**：**`prefix`**、**`label`**、**`option`**、**`empty`**（名称以当前 **`element-plus` 版本**支持的 **`ElSelect` 插槽**为准；若个别名称在版本中不存在，实现 MUST 在 JSDoc 中说明实际绑定关系）。

### Requirement: 事件

组件 MUST **`emit`**：

- **`update:modelValue`**：与 **`v-model`** 对齐，载荷形态由 **`multiple`** 与 **`separator`** 决定。
- **`change(valueOrString)`**：载荷 MUST 与对外 **`v-model`** 形态 **一致**。
- **`visible-change(open: boolean)`**。
- **`loading-change(loading: boolean)`**：在 **`fetchData`** 发起与结束（成功/失败）时通知。

### Requirement: 对外暴露

组件 MUST 通过 **`defineExpose`** 暴露：

- **`loading`**：当前是否存在进行中的 **`fetchData`**（或等价只读状态）。
- **`reload()``**：按当前 **`remote` / `autoLoad` / `fetchParams`** 状态 **重新拉取选项**（具体是否保留最后一次 **`query`** 由实现固定并在 JSDoc 说明）。

### Requirement: 验收场景

- **`remote=true`**：用户输入关键字后，**`fetchData`** MUST 以带 **`query`** 的参数被调用，且下拉选项 MUST 更新为最后一次成功返回的数据（在竞态场景下符合 **last-write-wins**）。
- **`multiple=true`**：**`separator=true`** 时对外 MUST 为 **逗号分隔字符串**；**`separator=false`** 时对外 MUST 为 **数组**；且 **对不齐的 value MUST 仍保留** 于模型中。
