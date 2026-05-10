## Context

- **quick-ui**：Vue 3 + Element Plus；详情页重复拼装 **`ElDescriptions`** 与 **`C7DictTag` / `C7Copy`** 等，契约分散。
- **已定稿设计**：**`docs/superpowers/specs/2026-05-07-c7-descriptions-design.md`**（brainstorming 结论： **`tag` → `C7DictTag`**、空值分层、**`$attrs`** 透传、插槽作用域 **`{ row, value, item }`**）。
- **取值工具**：现有 C7 包已使用 **`lodash/get`**（如 **`C7Select`**、**`C7Checkbox`**），本组件 **MUST** 与之保持一致，**不**引入 **`lodash-es`** 除非后续全仓统一迁移。

## Goals / Non-Goals

**Goals**

- 单文件 **`C7Descriptions/index.vue`**（过长再抽 composable，首期不强制）。
- **根 `ElDescriptions`**：**`inheritAttrs: false`** + **`v-bind="$attrs"`**；显式 **`data` / `items` / `defaultEmptyText`**。
- **`items`**：**`prop` 点路径**、**`columnType`** 分支、**`formatter`**、**`emptyText`**、**`slotName`**；**`el-descriptions-item`** 合法字段 **`v-bind`** 到项配置（与 EP 对齐）。
- **`tag`**：**始终 `C7DictTag`**，**`:model-value`** 为解析值；**`options`** 等 props **平铺**于 **`item`**；**不适用** `defaultEmptyText` / `emptyText`。
- **`copy`**：**`C7Copy`** + **`copyProps`**；**`image`**：**`ElImage`** + **`imageAttrs`**；**`link`**：**字符串 `linkHref`**。
- **插槽**：**`title` / `extra`**；**`slotName`** 作用域 **`{ row, value, item }`**，**`row === data`**。
- **JSDoc**：组件与关键纯函数 **MUST** 具备 **`/** ... */`**，写清 **props**、**空值判定**、**`link` 列为空时是否渲染外链锚点**（与实现一致的一种策略）。

**Non-Goals**

- **不**修改 **`C7DictTag`** / **`C7Copy`** 源码行为。
- **不**在本期支持 **`linkHref` 为函数**、**`[]` 视为展示空**（见主设计）。
- **不**将 **`dictList`** 作为正式 API（文档与类型统一 **`options`**）。

## Decisions

1. **`data` 为 `null` / `undefined`**  
   - 归一为 **`safeRow`**：实现内使用 **`data ?? {}`** 作为取值与 **`row` 引用**（若需与「`row === data`」字面严格一致，仅在 **`data` 为对象**时 **`row` 为 `data`**；**`data` 为空**时 **`row` 为占位 `{}`** 并在 JSDoc 说明「非引用相等」例外，**或**要求调用方始终传对象——**推荐**：**`data` 缺省为 `{}`**，**`row` 始终等于传入的 `data` 引用`**；若父传 **`null`**，则 **`row` 为 `null`** 且点路径解析为 **`undefined`**，子层不抛错。与已定稿 superpowers 设计「建议默认 `{}`」对齐：**props 默认 `() => ({})`**，调用方传 **`null`** 时仍 **显式处理** 为 **`{}`** 或保留 **`null`**——采用 **superpowers 定稿**：允许 **`null`** 以无对象处理；**插槽 `row`** 与 **`data` 同引用**，故 **`data` 为 `null` 时 `row` 为 `null`**。实现须在 JSDoc 写明。

2. **`link` 列为展示空时**  
   - **推荐**：**不渲染 `<a>`**，仅渲染 **`item.emptyText ?? defaultEmptyText`** 的纯文本（避免空 **`href`** 与误导点击）。在实现注释中写死。

3. **未知 `columnType`**  
   - 按 **文本** 渲染；**开发环境**（如 **`import.meta.env.DEV`**) **`console.warn` 一次**（含未知类型字符串），生产环境静默。

4. **`copy` 的复制串**  
   - **`formatter`** 若返回非空字符串则优先作为复制文本；否则 **`value`** 经 **`String`**（**`null`/`undefined`** → 空串）。**`C7Copy`** 的 **`:text`** 绑定该串；**`copyProps`** 可覆盖 **`text`**（若同时存在，**spec** 规定优先级：**`copyProps.text` 若存在则覆盖自动串**，否则用自动串——为减少歧义，**设计定为**：**自动解析的复制串作为基础**，**`copyProps` 内若含 `text` 则覆盖**）。实际上更简单：**复制串**仅由 **formatter/value** 决定，**`copyProps` 不传 `text`**；若调用方硬传 **`copyProps.text`**，则以 **`copyProps`** 为准（**spec** 写：**`copyProps.text` 优先于自动推导**）。  

   最终决策：**复制用文本** = **`copyProps?.text ?? formatter结果 ?? String(value)`**（formatter 无或返回 `undefined` 则跳过）。写入 spec。

5. **`image` 的 `src`**  
   - **`ElImage`** 的 **`:src`** 绑定 **`prop` 解析值**；**`imageAttrs`** 与 **`src` 同时存在时**：**`src` 以解析值为准**（**`imageAttrs` 不覆盖 `src`**），其余 **`imageAttrs`** 合并。

## Risks / Trade-offs

| 风险 | 缓解 |
|------|------|
| **`ElDescriptions` / `ElDescriptionsItem` API 随 EP 变更** | 透传策略依赖 EP 文档；升级 EP 时回归本组件 smoke |
| **`items` 平铺与 `C7DictTag` props 命名冲突** | 列公共字段名固定文档化；**`tag` 专用字段** 与 **`C7DictTag`** 一致，冲突时以 **EP item 字段** 与 **dict 字段** 分离策略在 JSDoc 列出（如 **`label` 为列标题**，**`dictType` 为 C7DictTag**） |

## Migration Plan

- 新详情页直接使用 **`C7Descriptions`**；旧页可逐步替换手写 **`ElDescriptions`**。**无后端迁移**。

## Open Questions

- （无）**`[]` 是否展示空** 已定为首期 **否**。
