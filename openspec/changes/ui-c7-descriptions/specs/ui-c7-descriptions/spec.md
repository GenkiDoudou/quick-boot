# ui-c7-descriptions

## Purpose

为 **quick-ui** 提供 **`C7Descriptions`**：在 **`ElDescriptions`** 上以 **`data` + `items`** 配置驱动详情展示，内置 **`tag`（`C7DictTag`）**、**`image`**、**`link`**、**`copy`** 与文本/插槽扩展，与 **`docs/superpowers/specs/2026-05-07-c7-descriptions-design.md`** 及 **`原始需求/前端/C7描述列表.md`** 对齐。

## ADDED Requirements

### Requirement: 组件位置与全局注册

**`C7Descriptions`** MUST 位于 **`quick-ui/src/packages/C7Descriptions/index.vue`**（或同目录等价入口），并通过 **`quick-ui/src/packages/index.js`** 的 **`installPackages(app)`** 注册为全局组件 **`C7Descriptions`**，且 MUST 从该模块 **export** 供按需引用。

#### Scenario: 安装后模板可用

- **WHEN** 应用已执行 **`installPackages`**
- **THEN** 模板中 MUST 能直接使用 **`<C7Descriptions />`** 而无需页面侧再次 **`app.component`**

### Requirement: 根 ElDescriptions 与 attrs 透传

组件根节点 MUST 为 **`ElDescriptions`**。组件 MUST **`defineOptions({ name: 'C7Descriptions', inheritAttrs: false })`**，且在根 **`ElDescriptions`** 上使用 **`v-bind="$attrs"`**，使得 **`border`、`column`、`direction`、`size`** 等 **Element Plus `el-descriptions`** 文档中的合法属性由调用方写在 **`C7Descriptions`** 标签上即可生效。

#### Scenario: border 与 column 生效

- **WHEN** 传入 **`border`** 与 **`column`**（合法值）
- **THEN** 渲染结果 MUST 与在 **`el-descriptions`** 上直接使用相同属性时一致

### Requirement: 显式 props 与默认值

组件 MUST 声明以下 **props**（**不得**依赖 attrs 承载下列键名）：

- **`data`**：详情对象；**`items`**：列配置数组；**`defaultEmptyText`**：非 **`tag`** 列的默认空文案，默认 **`'暂无'`**。

**`items`** MUST 为数组；**`data`** 为 **`null`/`undefined`** 时，点路径解析结果 MUST 为 **`undefined`**，且 MUST **不抛异常**；**`row`** 与 **`data` 引用** 的关系 MUST 与 **「具名插槽作用域」** Requirement 一致。

#### Scenario: 默认空文案

- **WHEN** 某非 **`tag`** 列解析值为 **`null`** 且未配置 **`item.emptyText`**
- **THEN** 展示文案 MUST 为 **`defaultEmptyText`**（默认 **`暂无`**）

### Requirement: title 与 extra 插槽

组件 MUST 将 **`title`**、**`extra`** 插槽转发至 **`ElDescriptions`**，行为与 **Element Plus** 一致。

#### Scenario: 自定义标题区

- **WHEN** 父组件提供 **`#title`** 或 **`#extra`**
- **THEN** 描述列表头部区域 MUST 出现对应插槽内容

### Requirement: items 与 ElDescriptionsItem 绑定

对 **`items`** 中每一项，组件 MUST 渲染 **`el-descriptions-item`**，并将该项对象上 **属于 `el-descriptions-item` 的合法属性**（至少包含 **`label`、`span`**，及 EP 支持的其它字段）通过 **`v-bind`** 绑定到 **`el-descriptions-item`**。

#### Scenario: span 控制列宽

- **WHEN** 某项配置 **`span=2`**
- **THEN** 该项 MUST 按 EP 规则占据对应列跨度

### Requirement: 点路径取值

**`item.prop`** MUST 支持 **点号路径**（如 **`user.name`**）。取值 MUST 使用与现有 **`quick-ui/src/packages`** 一致的 **`lodash/get`**（或项目内已用于 **`C7Select`** 的同一 **`get`** 工具）。

#### Scenario: 嵌套字段展示

- **WHEN** **`data`** 为 **`{ user: { name: 'A' } }`** 且 **`prop`** 为 **`user.name`**
- **THEN** 该列展示内容 MUST 基于 **`'A'`**（经 **`formatter`/列类型** 处理）

### Requirement: 渲染优先级 slotName

当 **`item.slotName`** 为非空字符串 **且** 父组件提供了同名 **具名插槽** 时，该列 MUST **仅**通过该插槽渲染内容，**MUST NOT** 再应用 **`columnType`** 内置渲染。

#### Scenario: 插槽覆盖 columnType

- **WHEN** **`slotName='foo'`** 且存在 **`#foo`**
- **THEN** 内置 **`columnType`** 分支 MUST 不执行

### Requirement: 具名插槽作用域

对 **`item.slotName`** 对应的具名插槽，组件 MUST 注入作用域对象：**`{ row, value, item }`**，其中 **`value`** 为按 **`item.prop`** 从 **`data`** 解析的值，**`item`** 为当前列配置；**`row`** MUST 与父传入的 **`data`** 为 **同一引用**（当 **`data`** 为对象引用时；**`data` 非对象**时的语义 MUST 在组件 JSDoc 与 **「显式 props」** Requirement 中一致描述）。

#### Scenario: 插槽内可写回同一对象

