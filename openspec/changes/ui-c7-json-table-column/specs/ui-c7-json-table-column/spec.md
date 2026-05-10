# ui-c7-json-table-column

## Purpose

为 **quick-ui** 提供 **`C7JsonTableColumn`**：根据列描述数组 **`columns`** 生成 **`Element Plus`** 的 **`ElTableColumn`** 集合，内置常见 **`columnType`** 单元格渲染与列头插槽转发，减少列表页重复代码，并与 **C7JsonTable** 的列配置方向对齐。需求来源：**`原始需求/前端/C7JSON表格列.md`**、**`docs/superpowers/specs/2026-05-08-c7-json-table-column-design.md`**。

## ADDED Requirements

### Requirement: 组件位置、根节点与全局注册

**`C7JsonTableColumn`** MUST 实现于 **`quick-ui/src/packages/C7JsonTableColumn/index.vue`**（首版单文件）。组件根 MUST 渲染为 **多个 `el-table-column`**（Vue 3 Fragment），以便父级写作 **`<el-table><C7JsonTableColumn :columns="cols" />…</el-table>`**。

组件 MUST 通过 **`quick-ui/src/packages/index.js`** **export**，并纳入 **`installPackages(app)`** 注册为全局组件 **`C7JsonTableColumn`**。

#### Scenario: 全局注册后模板可用

- **WHEN** 应用已执行 **`installPackages`**
- **THEN** 模板中 MUST 能直接使用 **`<C7JsonTableColumn />`** 而无需额外局部注册

---

### Requirement: Props `columns` 与 `emptyText`

**`columns`** MUST 为组件的 **必填**入参（类型为数组；运行时校验）。**`emptyText`** MUST 为可选 **string**，表示表级默认空文案。

当 **`columns` 非数组**时，组件 MUST **不渲染任何列**，且在 **开发环境** MUST **`console.warn`**。

当 **`columns` 为空数组**时，组件 MUST **不渲染列**（合法状态，无 warn）。

#### Scenario: 非法 columns 不渲染并列 warn

- **WHEN** **`columns`** 传入 **`null`**、**`undefined`** 或非数组对象
- **THEN** 表格内 MUST 不出现由本组件生成的列；且在 **开发环境** MUST 出现 **warn**

---

### Requirement: 列过滤与 order 排序

仅 **`visible !== false`** 的列 MUST 被渲染（未设置 **`visible`** 时默认展示）。

列渲染顺序 MUST 满足：**先按 `order` 数值升序**；**未设置 `order` 的列** MUST 排在 **已设置 `order` 的列之后**；**`order` 相同**时 MUST 保持 **`columns` 输入数组中的原始相对顺序**（稳定排序）。

#### Scenario: order 未设置在后

- **WHEN** **`columns`** 含 **A（无 order）**、**B（order=1）**、**C（无 order）** 且输入顺序为 A、B、C
- **THEN** 渲染顺序 MUST 为 **B、A、C**

#### Scenario: 同 order 保持输入顺序

- **WHEN** 两列 **`order` 均为 `1`** 且输入顺序为先 X 后 Y
- **THEN** 同 **`order`** 分组内 MUST 先 X 后 Y

---

### Requirement: 透传到 ElTableColumn

对每一列，若列对象上存在以下字段，MUST 绑定到对应 **`el-table-column`**：**`prop`、`label`、`width`、`minWidth`、`fixed`、`align`、`headerAlign`、`sortable`、`showOverflowTooltip`**（语义与 **Element Plus** 一致）。

列对象上的 **`props`** 键 MUST 表示「其余列级属性」对象，并以 **`v-bind` 合并**到 **`el-table-column`**（与上述显式字段不冲突；冲突时以 **`props`** 或实现文档化规则为准，且须在 JSDoc 说明）。

#### Scenario: props 中的 resizable 生效

- **WHEN** 某列 **`props.resizable`** 为 **`true`**
- **THEN** 对应 **`el-table-column`** MUST 表现为可调整列宽（以 EP 行为为准）

