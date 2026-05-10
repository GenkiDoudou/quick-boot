# ui-c7-checkbox

## Purpose

为 **quick-ui** 提供 **`C7Checkbox`**：在 **Element Plus `ElCheckboxGroup`** 之上统一 **选项数据来源**（与 **`C7Select`** 一致的 **`fetchData` / `fetchParams` / `resultKey` / `dataFormatter` / `labelKey` / `valueKey`** 与 **`response.data` 解析链**）、**多选值的对外编码**（**逗号分隔 string** 或 **`string[]`**，且 **元素均为 string**）、以及 **可选的「全选/半选」** 与 **`max` 约束下全选禁用** 规则。需求来源：`原始需求/前端/C7多选框.md`。

## ADDED Requirements

### Requirement: 组件与注册位置

**`C7Checkbox`** MUST 位于 **`quick-ui/src/packages/C7Checkbox`**，并通过 **`quick-ui/src/packages/index.js`** 的 **`installPackages(app)`** 注册为全局组件 **`C7Checkbox`**；亦 MAY 通过 **`import { C7Checkbox } from '@/packages'`** 按需使用。

#### Scenario: 全局注册可用

- **WHEN** 应用已执行 **`installPackages`**
- **THEN** 模板中 MUST 能使用 **`<C7Checkbox />`** 且无需逐页局部注册

### Requirement: 静态数据来源

系统 MUST 支持通过 **`dataList`** 传入静态选项。当 **`dataList`** 与异步加载同时可触发时，**优先级 MUST 为：`dataList` 优先**（有 **`dataList`** 则 MUST NOT 为选项再发起 **`fetchData`**，除非 **`reload()`** 被调用且 design 允许刷新——**默认：静态优先时不自动 fetch**）。

#### Scenario: dataList 优先于 fetch

- **WHEN** 同时传入非空 **`dataList`** 与有效 **`fetchData`**
- **THEN** 选项 MUST 来自 **`dataList`**，且 MUST NOT 在挂载时因 **`autoLoad`** 默认触发异步覆盖（**`autoLoad` 与静态并存策略**以 **design** 为准：**静态存在则不 fetch**）

### Requirement: 异步数据来源与解析链（与 C7Select 对齐）

系统 MUST 支持 **`fetchData(mergedParams)`**，其中 **`mergedParams`** 为 **`fetchParams`** 的浅合并拷贝（**不含 `query`**）。

- HTTP 层 MUST 与项目 **`request` / axios 封装**一致：**Promise reject** 表示失败；**resolve** 后方可进入解析链。
- 解析起点 MUST 为 **`response.data`**（与 **`ui-c7-select`** 一致）。
- **`resultKey`**：MAY 用于从 **`response.data`** 进一步取出数组；未配置时 **`response.data` MUST 可作为选项数组输入**（或进入 **`dataFormatter`**）。
- **`dataFormatter`**：MAY 在 **`resultKey` 之后**做最终映射/过滤。
- **`labelKey` / `valueKey`**：MUST 将每项映射为 **`{ label, value }`** 供 **`ElCheckbox`** 使用；映射后的 **`value` MUST 经 `String()` 规范为 string** 再进入内部值数组。

#### Scenario: 解析链产出 string 选项

- **WHEN** **`fetchData`** resolve 且 **`response.data`** 经 **`resultKey`/`dataFormatter`** 得到列表
- **THEN** 每个选项绑定值 MUST 为 **string**，且展示文案 MUST 来自 **`labelKey`** 映射结果

### Requirement: autoLoad

当 **`autoLoad=true`** 时，组件 MUST 在挂载后 **自动调用一次** **`fetchData(mergedParams)`**（**无 `query`**）。

若 **`fetchData` 未提供** 且 **`autoLoad=true`**，实现 MUST 采取明确策略（推荐：**no-op** 且开发环境 **`console.warn`**），并在 JSDoc 说明。

#### Scenario: 挂载自动加载

- **WHEN** **`autoLoad=true`** 且提供 **`fetchData`**，且未提供 **`dataList`**
- **THEN** 组件 MUST 发起 **一次** **`fetchData`** 并更新可选项

### Requirement: joinValue 与 v-model 形态

内部选中状态 MUST 始终为 **`string[]`**。

- 当 **`joinValue=true`** 时：对外的 **`v-model` / `update:modelValue`** MUST 为 **逗号分隔字符串**；每个分段 MUST 为 **string**；**空选择**对外形态 MUST 固定一种（**`''`** 或 **`null`**）并与 **`C7Select` 多选 separator 空值策略**在实现中 **二选一对齐**，且 MUST 在 JSDoc 写明。
- 当 **`joinValue=false`** 时：对外 MUST 为 **`string[]`**。

当外部 **`modelValue`** 为 **逗号分隔字符串** 时，组件 MUST **解析为 `string[]`** 再驱动 **`ElCheckboxGroup`**（空字符串 MUST 解析为 **空数组**）。当外部为 **数组** 时，组件 MUST 将元素 **规范为 string**（**`String(x)`**）。

