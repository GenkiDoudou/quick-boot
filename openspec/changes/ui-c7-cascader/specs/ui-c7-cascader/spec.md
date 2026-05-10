# ui-c7-cascader

## Purpose

为 **quick-ui** 提供 **`C7Cascader`**：在 **Element Plus `ElCascader`** 之上统一 **树形选项**的 **静态 / 整树异步 / 懒加载** 数据来源、**`response.data` 解析链**（**`resultKey` / `dataFormatter`**）、**`labelKey` / `valueKey` / `childrenKey`** 映射，以及与 **`C7TreeSelect`** 对齐的 **`valueType`**、**`multiple` + `separator`**（在允许组合下）、**`load-error` / `loading-change`**。需求来源：**`原始需求/前端/C7级联选择器.md`** 与 **`docs/superpowers/specs/2026-05-08-c7-cascader-design.md`**。

## ADDED Requirements

### Requirement: 组件与注册位置

**`C7Cascader`** MUST 位于 **`quick-ui/src/packages/C7Cascader`**（推荐单文件 **`index.vue`**），并通过 **`quick-ui/src/packages/index.js`** 的 **`installPackages(app)`** 注册为全局组件 **`C7Cascader`**；亦 MAY 自 **`@/packages`** 按需 **import**。

#### Scenario: 安装包后全局可用

- **WHEN** 应用调用 **`installPackages(app)`**
- **THEN** 模板中 MUST 可使用 **`<C7Cascader />`** 而无需逐页 **import**（与现有 C7 包一致）

### Requirement: 与 Element Plus 的透传与保留字

组件 MUST 使用 **`inheritAttrs: false`**，并将 **未在组件自身 props 中声明的「业务/EP 配置」** 透传至 **`ElCascader`**（例如 **`emitPath`**、**`checkStrictly`**、**`props`**、**`show-all-levels`** 等，以当前 EP 版本为准）。

组件 MUST 在实现中维护 **保留属性集合**（至少包含：**`dataList`**、**`options`**、**`fetchData`**、**`fetchParams`**、**`resultKey`**、**`dataFormatter`**、**`autoLoad`**、**`lazy`**、**`rootParentId`**、**`labelKey`**、**`valueKey`**、**`childrenKey`**、**`separator`**、**`valueType`**、**`modelValue`**、**`multiple`** 等与 C7 契约相关的键），这些键 MUST NOT 以未知 DOM 属性的方式泄漏到 **`ElCascader`** 根上。

#### Scenario: emitPath 由业务配置

- **WHEN** 业务在 **`C7Cascader`** 上设置 **`emitPath`**（或等价 attrs）
- **THEN** 行为 MUST 与 **Element Plus 文档** 一致；**C7Cascader** MUST NOT 私自改写 EP 默认值语义

### Requirement: 静态数据来源

系统 MUST 支持通过 **`dataList`** 或 **`options`** 传入静态树数据；二者为别名语义。当 **`dataList !== undefined`** 时，MUST **仅使用 `dataList`** 作为静态数据源（**`options` 忽略**），与 **`C7TreeSelect`** 一致。

静态数据 MUST 经 **`mapTree`**（或等价）映射为 EP 所需 **`{ label, value, children }`** 形态后，作为 **`ElCascader`** 的 **`options`**（或 EP 当前版本要求的等价 prop 名）。

#### Scenario: 仅绑定 dataList

- **WHEN** **`dataList`** 传入非空树且 **`fetchData`** 未配置
- **THEN** 组件 MUST **不发起** **`fetchData`**，且级联面板 MUST 能展示对应层级选项

### Requirement: 整树异步与解析链

当 **未**处于本 spec 定义的 **懒加载模式**（见下条）、且 **未**绑定静态数据源时，若 **`autoLoad=true`** 且 **`fetchData`** 为函数，组件 MUST 在 **挂载后** 调用 **`fetchData({ ...fetchParams })`**（浅合并；**MUST NOT** 包含 **`parentId`**）。

解析链 MUST 与 **`C7TreeSelect`** 一致：

- 起点为 **`fetchData` resolve 后**的 **`res.data`**。
- **`resultKey`**：MAY 使用 **lodash `get`** 从 **`res.data`** 取数组；若为空字符串或未配置，则使用 **`res.data`** 作为 **`dataFormatter`** 的输入（与树选一致约定）。
- **`dataFormatter`**：在 **`resultKey` 取值之后**执行；若返回非数组，MUST 视为 **空数组**。

若 **`autoLoad=true`** 且 **`fetchData`** 未提供，实现 MUST **no-op**，且在 **开发环境** **`console.warn`**。

#### Scenario: autoLoad 挂载后出树

- **WHEN** 无静态绑定、**非懒加载**、**`autoLoad=true`**、**`fetchData`** 返回 **`res.data`** 且 **`resultKey`** 指向合法数组
- **THEN** **`options`** MUST 在成功回调后展示映射后的树；**`fetchData`** MUST 仅以 **`{ ...fetchParams }`** 调用一次（直至 **reload** 等显式刷新）

### Requirement: 懒加载与 fetchData 入参

