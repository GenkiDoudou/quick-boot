# ui-c7-tree-select

## Purpose

为 **quick-ui** 提供 **`C7TreeSelect`**：在 **Element Plus `ElTreeSelect`** 之上统一 **整棵树** 的 **静态/异步加载**、**字段映射**、**多选对外编码（数组 vs `separator` 逗号串）**、**`valueType`（含 `auto`）**，并与 **`C7Select`** 的 **`fetchData` / `fetchParams` / `resultKey` / `dataFormatter` / `autoLoad` / `separator`** 命名与行为对齐。需求来源：`原始需求/前端/C7树选择.md`；设计：`docs/superpowers/specs/2026-05-07-c7-tree-select-design.md`。

## ADDED Requirements

### Requirement: 组件与注册位置

**`C7TreeSelect`** MUST 位于 **`quick-ui/src/packages/C7TreeSelect`**（至少 **`index.vue`**），并通过 **`quick-ui/src/packages/index.js`** 的 **`installPackages(app)`** 注册为全局组件 **`C7TreeSelect`**；亦 MAY 通过 **`import { C7TreeSelect } from '@/packages'`** 按需使用。

#### Scenario: 全局注册可用

- **WHEN** 应用已执行 **`installPackages`**
- **THEN** 模板中 MUST 能使用 **`<C7TreeSelect />`** 且无需逐页局部注册

### Requirement: 静态数据来源与 options 别名

系统 MUST 支持 **`dataList`** 与 **`options`** 作为静态树来源；**当 `dataList !== undefined` 时 MUST 仅以 `dataList` 为准**（**`options` MUST 被忽略**）。当存在静态绑定时，组件 MUST NOT 在挂载时因 **`autoLoad`** 自动发起 **`fetchData`** 覆盖静态树（与 **`C7Select`** 静态优先策略一致）。

#### Scenario: dataList 优先于 options

- **WHEN** **`dataList`** 与 **`options`** 同时传入且 **`dataList !== undefined`**
- **THEN** 展示用树数据 MUST 来自 **`dataList`**

#### Scenario: 静态存在时不自动 fetch

- **WHEN** 传入 **`dataList`**（含空数组）且配置了 **`fetchData`** 与 **`autoLoad=true`**
- **THEN** 挂载时 MUST NOT 调用 **`fetchData`** 替换该静态来源

### Requirement: 异步整树加载与解析链

系统 MUST 支持 **`fetchData(mergedParams)`**，其中 **`mergedParams`** MUST 为 **`fetchParams`** 的浅拷贝合并（**不含 `query`** 键）。

- **`fetchData`** resolve 后，解析起点 MUST 为 **`response.data`**（与 **`C7Select`** 一致）。
- **`resultKey`**：MAY 从 **`response.data`** 上取点路径得到树数组；未配置时 **`dataFormatter`** 的输入以 **`response.data`** 为准（实现 MUST 在 JSDoc 写明空 **`resultKey`** 时的数组期望）。
- **`dataFormatter`**：MAY 在 **`resultKey`** 之后对数组做最终整形。
- 解析结果 MUST 为 **数组** 方可作为树根；非数组时实现 MUST 规范为空数组并可在开发环境 warn（与 **`C7Select`** 容错风格对齐）。

#### Scenario: autoLoad 挂载拉树

- **WHEN** **`autoLoad=true`**、提供 **`fetchData`**、且 **未**定义静态 **`dataList`/`options` 绑定**（与 **`C7Select`** 判定一致）
- **THEN** 组件 MUST 在挂载后发起 **一次** **`fetchData({ ...fetchParams })`**（无 **`query`**）

### Requirement: 请求失败与 load-error

当 **`fetchData`** reject 或抛出异常时，组件 MUST **`emit('load-error', err)`**（**`err`** 为 rejection 值或 **`Error`**）。组件 MUST **保留**上一次成功加载的树数据（MUST NOT 静默清空为成功态）。

#### Scenario: 失败不覆盖旧树

- **WHEN** 已有成功树数据后 **`fetchData`** 再次失败
- **THEN** 展示用树数据 MUST 仍为失败前最后一次成功结果

### Requirement: mapTree 与字段映射

系统 MUST 支持 **`labelKey` / `valueKey` / `childrenKey`**（默认值与 **`docs/superpowers/specs/2026-05-07-c7-tree-select-design.md`** 一致：**`label` / `value` / `children`**）。内部 MUST 通过 **`mapTree`** 将节点映射为 **`ElTreeSelect`** 所需的 **`label` / `value` / `children`**（及 **`disabled`** 若源节点 **`disabled === true`**）。

#### Scenario: 自定义字段映射

- **WHEN** 根节点使用 **`name`/`id`/`children`** 且传入对应 **`labelKey`/`valueKey`/`childrenKey`**
- **THEN** **`ElTreeSelect`** MUST 能正确展示标签并按 **`id`** 作为值进行选择