- **WHEN** 插槽内修改 **`row.xxx`**
- **THEN** 父组件持有的 **`data.xxx`** MUST 同步变化（引用一致）

### Requirement: columnType 缺省与展示空

当 **未**命中 **`slotName` 插槽** 且 **`columnType`** 未指定或为空时，组件 MUST 按 **文本** 列处理：**`formatter(value, row, item)`** 若存在且返回值可用于展示，则使用该返回值；否则将 **`value`** 做合理字符串化。

**展示空**（**仅非 `tag` 列**）判定 MUST 为：**`value` 为 `null`、`undefined` 或 `''`**。**`[]`** 在首期 MUST **不**视为展示空。

当判定为 **展示空** 时，组件 MUST 展示 **`item.emptyText ?? defaultEmptyText`**。

#### Scenario: 空字符串显示暂无

- **WHEN** 文本列 **`value` 为 `''`**
- **THEN** 用户 MUST 看到 **`暂无`**（或当前 **`defaultEmptyText`**）

### Requirement: columnType tag 与 C7DictTag

当 **`columnType === 'tag'`** 时，组件 MUST **始终**渲染 **`C7DictTag`**。**`:model-value`**（或等价 **`v-model` 绑定侧**）MUST 绑定为当前 **`prop` 解析值**。**`C7DictTag`** 的其余 props（**`options`、`separator`、`showValue`、`max`、`collapse`、`dictType`、`size`、`effect`、`round`** 等，以 **`C7DictTag`** 实现为准）MUST 从 **`item`** 上 **同名键** 传入。

**`tag` 列`** MUST **不**应用 **`item.emptyText`** 与 **`defaultEmptyText`**；空态与未匹配展示 MUST **完全**与单独使用 **`C7DictTag`** 时一致（含 **`-`**）。

组件 MUST **不**将 **`dictList`** 作为正式 API；文档与类型 MUST 使用 **`options`**。

#### Scenario: tag 列空值显示与 C7DictTag 一致

- **WHEN** **`columnType='tag'`** 且解析值为 **`undefined`**
- **THEN** 展示 MUST 与仅挂载 **`C7DictTag`** 且 **`modelValue` 未定义** 时一致

### Requirement: columnType image

当 **`columnType === 'image'`** 时，组件 MUST 渲染 **`ElImage`**，且 **`src`** MUST 绑定 **`prop` 解析值**（字符串 URL）。组件 MUST 支持 **`item.imageAttrs`** 对象 **`v-bind`** 到 **`ElImage`**，且 **`src`** MUST **以解析值为准**，**`imageAttrs.src`** MUST **不覆盖**该解析 **`src`**。

#### Scenario: 图片可预览

- **WHEN** **`value`** 为合法图片 URL
- **THEN** 用户 MUST 能看到 **`ElImage`** 缩略图且 MUST 能使用 EP 默认预览能力（除非 **`imageAttrs`** 显式关闭）

### Requirement: columnType link

当 **`columnType === 'link'`** 时，若 **非展示空**，组件 MUST 渲染 **`<a>`**，**`href`** 来自 **`item.linkHref`**（字符串），**`target`** 来自 **`item.linkTarget`**；**`linkText`** 缺省时，展示串 MUST 为：**`formatter`** 有可用返回值则用其，否则 **`String(value)`**。

当 **展示空** 时，组件 MUST **不**渲染可导航的 **`<a>`**；MUST 展示 **`item.emptyText ?? defaultEmptyText`**（纯文本策略，与 **design.md** 一致）。

#### Scenario: 外链可点击

- **WHEN** **`linkHref`** 为 **`https://example.com`** 且值非空
- **THEN** 用户 MUST 能点击打开对应 **`href`**

### Requirement: columnType copy

当 **`columnType`** 为 **`copy`** 或 **`copyable`** 时，组件 MUST 渲染 **`C7Copy`**。**复制用文本** MUST 按：**`copyProps.text`**（若存在）优先，否则 **`formatter`** 返回字符串（若存在且非 **`undefined`**），否则 **`String(value === null || value === undefined ? '' : value)`**。

**`item.copyProps`** MUST **`v-bind`** 到 **`C7Copy`**（**`text`** 键遵守上述优先级）。

#### Scenario: 点击复制写入剪贴板

- **WHEN** 值为 **`hello`** 且 **`copyProps`** 未覆盖 **`text`**
- **THEN** 用户触发复制后剪贴板内容 MUST 包含 **`hello`**（在 **`C7Copy`** 既有成功语义下）

### Requirement: 未知 columnType

当 **`columnType`** 非空且 **不为** 已支持的 **`tag`/`image`/`link`/`copy`/`copyable`** 时，组件 MUST 按 **文本** 列规则渲染。在 **开发环境** 下，组件 MUST **`console.warn`** 至少一次并包含未知类型标识。

#### Scenario: 未知类型降级为文本

- **WHEN** **`columnType='weird'`** 且值为 **`'x'`**
- **THEN** 用户 MUST 能看到 **`x`**（或经 **`formatter`** 后的结果）

### Requirement: 不在本期修改 C7DictTag

本变更 MUST **不**修改 **`quick-ui/src/packages/C7DictTag`** 源码行为；仅 **引用**该组件。

#### Scenario: C7DictTag 行为保持独立

- **WHEN** 仅升级 **`C7Descriptions`** 而未改 **`C7DictTag`**
- **THEN** **`C7DictTag`** 的单元/集成行为 MUST 与变更前一致
