## Context

- **quick-ui**：Vue 3 + Element Plus；已有 **`C7DatePicker`**（`quick-ui/src/packages/C7DatePicker/index.vue`）提供 **`rangeMerge` / `mergeDelimiter`**、**`outerToInner` / `innerToOuter`** 与 **`warnParseOnce`** 等约定。
- **原始需求**：**`原始需求/前端/C7时间选择器.md`**。
- **已定稿设计说明**：**`docs/superpowers/specs/2026-05-07-c7-time-picker-design.md`**（brainstorming 澄清：**1B** 含需求/规范/实现对照；**2A** 默认 **`HH:mm:ss`**；**3A** **`mergeDelimiter` 与 `range-separator` 分离**；**4A** **`rangeMerge=false` 不解析外向 `string`**；**5A** 设计落盘路径）。
- **proposal**：新增 **`C7TimePicker`**；与 **`C7DatePicker`** 在「单数据流 / 分隔符语义」上对齐，在「范围外向 **`string`** 是否拆分」上 **按设计有意区分**。

## Goals / Non-Goals

**Goals**

- **`ElTimePicker`** 根封装；**`inheritAttrs: false`**；独占 props **不透传**到 EP；**`model-value` / `@update:model-value`** 由包装层接管。
- **默认格式**：**`format` 未定义**且 **`valueFormat` 与 `value-format` 均未定义**时注入 **`HH:mm:ss`**。
- **范围**：以 **`is-range` 或 `isRange` 为 true`** 判定；**`rangeMerge` / `mergeDelimiter`** 仅在此生效。
- **单数据流**：**`computed(innerModelValue)`** + **`outerToInner` / `innerToOuter`**，对齐 **`C7DatePicker`**，避免 **`watch` 双状态**。
- **事件**：**`update:modelValue`**、**`change`**（载荷经 **`innerToOuter`**）、**`blur` / `focus`** 显式转发；其它 EP 事件由 **attrs** 承担，**不**在 **`defineEmits`** 穷举。

**Non-Goals**

- 不包含 **日期 + 时间** 混合（由 **`C7DatePicker`** 等承担）。
- **不**修改 **`C7DatePicker`** 源码。

## Decisions

1. **与 `C7DatePicker` 同构**  
   - **理由**：同一套「存储分隔符 vs 面板 **`range-separator`**」「非法串 → **`null` + warn**」心智，降低业务误用。

2. **`mergeDelimiter` 空值回退为 `','`**  
   - 与 **`C7DatePicker#effectiveDelimiter`** 一致。

3. **非范围 `outer` / `inner` 直透传**  
   - **不对**非范围 **`''`** 做额外归一化（与 **`C7DatePicker`** 非范围分支一致）。

4. **`innerToOuter` 范围 + `rangeMerge` 的 null 段**  
   - 使用与 **`C7DatePicker`** 相同的 **`String(a ?? '')`** 拼接策略；**`a == null && b == null`** → 外向 **`null`**。

5. **attrs 处理**  
   - 从 **`useAttrs()`** 拷贝到 **`pickerAttrs` `computed`** 时删除 **`onUpdate:modelValue` / `onChange` / `onBlur` / `onFocus`**（或与本组件显式监听冲突的键），再合并默认 **`format` / `valueFormat`**。

6. **与 `C7DatePicker` 的 `outerToInner` 差异（已定稿）**  
   - **`C7DatePicker`**：范围 **`type`** 下，外向 **`string`** **始终**按 **`effectiveDelimiter`** 拆两段（与 **`rangeMerge` 无关**；**`rangeMerge` 只影响 `innerToOuter` 是否合并**）。  
   - **`C7TimePicker`**：**`rangeMerge=false`** 时 **不**解析外向 **`string`**（仅长度 2 数组）；需合并串回显须 **`rangeMerge=true`** 或上层先拆数组。  
   - **理由**：避免在「外向应为数组」的契约下 **静默误解析**；评审以 **`2026-05-07-c7-time-picker-design.md`** 第 8 节为准。

## Risks / Trade-offs

| 风险 | 缓解 |
|------|------|
| **EP 版本差异导致 `is-range` 值形态不同** | 以 **boolean true** 判定；在 JSDoc 写明依赖 **Element Plus 2.x** 与项目锁定版本 |
| **范围 + 非 `rangeMerge` 误传合并串** | **时间侧**不解析 **`string`** → **`null` + warn**；**不同于** **`C7DatePicker`** 范围下对 **`string`** 的拆分行为；在 **spec**、**OpenSpec design**、组件 JSDoc **显式注明** |
| **`computed` 重复触发导致 warn 刷屏** | 复用 **`warnParseOnce`** 模式 |

## Migration Plan

- 新页面使用 **`C7TimePicker`**；无数据迁移。

## Open Questions

- （无）实现阶段若 EP 对清空态返回 **`''` 而非 `null`**，在 **`innerToOuter`** 出口是否与 **`C7DatePicker`** 一样统一为 **`null`**：**以 spec 与 `C7DatePicker` 行为对照为准**，在实现 PR 中一次性写死并补注释。