#### Scenario: 逗号串回显

- **WHEN** **`joinValue=true`** 且 **`modelValue`** 为 **`"1,2"`**
- **THEN** **`value` 为 `"1"` 与 `"2"`** 的选项 MUST 为选中态

### Requirement: 与 options 对不齐的 value（保留策略）

当 **`modelValue`** 中存在 **不在当前选项列表 `value` 集合** 中的选中值时，组件 MUST **保留**这些值于内部模型（MUST NOT 静默丢弃），直至用户操作或外部改写 **`modelValue`**。

#### Scenario: 异步未到仍保留已选

- **WHEN** **`modelValue`** 含 **`"9"`** 且当前 **`dataList`** 尚未包含 **`"9"`**
- **THEN** 内部选中集合 MUST 仍包含 **`"9"`**（展示允许无对应 label 的退化形态，与 EP 可接受行为一致即可）

### Requirement: showSelectAll、半选与 max 下全选禁用

当 **`showSelectAll=true`** 时，组件 MUST 展示「全选」控件，并 MUST 支持 **半选**（部分未禁用选项被选中且非全选）。

系统 MUST 支持 **`indeterminate` prop 作为 `showSelectAll` 的 deprecated 别名**（二者布尔语义一致；若同时传入，**以 `showSelectAll` 为准** 或 **二者 OR**——实现 MUST 固定一种并在 JSDoc 写明；**推荐：`showSelectAll || indeterminate`**）。

当配置了 **`max`**，且 **未禁用可选中项数量 `selectableCount` 满足 `selectableCount > max`** 时，「全选」控件 MUST **`disabled`**。

#### Scenario: 全选因 max 禁用

- **WHEN** **`showSelectAll=true`**、**`max=3`**，且未禁用选项有 **10** 项
- **THEN** 「全选」控件 MUST 为 **disabled**

#### Scenario: 半选可见

- **WHEN** **`showSelectAll=true`** 且仅选中部分未禁用项
- **THEN** 「全选」控件 MUST 呈现 **半选**语义（与 Element Plus **`indeterminate`** 视觉一致）

### Requirement: min、max、disabled

系统 MUST 支持 **`min` / `max` / `disabled`**（含 **`disabled` 全组禁用**语义）。**`min` 不满足**时的交互拦截 **不**作为本 spec 强制项（由页面校验承担）；组件 MUST 仍 **正确 emit `change` 与 `update:modelValue`**。

当某选项标记为禁用时，「全选」MUST **只作用于未禁用项**（与原始需求一致）。

#### Scenario: 禁用项不被全选选中

- **WHEN** **`showSelectAll=true`** 且用户点击「全选」
- **THEN** 选中集合 MUST **不包含**禁用项对应 value

### Requirement: checkboxStyle

系统 MUST 支持 **`checkboxStyle`**，取值为 **`default` | `button` | `border`**，并 MUST 映射到 Element Plus **`ElCheckbox`** 的对应展示模式（以当前 EP 版本 API 为准）。

#### Scenario: border 样式生效

- **WHEN** **`checkboxStyle="border"`**
- **THEN** 渲染结果 MUST 为 **border 风格**复选框组（与 EP 文档一致的可观测外观）

### Requirement: 事件

组件 MUST **`emit`**：

- **`update:modelValue`**：载荷形态由 **`joinValue`** 决定（**string** 或 **`string[]`**）。
- **`change(selected: string[])`**：载荷 MUST **始终**为 **`string[]`**（当前选中值，与 **`joinValue`** 无关）。
- **`loading-change(loading: boolean)`**：在 **`fetchData`** 发起与结束（成功/失败）时通知。

#### Scenario: change 始终为数组

- **WHEN** **`joinValue=true`** 且用户改变选中项
- **THEN** **`change`** 的载荷 MUST 为 **`string[]`**，且 **`update:modelValue`** MUST 为 **逗号分隔 string**

### Requirement: 对外暴露

组件 MUST 通过 **`defineExpose`** 暴露：

- **`loading`**：是否存在进行中的 **`fetchData`**。
- **`reload()`**：在适用时 **重新拉取选项**（当 **`dataList` 静态优先**时，`reload()` 行为 MUST 在 JSDoc 固定：推荐 **仅当存在 `fetchData` 时重新 fetch**，否则 no-op）。

#### Scenario: reload 触发重新请求

- **WHEN** 使用 **`fetchData`** 且无静态 **`dataList`**，且外部调用 **`reload()`**
- **THEN** MUST 再次执行 **`fetchData`** 并更新选项

### Requirement: 验收场景

#### Scenario: joinValue 逗号串回显与输出

- **WHEN** **`joinValue=true`** 且 **`modelValue="1,2"`**
- **THEN** MUST 正确回显两项选中；用户修改后 **`update:modelValue`** MUST 为符合 **`joinValue`** 的 string 或数组形态

#### Scenario: 全选三态正确

- **WHEN** **`showSelectAll=true`** 且未违反 **`max`**
- **THEN** 「全选 / 半选 / 全不选」状态 MUST 正确
