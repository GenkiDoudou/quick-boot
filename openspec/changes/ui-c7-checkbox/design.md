## Context

- **quick-ui**：Vue 3 + Element Plus；**`C7Select`** 已在 `openspec/changes/ui-c7-select` 中定义 **异步解析链**（**`fetchData` → `response.data` → `resultKey` → `dataFormatter`**）与 **`loading` / `reload()`** 约定。
- **原始需求**：`原始需求/前端/C7多选框.md`。
- **已拍板**（探索结论）：**方框勾选**（`ElCheckboxGroup`）；**对外值一律为 string**（**`joinValue=true`** 为逗号串，否则 **`string[]`**）；**与 `C7Select` 公用一套数据来源与解析链**；**当「全选」无法在不违反 `max` 的前提下选中全部未禁用项时，「全选」控件禁用**；**`showSelectAll`** 为主命名，**`indeterminate`** 为 deprecated 别名。

## Goals / Non-Goals

**Goals**

- **选项**：**`dataList` 优先**；否则 **`fetchData(mergedParams)`**，其中 **`mergedParams`** 由 **`fetchParams`** 浅合并而来（本组件 **无 `remote`/`query`** 分支，与 **`C7Select` 的非 remote 自动加载**对齐）。
- **`autoLoad=true`**：挂载后 **自动调用一次** **`fetchData`**（无 **`fetchData`** 时的策略与 **`C7Select`** 一致：推荐 **no-op + dev warn**）。
- **解析链**：起点 **必须为 `response.data`**；**`resultKey` / `dataFormatter` / `labelKey` / `valueKey`** 语义与 **`C7Select`** 的 spec 描述 **对齐**（实现可抽取共享 util，但 **本变更 tasks 以「行为一致」为验收**，不要求强制抽 util）。
- **v-model 适配**：内部 **`string[]`**；入参允许 **逗号串或 `string[]`**，**统一规范化为 `string[]`**（每项 **`String(value)`**）；出参由 **`joinValue`** 决定串或数组。
- **全选行**：独立「全选」控件 + **`ElCheckboxGroup` 内各选项**；**半选**由「部分选中且非全选」推导；**全选 disabled** 条件见 **Decisions**。
- **可观测性**：**`loading-change`**；**`defineExpose({ loading, reload })`**。

**Non-Goals**

- **不**实现类 **`ElSelect` remote-method** 的关键字搜索（多选框场景不在本变更范围）。
- **不**规定业务表单 **`el-form-item` 校验** 必须由本组件触发（**`min`/`max`** 与 **Element Plus 校验**的衔接在页面层处理；组件提供 **props** 与 **稳定 `change`** 即可）。
- **不**解决「value 本身含分隔符」的歧义（与 **`C7Select` separator** 限制一致：在 JSDoc/spec **限制**中声明）。

## Decisions

1. **`fetchData` 签名**  
   - 采用 **`fetchData(mergedParams)`**，**`mergedParams`** = **`{ ...fetchParams }`**（无 **`query`**）。与 **`C7Select` 非 remote** 分支对齐，便于团队记忆一套参数合并方式。

2. **`joinValue` 与 `C7Select` 的 `separator`**  
   - **语义对齐**：**`joinValue=true`** ⇔ **`separator=true`**（对外逗号串）；**`joinValue=false`** ⇔ 对外数组。  
   - **命名**：本组件对外主 prop 使用 **`joinValue`**（贴合原始需求文档）；**不**强制提供 **`separator`** 别名（若实现阶段发现迁移成本高，可在实现中加 **deprecated 别名**并写 JSDoc，**不**上升为 spec 必须项）。

3. **`showSelectAll` 与 `indeterminate` 别名**  
   - **主 prop**：**`showSelectAll`** 控制是否渲染「全选」行。  
   - **别名**：**`indeterminate`**（旧文档）**仅作为 prop 别名**：与 **`showSelectAll`** 同步布尔语义；**不在此 prop 上暴露「单个 checkbox DOM indeterminate」的歧义用法**。

4. **「全选」禁用条件**  
   - 设 **`selectableCount`** = 当前选项中 **`disabled !== true`**（或未标记禁用）的项数。  
   - 若配置了 **`max`** 且 **`selectableCount > max`**，则 **「全选」控件 MUST `disabled`**。  
   - **理由**：避免用户点击「全选」后期望全选却被截断到 **`max`**；与探索结论一致。

5. **`change` 事件载荷**  
   - **`change(selected: string[])`** 始终为 **当前选中值的 `string[]`**（与 **`joinValue`** 无关），便于业务侧不解析逗号串。  
   - **`update:modelValue`** 仍与 **`joinValue`** 对齐（串或 **`string[]`**）。

6. **`checkboxStyle`**  
   - 取值 **`default` | `button` | `border`**；若历史代码仅传 **`button`** boolean，实现 MAY 在 major 前兼容映射到 **`checkboxStyle='button'`**（写入 JSDoc；spec 以 **`checkboxStyle`** 为准）。

7. **`min` 未满足**  
   - 组件 **不**内置阻止提交；**可选**：在 **`change`** 时 **不**抛错，由表单 **`rules`** 使用 **`min`** 校验（与 **Non-Goals** 一致）。若未来需要 **内置 warn**，单开变更。

## Risks / Trade-offs

| 风险 | 缓解 |
|------|------|
| 与 **`C7Select`** 解析链细节漂移 | tasks 中要求 **对照 `ui-c7-select` spec** 做 **对齐用例**（或共享单测/fixture） |
| **`ElCheckbox` border/button** 与 EP 版本差异 | 以 **`quick-ui` 锁定的 element-plus 版本**为准；样式落在 **`checkboxStyle` → EP props** 映射 |
| **全选 + 动态 `max`** | **`max` 变化**时重新计算 **全选 disabled**；在 **design** 已定义 **`selectableCount > max`** |

## Migration Plan

- 新页面直接使用 **`C7Checkbox`**。  
- 旧页面若使用 **`indeterminate` 表示全选**：改为 **`showSelectAll`**（或保留 **`indeterminate`** 别名直至移除）。

## Open Questions

- **空选择对外形态**：**`joinValue=true`** 时 **`modelValue`** 使用 **空字符串 `''`** 还是 **`null`** — 实现 MUST 与 **`C7Select` 多选 separator** 的空值策略 **二选一对齐** 并在 JSDoc 写明（推荐：**空字符串**）。