---

### Requirement: columnType 默认值与未知类型

**`columnType`** 未设置时 MUST 视为 **`text`**。

未知 **`columnType`** 值 MUST 按 **`text`** 规则渲染，且在 **开发环境** MUST **`console.warn`**。

#### Scenario: 未知类型降级为 text

- **WHEN** **`columnType`** 为 **`"unknown"`**
- **THEN** 行为 MUST 与 **`text`** 一致，且 **开发环境** MUST **warn**

---

### Requirement: columnType text 与 formatter 互斥语义

当列上 **存在 `formatter`** 时，组件 MUST 将 **`formatter`** **直接传给** **`el-table-column`**（签名为 **`(row, column, cellValue, index) => string`**，与 EP 一致），且 MUST **不对返回值再做空值替换为 `emptyText` 或 `'-'`**。

当列上 **不存在 `formatter`** 时，组件 MUST 按 **`prop`** 从行数据读取 **`cellValue`**；若 **`cellValue`** 为 **`null`、`undefined` 或 `''`**，则展示 **列级 `emptyText` ?? 组件 prop `emptyText` ?? 占位常量 `'-'`**（占位常量 MUST 与实现代码注释及本文档一致）。

**MUST NOT** 将「仅空白字符串」视为空值（除非后续独立变更明确修改）。

#### Scenario: 有 formatter 时不套 emptyText

- **WHEN** 列配置 **`formatter`** 且返回空字符串
- **THEN** 单元格展示 MUST 以 **EP/formatter 返回值**为准，**MUST NOT** 被组件替换为 **`-`** 或表级 **`emptyText`**

#### Scenario: 无 formatter 时空值占位

- **WHEN** 列无 **`formatter`**，**`prop`** 对应单元格值为 **`null`**，且列与表级均未配置 **`emptyText`**
- **THEN** 单元格 MUST 展示 **`'-'`**

---

### Requirement: columnType tag

当 **`columnType === 'tag'`** 时，单元格 MUST 使用 **`C7DictTag`** 只读展示：**`modelValue`** MUST 绑定为 **`row[prop]`**；**`prop`** 缺失时 MUST **开发环境 warn** 且 **该列不渲染**。

**`C7DictTag`** 的选项列表 MUST 取自 **`col.options ?? col.dictList`**（**`options` 优先于 `dictList`**）。其它 **`C7DictTag`** 合法属性 MUST 允许从列对象同名透传，或通过 **`props`/扩展字段** 传入（实现以文档示例为准）。

#### Scenario: options 覆盖 dictList

- **WHEN** 同列同时存在 **`options`** 与 **`dictList`**
- **THEN** **`C7DictTag`** MUST 使用 **`options`** 作为数据源

---

### Requirement: columnType image

当 **`columnType === 'image'`** 时，单元格 MUST 使用 **`C7Preview`**：**`urls`** MUST 绑定 **`row[prop]`**（**string**，与 **C7Preview** 首版逗号分隔约定一致）；**`coverType`** MUST 为 **`'none'`**；**`autoDetect`** MUST 默认与 **C7Preview** 组件默认一致。

**`prop`** 缺失时 MUST **开发环境 warn** 且 **该列不渲染**。缩略尺寸等视觉参数 MUST 可通过 **C7Preview** 已有 props 在列对象上配置（文档给出推荐默认值）。

#### Scenario: 表格内预览不为 button 式

- **WHEN** **`columnType === 'image'`**
- **THEN** **`C7Preview`** 的 **`coverType`** MUST 为 **`none`**（避免默认 **`button`** 式在表内嵌套造成交互混乱）

---

### Requirement: columnType link

当 **`columnType === 'link'`** 时，单元格 MUST 渲染 **`<a>`**。**`linkHref`** 与 **`linkText`** 每项 MUST 支持 **`string`** 或 **`(row, column, cellValue, index) => string`**。**`linkTarget`** 首版 MUST 仅支持 **`string`**（如 **`_blank`**）；未传入时 MUST **不设置 `target` 属性**。