### Requirement: separator 与多选 v-model

当 **`multiple=true`** 且 **`separator=true`** 时，对外的 **`v-model` / `update:modelValue` / `change`** MUST 为 **英文逗号分隔字符串**；**空选择**对外 MUST 为 **`''`**（与 **`C7Select`** 的 **`separator`** 空值策略一致）。当 **`separator=false`** 时，对外 MUST 为 **数组**。

当外部 **`modelValue`** 为逗号分隔字符串时，组件 MUST **解析为数组**再驱动内部 **`ElTreeSelect`**（分段 **trim**、**忽略空段**；与 **`C7Select`** 行为对齐）。

#### Scenario: 多选逗号对外

- **WHEN** **`multiple=true`**、**`separator=true`**，且用户选中两项 **`1` 与 `2`**
- **THEN** **`update:modelValue`** MUST 发射 **`"1,2"`**（顺序与 EP 返回值一致，**不**强制额外排序）

### Requirement: valueType 与 auto

系统 MUST 支持 **`valueType`** 为 **`auto` | `string` | `number`**。

- 当 **`valueType='string'`** 时，对外的单选值与多选数组元素 MUST 经 **`String()`** 规范化（**`null`/`undefined` 边界**在 JSDoc 写明）。
- 当 **`valueType='number'`** 时，MUST 使用 **`Number()`** 尝试转换；**`NaN`** 的退化策略 MUST 在 JSDoc 写明并与实现一致。
- 当 **`valueType='auto'`** 时，MUST 以 **映射后根列表首节点** 的 **`value` 的 `typeof`** 判定：仅当为 **`number`** 且 **非 `NaN`** 时对外按 **number** 处理元素/单选；否则按 **string**。

#### Scenario: auto 随异步树首节点类型

- **WHEN** **`valueType='auto'`** 且首帧树为空、随后异步加载根首节点 **`value`** 为 **number**
- **THEN** 组件 MUST 在树数据到达后 **重新应用** 外部 **`modelValue`** 与内部的类型适配（**不得**永久停留在首帧的默认 string 推断而拒绝合法 number 选中）

### Requirement: attrs 透传与 filterable

组件 MUST 使用 **`inheritAttrs: false`**，并将 **未列入保留键** 的 **`$attrs`** 透传给 **`ElTreeSelect`**。保留键 MUST 至少包含 **`dataList`/`options`/`fetchData`/`fetchParams`/`resultKey`/`dataFormatter`/`autoLoad`/`separator`/`modelValue`/`multiple`/`labelKey`/`valueKey`/`childrenKey`/`valueType`**。

**`filterable`** 与 **`filter-node-method`**（或 EP 接受的等效命名）MUST 能通过透传生效。

#### Scenario: filterable 透传

- **WHEN** 调用方传入 **`filterable=true`**
- **THEN** **`ElTreeSelect`** MUST 进入可过滤交互（与 EP 文档行为一致）

### Requirement: reload 与 defineExpose

组件 MUST **`defineExpose`** 暴露 **`reload`** 方法：**异步模式**下 MUST 重新执行与 **`autoLoad`** 首次相同的 **`fetchData({ ...fetchParams })`**；**静态模式**下 MUST 至少重新执行 **`modelValue` → 内部值** 的同步（重新应用映射）。

组件 MUST 暴露 **`loading`**（进行中为 **`true`**）与 **`treeSelectRef`**（原生 **`ElTreeSelect` 实例**引用）。

#### Scenario: reload 刷新异步树

- **WHEN** 异步模式下调用 **`reload()`**
- **THEN** 组件 MUST 再次调用 **`fetchData`** 并在成功时更新树数据

### Requirement: 事件集合

组件 MUST **`emit('update:modelValue')`** 与 **`emit('change')`**，且 **`change`** 载荷 MUST 与对外 **`modelValue`** 形态一致。

组件 MUST 支持 **`visible-change`** 与 **`loading-change`** 的 emit（语义与 **`C7Select`** 对齐：**`loading-change`** 在 **`fetchData` 并发计数 >0** 时为 **`true`**）。

#### Scenario: loading-change 在请求期间为 true

- **WHEN** **`fetchData`** 尚未 resolve
- **THEN** **`loading-change`** MUST 至少一次为 **`true`**（与实现 **`inFlightCount`** 语义一致即可）

### Requirement: 一期不包含 lazy 远程子节点加载

本 capability MUST **不**将 **`lazy` + `load` 节点级异步** 作为验收项；业务若直接使用 **`ElTreeSelect`** 的 lazy，须自行评估与本组件 **`data` 管理**是否冲突（本变更 **不**承诺兼容）。

#### Scenario: 文档声明非目标

- **WHEN** 阅读 **`design.md` Non-Goals**
- **THEN** MUST 明确 **lazy 整树外子加载** 不在一期范围