当 **`lazy` 为 true**（与 EP **`lazy`** 语义一致）且 **`fetchData`** 为函数时，组件 MUST 使用 EP 的 **`lazy` + `lazyLoad`** 机制，在需要加载子层时调用 **`fetchData({ parentId, ...fetchParams })`**（浅合并 **`fetchParams`**）。

根层请求的 **`parentId`** MUST 等于 **`rootParentId`** prop（默认 **`null`** 须在 JSDoc 写明）。子层请求的 **`parentId`** MUST 为 **当前父节点映射后的 `value`**（即 **`valueKey`** 对应字段经 **`mapTree`** 后的 **`value`**）。

**`fetchData`** 返回的子层数据 MUST 解释为 **当前父节点下的子节点扁平数组**；组件 MUST 将其映射为 EP 子节点列表并交由 **`resolve`**（或 EP 要求的完成方式）结束本次懒加载。

#### Scenario: 展开节点带 parentId

- **WHEN** 用户展开某一非根父节点且 **`lazy=true`**
- **THEN** **`fetchData`** MUST 以包含 **该父节点 `value`** 的 **`parentId`** 被调用；成功后 MUST 展示子级选项

### Requirement: 错误、加载中与竞态

每次 **`fetchData`** 调用 MUST 纳入 **进行中计数**；**`loading-change(true)`** MUST 在存在未完成请求时发出；全部完成后 MUST 发出 **`loading-change(false)`**，语义对齐 **`C7TreeSelect`**。

**`fetchData`** **reject** 时，组件 MUST **`emit('load-error', err)`**，且 MUST NOT 替业务自动 **`ElMessage`**。

实现 MUST 使用 **`fetchGeneration`**（或等价）保证 **过期异步回调** 不覆盖当前 **options / 子节点** 状态。

#### Scenario: 请求失败可感知

- **WHEN** **`fetchData`** **Promise reject**
- **THEN** **`load-error`** MUST 被触发且参数为 **`err`**；**`loading-change`** MUST 最终回到 **false**

### Requirement: multiple、separator 与 emitPath 边界

当 **`multiple=true`** 且 **`separator=true`**，且 **`emitPath` 不为 true**、且 EP 内部多选值形态为 **一维标量数组**（与 **叶子多选** 一致）时，对外的 **`v-model` / `update:modelValue`** 与 **`change`** MUST 为 **英文逗号拼接字符串**；空选择 MUST 为 **`''`**；对内 MUST 仍使用 **EP 所需数组**。

当 **`emitPath=true`** 或内部值为 **非一维标量列表**（例如 **路径为嵌套数组**）且 **`separator=true`** 时，**`separator` MUST 视为无效**：对外 MUST 输出 **与 EP 一致的数组结构**；实现 MUST 在 **开发环境** **`console.warn`** 说明原因。

#### Scenario: emitPath 为 true 时不强制逗号串

- **WHEN** **`multiple=true`**、**`separator=true`**、**`emitPath=true`**
- **THEN** 对外 **`modelValue`** MUST 为 **EP 定义的路径数组形态**（不得用逗号拼接损坏结构）；且 **DEV** 下 MUST 出现 **warn**

### Requirement: valueType

组件 MUST 支持 **`valueType`** 取 **`auto`**、**`string`**、**`number`**，语义对齐 **`C7TreeSelect`**：**`auto`** 以规范后样本节点的 **`value` 类型**推断对外标量类型；**`emitPath=true`** 时 MUST 对 **路径中各层标量** 应用相同 coerce 规则，**不改变**路径的 **数组嵌套结构**。

#### Scenario: valueType 为 number 时单选叶子

- **WHEN** **`emitPath=false`**、**`valueType='number'`**、选项 **`value` 可解析为数字**
- **THEN** 对外 **`v-model`** MUST 为 **number** 类型叶子值（在 EP 与该组合一致的前提下）

### Requirement: 事件

组件 MUST **`emit`**：**`update:modelValue`**、**`change`**（载荷与对外 **`modelValue`** 一致）、**`visible-change`**、**`loading-change`**、**`load-error`**。

#### Scenario: change 与 v-model 一致

- **WHEN** 用户变更选中项导致 **`update:modelValue`** 发出
- **THEN** 随后 **`change`** 载荷 MUST 与 **`update:modelValue`** 的对外形态 **一致**

### Requirement: 验收场景（与原始需求对齐）

系统 MUST 满足：

1. **非懒加载**：无静态数据、**`autoLoad=true`**、**`fetchData`** 可用时，挂载后 **options** 可来自接口（失败走 **`load-error`**）。
2. **懒加载**：展开节点时 **`fetchData({ parentId, ...fetchParams })`** MUST 被调用且子层展示。
3. **多选**：在 **本 spec「separator 边界」**允许时，**`separator=true`** 对外 MUST 为 **逗号分隔字符串**。
4. **字段映射**：**`labelKey` / `valueKey` / `childrenKey`** 在静态、整树、懒加载路径上 MUST 生效。

#### Scenario: 字段映射在懒加载子层生效

- **WHEN** 懒加载接口返回行使用非默认字段名（如 **`deptName`/`id`**）
- **THEN** 映射后级联面板 MUST 显示正确 **label** 且 **`parentId`** 使用 **`id`**