当解析后的 **`href`** 为 **空串、`null` 或 `undefined`** 时，组件 MUST **不渲染可点击链接**，并 MUST 展示 **`-`**（与 **text** 空占位视觉一致）。

**`prop`** 缺失时 MUST **开发环境 warn** 且 **该列不渲染**（**`cellValue`** 无法定义）。

#### Scenario: href 为空时显示占位

- **WHEN** **`linkHref`** 解析结果为 **`''`**
- **THEN** MUST **无 `<a href>` 可点击链接**，且展示 **`-`**

---

### Requirement: columnType slot

当 **`columnType === 'slot'`** 时，具名插槽名 MUST 为 **`slotName || prop`**。**`slotName` 与 `prop` 均无有效值**时，MUST **开发环境 warn** 且 **该列不渲染内容区**（或等价跳过策略与实现一致且在 JSDoc 说明）。

在对应 **`el-table-column` #default** 中，组件 MUST **`<slot :name="…" v-bind="scope" />`**，其中 **`scope`** MUST 与 **EP `ElTableColumn` 默认作用域**一致：**至少包含 `row`、`column`、`$index`**（与 **5A** 澄清一致）。

当父级 **未**提供对应具名插槽时，单元格 MUST 展示 **`-`**。

#### Scenario: 父提供 action 插槽

- **WHEN** 列 **`prop`** 为 **`action`** 且 **`columnType`** 为 **`slot`**，父组件提供 **`#action`**
- **THEN** 该列单元格 MUST 渲染父插槽内容，且作用域含 **`row`**

---

### Requirement: 列头插槽 header-${prop}

仅当列具备 **`prop`** 时，组件 MUST 支持父级通过 **`#header-${prop}`** 覆盖列头：在 **`el-table-column` #header** 中转发，**`v-bind`** 的作用域形状 MUST 以当前 **Element Plus `ElTableColumn` header 插槽**为准（实现以类型定义或官方文档写入 JSDoc）。

无 **`prop`** 的列 MUST **不支持**该具名列头插槽（列头仅 **`label`** 或 EP 默认行为）。

#### Scenario: 有 prop 时可覆盖列头

- **WHEN** 列 **`prop`** 为 **`username`** 且父提供 **`#header-username`**
- **THEN** 列头 MUST 渲染该插槽内容而非仅 **`label`**

---

### Requirement: 与 C7DictTag / C7Preview 的依赖契约

**`tag`** 列：**`options`/`dictList`** MUST 为数组语义；不匹配时的展示/回退行为 MUST 与 **`C7DictTag`** 既有实现一致。

**`image`** 列：**`coverType`** MUST **不为 `button`**（见 image Requirement）；若业务需要按钮式预览，调用方 MUST 使用 **`slot`** 列自行组合。

#### Scenario: tag 列沿用 DictTag 行为

- **WHEN** **`options`** 为空数组
- **THEN** 展示行为 MUST 与 **`C7DictTag`** 对空选项的既有行为一致

---

### Requirement: 文档与 E2E

**VitePress** MUST 在与其它 **C7** 通用组件一致的目录下新增 **`C7JsonTableColumn`** 文档页，内容 MUST 至少覆盖：**Props**、列配置字段表、**`columnType`** 说明、父组件示例、与 **C7JsonTable** 的关系说明。

项目 MUST 提供 **E2E 演示路由/页面**（名称如 **`C7JsonTableColumnE2E`** 或等价），覆盖：**各 `columnType` 关键路径**、**`options` vs `dictList`**、**`formatter` 与无 formatter 分支**、**具名 slot**、**`#header-${prop}`**、**`order` 排序**。

#### Scenario: 文档可访问

- **WHEN** 开发者打开文档站点中 **C7JsonTableColumn** 页面
- **THEN** MUST 能读到 **Props** 与列配置说明，并包含可运行示例片段或指向 E2E 页
