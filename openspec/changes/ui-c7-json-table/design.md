## Context

- **背景**：列表页模式重复；已定稿 **`docs/superpowers/specs/2026-05-08-c7-json-table-design.md`** 与原始需求 **`原始需求/前端/C7JSON表格.md`**。
- **现状**：**`C7JsonTableColumn`**、**`C7Pagination`**、**`C7ExcelDownload`** 已在 `quick-ui/src/packages` 落地；**`C7JsonTable`** 尚未实现。
- **约束**：首版 **单文件 SFC**；搜索列为 **C7JsonForm 概念子集**；列持久化 **仅 localStorage**；列表错误 **不二次 toast**（走全局拦截器 + emit）。

## Goals / Non-Goals

**Goals:**

- 实现 **`C7JsonTable`**：配置 **`listFunction` + `tableColumns` + `searchColumns`** 即可跑通分页、排序、搜索、列设置、删除、导出与刷新。
- 与 **`C7JsonTableColumn`**、**`C7Pagination`**、**`C7ExcelDownload`** 的契约一致（Blob 导出、分页双绑与 `change` 语义）。
- **`packages/index.js`** 注册并导出。

**Non-Goals:**

- **`searchColumns` 全量对齐 C7JsonForm**（联动、校验、全部 field type）。
- 树表懒加载业务级验收；列设置服务端持久化。

## Decisions

| 决策 | 说明 | 备选 |
|------|------|------|
| **单文件首版** | 降低骨架成本，与 JsonTableColumn 首版策略一致 | 多子组件拆分 → 推迟到体量过大时 |
| **列区默认 C7JsonTableColumn** | 复用 `columnType` 与可见性规则 | 手写 `el-table-column` 为主 → 与列包重复 |
| **`#table-columns` 覆盖列子树** | 高级场景完全自定义列，外层仍单一 `el-table` | 整表 slot → 重复维护 data/ref |
| **导出走 Blob 契约** | 与 **`C7ExcelDownload`** / **`downloadRequest(returnBlobWithHeaders)`** 一致 | 仅 URL 下载 → 与现有导出链不一致 |
| **`columnSettingKey` + localStorage** | 满足原始需求、零后端 | 可注入 storage → YAGNI |
| **`before-fetch` 行为** | 实现前在仓库内核对 `request`/`list` 封装：若存在「返回 false 取消请求」惯例则支持，否则仅 emit、不拦截 | 写死在 spec 的一条路径 |

## Risks / Trade-offs

- **[Risk] 单文件过长** → **Mitigation**：先合并逻辑；超过阈值再抽 `useJsonTableQuery` / 列设置 composable。
- **[Risk] `searchColumns` 与未来 C7JsonForm 字段漂移** → **Mitigation**：文档列明「支持子集」；后续独立变更升级为嵌入 JsonForm 或扩展 schema。
- **[Risk] 导出 Loading 与 C7ExcelDownload 的 downloading 重复** → **Mitigation**：实现时在 tasks 中明确「全屏 ElLoading XOR 按钮 loading」二选一策略。
- **[Risk] `rowsKey`/`totalKey` 解析与后端结构不一致** → **Mitigation**：props 默认对齐项目主流列表接口；解析失败 dev warn + 空数据。

## Migration Plan

- 纯新增组件：**无**数据迁移；业务页可按页逐步替换手写列表。
- **回滚**：移除路由/引用并删除包目录；**不影响**已有页面（未引用则无行为变化）。

## Open Questions

- （实现阶段关闭）**`before-fetch` 是否拦截请求**：以 **`quick-ui`** 内列表页与 `utils/request` 的既有模式为准，在编码前只读调查后定案并写入组件 JSDoc。
