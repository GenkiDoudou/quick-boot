## Context

- **背景**：列表列渲染（字典、预览、链接、slot 等）重复，与 **C7JsonTable** 的 **`tableColumns[]`** 方向一致的需求已在前序 **brainstorming** 定稿（见 **`docs/superpowers/specs/2026-05-08-c7-json-table-column-design.md`**）。
- **当前状态**：**quick-ui** 已有 **C7DictTag**、**C7Preview** 等依赖组件；尚无 **`C7JsonTableColumn`**。
- **约束**：首版 **单文件 `index.vue`**（`v-for` + 按 **`columnType`** 分支）；逻辑膨胀后再抽 **`normalizeColumns.ts`**。组件 MUST 可在任意 **`ElTable`** 内使用。

## Goals / Non-Goals

**Goals:**

- 按 **`columns`** 生成多个 **`el-table-column`**（Vue 3 **Fragment 多根**）。
- 列过滤（**`visible !== false`**）、**`order` 数值升序**且未设置 **`order`** 的列排在后面、同 **`order`** 保持输入数组稳定顺序。
- 透传 **`prop`、`label`、`width`、`minWidth`、`fixed`、`align`、`headerAlign`、`sortable`、`showOverflowTooltip`**；列对象上的 **`props`**（实现侧可用 **`columnProps`** 变量名）以 **`v-bind` 合并**到 **`el-table-column`**。
- **`columnType`**：**`text`（默认）**、**`tag`**、**`image`**、**`link`**、**`slot`**；未知类型按 **`text`** 且 **dev warn**。
- 表级 **`emptyText`**；**`text`** 与 **`formatter`** 互斥语义（有 **`formatter`** 则完全交给 EP，不做空值改 **`emptyText`**）。
- 列头 **`#header-${prop}`** 转发（仅当列有 **`prop`**）；**`slot`** 列 **`#default`** 作用域与 EP 一致，具名 **`slotName || prop`**。
- **`packages/index.js`** 导出并 **`installPackages`** 注册。

**Non-Goals:**

- 搜索区、分页、列持久化、批量操作；**`selection` / `index`** 等列由父级自行声明。
- 树表懒加载细节。

## Decisions

| 决策 | 选择 | 理由 |
|------|------|------|
| 根结构 | 多根 **`el-table-column`** | 父级写法 **`<el-table><C7JsonTableColumn …/></el-table>`** 最直观，与设计文档一致。 |
| **`tag` 数据源** | **`options` 优先于 `dictList`** | 与澄清 **2B** 一致，避免两套字段语义冲突。 |
| **`link` 的 href/text** | **`string` 或函数 `(row, column, cellValue, index) => string`** | 澄清 **3B**；**`linkTarget`** 首版仅 **string**，不传则不设 **`target`**。 |
| **`slot` 作用域** | **`{ row, column, $index }`** | 澄清 **5A**；**`header`** 作用域以当前 EP **`ElTableColumn`** 类型为准写入 JSDoc。 |
| **`text` 空值** | **`null` / `undefined` / ''`** 视为空；占位 **列 `emptyText` ?? 表级 `emptyText` ?? `'-'`**（常量与代码注释、规范一致）；**不含**仅空白字符串。 |
| **`image`** | **`C7Preview`**：**`urls`** = **`row[prop]`**（string，逗号分隔与 **C7Preview** 一致）；**`coverType='none'`**；**`autoDetect`** 默认与 **C7Preview** 默认一致；缩略尺寸通过 **C7Preview** 已有 props 在列配置上传入。 |
| **`columns` 非法** | 非数组：不渲染列 + **dev warn** | 避免静默错误配置。 |

**备选未采纳**：将组件设计为单根 wrapper——会增加 DOM 与样式穿透成本，与 EP 表格列模型不符。

## Risks / Trade-offs

| 风险 | 缓解 |
|------|------|
| EP 升级导致 **`#header` 作用域** 字段变化 | 实现阶段以 EP 类型定义为准更新 JSDoc；规范要求「与 EP 一致」而非写死伪代码字段名。 |
| **`slot` 列未提供父插槽** | 单元格展示 **`-`**，便于发现配置遗漏（设计文档 **§8**）。 |
| **`formatter` + 空值`** 显示依赖 EP | 明确不叠加 **`emptyText`**，与 EP 行为一致，避免双重语义。 |

## Migration Plan

- **新增组件**，无存量 API 迁移。
- **回滚**：移除路由与 **E2E 页**、从 **`index.js`** 注销导出/安装、删除包目录与文档条目即可。

## Open Questions

- （无）实现前若需调整 **`text` 空白字符串** 是否算空，须走规范变更而非静默改行为。
