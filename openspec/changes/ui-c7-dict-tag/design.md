## Context

- **quick-ui**：Vue 3 + Element Plus；已有 **`C7Select` / `C7Checkbox`** 等，**`separator`** 与 **`options`** 形态可在展示组件侧对齐。
- **原始需求**：**`原始需求/前端/C7字典标签.md`**。
- **proposal** 已定：**`C7DictTag`**；探索阶段澄清：**`modelValue`** 命名、**重复 value 多 tag**、**未匹配 + `showValue=false` 显示 `-`**、**`+N` 可点**、**`max` 仅作用于已匹配 label 序列**、**tag 类型由字典类型驱动**。

## Goals / Non-Goals

**Goals**

- **只读展示**：无 **`v-model`** 回写需求；对外仍使用 **`modelValue`** 表示当前要展示的值集合。
- **解析**：**`modelValue`** 为 **array** 时按元素顺序；为 **string** 且含 **`separator`** 时拆分为原子值列表；**number** 与 **单 string** 视为单元素；**`null` / `undefined` / `''`** 走空态。
- **匹配**：对每个原子值 **`val`**，在 **`options`** 中查找满足 **`String(opt.value) === String(val)`** 的首项，取其 **`label`**；找不到则按 **`showValue`** 分支处理。
- **渲染顺序（两遍语义，一遍实现亦可）**：
  1. **预处理**：将 **`modelValue`** 解析为 **原子值数组**（顺序保留、不去重）。为每个原子计算是否 **已匹配**；若已匹配，计算其在 **仅含已匹配项** 的序列中的序号 **`mk`**（从 **1** 开始递增，仅对已匹配原子赋值）。
  2. **从左到右输出**（与原子顺序一致）：
     - **未匹配** 且 **`showValue=false`**：该位置渲染 **`-`**。
     - **未匹配** 且 **`showValue=true`**：该位置渲染 **`ElTag`（`type=info`）** 包裹 **原始原子值的字符串形式**。
     - **已匹配** 且 **`max<=0` 或未设置限制**：该位置渲染 **字典类型决定 `type` 的 `ElTag`（`label`）**。
     - **已匹配** 且 **`max>0`**：
       - 若 **`mk <= max`**：该位置渲染 **上述字典色 `ElTag`**。
       - 若 **`mk === max + 1`**：在该位置渲染 **唯一一枚 `+N`**，其中 **`N = 已匹配总数 − max`**（**`N > 0`**）。
       - 若 **`mk > max + 1`**：该位置 **不渲染任何节点**（不占布局空间），其 **label** 已包含在 **`+N`** 的溢出列表中。
- **`+N` 可点**：在 **`collapse=false`** 时，**`+N`** **MUST** 可点击（建议使用 **`ElPopover`** 或 **`ElDropdown`** 列出剩余已匹配项的 **label**；键盘 **Enter/Space** 聚焦触发为 **SHOULD**）。
- **`collapse=true`**：**`+N`** **MUST** 具备 **tooltip**（**`ElTooltip`**）展示超出项 **label** 列表（与原始需求一致）。**与「可点」同时成立时的优先级**：**点击优先打开 Popover/Dropdown**；**hover 仍可展示 Tooltip**（若两者冲突，实现选用 **Popover 内展示完整列表** 并 **弱化 hover-only**，以「可点」为准）。

**Non-Goals**

- 不内建 **字典远程加载**（由页面/父组件传入 **`options`**）。
- 不改变后端字典接口；**`dictType`** 仅用于前端 **`ElTag` type** 映射。

## Decisions

1. **`dictType` → `ElTag.type` 映射**  
   - 在组件旁提供 **默认映射表**（例如 **`success` / `warning` / `danger` / `info` / `primary`** 及项目常用字典类型别名）；**`dictType`** 未命中映射时 **fallback 为 `primary`**（与 **spec** 一致）。  
   - **备选**：由调用方 **`tagTypeResolver(dictType): ElTag['type']`** 注入——若实现阶段发现字典类型过多，可 **MAY** 增加可选 **inject prop**，本变更以 **内置表 + fallback** 为 **MVP**。

2. **`max` 计数范围**  
   - **仅统计已匹配项**；**`+N`** 的 **N** = **已匹配项总数 − min(max, 已匹配项总数)**（当 **`max>0`**）；**`max<=0`** 表示 **不限制**（展示全部已匹配 tag，与原始需求「仅当 max>0 折叠」一致）。

3. **未匹配 + `showValue=false`**  
   - 在该原子值位置渲染 **`-`**（单独一个短横字符组件/文本，与空态字符一致），**不**占用 **`+N`** 计数。

4. **重复 value**  
   - **不去重**；每个原子值独立匹配并渲染（两枚相同 **value** → 两枚 **tag** 或两枚 **`-`**/**info**，视匹配而定）。

5. **`effect` / `round` / `size`**  
   - **透传**至 **`ElTag`**（与 **Element Plus** 文档一致）；**未匹配 info tag** 同步透传 **`effect` / `round` / `size`**。

## Risks / Trade-offs

| 风险 | 缓解 |
|------|------|
| **Tooltip 与 Popover 叠加以致交互混乱** | 以 **Popover 承载「可点」完整列表** 为主；Tooltip 仅作补充或省略 |
| **字典类型爆炸** | **JSDoc** 写明扩展方式；预留 **可选** **`tagTypeResolver`**（若 tasks 排期允许） |
| **`+N` 位置与表格列宽** | 使用 **inline-flex** 与 **`gap`**；在 **spec** 中约束 **不换行** 为 **MAY**（由业务列宽决定） |

## Migration Plan

- 新列表/详情列改用 **`<C7DictTag />`**；无数据迁移。

## Open Questions

- **字典类型字符串全集**：首版以 **design 中示例映射 + fallback** 覆盖；若产品后续提供权威表，可同步扩展映射常量并补 **spec** 场景。
