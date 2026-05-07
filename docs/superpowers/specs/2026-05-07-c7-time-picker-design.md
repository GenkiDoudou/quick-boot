# C7TimePicker（C7 时间选择器）设计说明

**日期**：2026-05-07  
**状态**：已定稿（brainstorming 澄清：**1B** 交付含与现实现状**一致性 / 差异**说明；**2A** 仅默认 **`HH:mm:ss`**，父级显式传入则**不覆盖**；**3A** **`mergeDelimiter` 与 EP `range-separator` 语义分离**；**4A** 维持 **`rangeMerge=false` 时不将外向 `string` 按存储串解析**；**5A** 以本文档为设计落盘）  
**依据**：`原始需求/前端/C7时间选择器.md`；与 **`C7DatePicker`** 在 **「计算属性 + `outerToInner` / `innerToOuter`」单数据流、`mergeDelimiter` 回退规则、默认格式注入模式」** 上同构；在 **范围模式下外向 `string` 是否进入拆分逻辑** 上与 **`C7DatePicker` 有意不同**（见第 8 节、第 15 节）。

---

## 1. 背景与目标

时间选择与日期选择类似，存在 **`format` / `value-format`** 默认值约定，以及 **范围模式** 下「单字符串存储」与「数组」两种外向形态的切换诉求。

**目标**：在 **`ElTimePicker`** 上封装 **`C7TimePicker`**：

- 父级 **未** 传入 **`format` / `valueFormat`（含 `value-format`）** 时，注入默认 **`HH:mm:ss`**；
- 在 **`is-range` / `isRange`** 为真时，通过 **`rangeMerge`** 在 **合并字符串**（默认逗号分隔，可用 **`mergeDelimiter`** 配置）与 **EP 原生范围值**（一般为二元数组）之间切换；
- **`change`** 载荷与 **`update:modelValue`** 外向形态一致；**`blur` / `focus`** 与 EP 一致转发。

**验收**：**`is-range` + `rangeMerge=true`** 下，外部字符串 **`"08:00:00,18:00:00"`** 可回显、可编辑，并按配置输出。

---

## 2. 实现思路（已定稿）

采用与 **`C7DatePicker`** 同构的 **「计算属性 + `outerToInner` / `innerToOuter`」** 单数据流方案：内向仅绑定 EP 可接受的 **`model-value`**，避免 **`watch` 双状态** 带来的循环更新风险。不采用「仅在有合并串时介入」的薄封装，以免默认格式注入与 attrs 逻辑分散。

---

## 3. 命名与工程位置

| 项 | 约定 |
|----|------|
| 组件名 | **`C7TimePicker`** |
| 文件 | **`quick-ui/src/packages/C7TimePicker/index.vue`** |
| 导出 | 在 **`quick-ui/src/packages/index.js`** 中 **`import` / `export` / `app.component`** 与其它 C7 包一致 |

---

## 4. 根节点与透传

- 根节点：**`ElTimePicker`**。
- **`defineOptions({ name: 'C7TimePicker', inheritAttrs: false })`**。
- 根上使用经筛选的 **`v-bind`**：**不得**把本组件独占的 **`modelValue` / `rangeMerge` / `mergeDelimiter`** 再透传给 EP；**`model-value` / `@update:model-value`** 由包装层接管。
- 文档说明：**除下文「显式 props」外，其余属性与 Element Plus `el-time-picker` 一致**。
- **`visible-change`** 等其它 EP 事件：**不**要求在 **`defineEmits`** 中穷举；调用方可通过 **模板监听** 或 **attrs 监听器** 使用。

---

## 5. 显式 props（不占 attrs）

| Prop | 类型 / 默认 | 说明 |
|------|-------------|------|
| **`modelValue`** | — | **对外**绑定值。单值：与 EP 一致。**范围 + `rangeMerge`**：**`string \| null`**（两段由 **`mergeDelimiter`** 拼接）。**范围 + 非 merge**：EP 范围形态（一般为 **长度 2 的数组**）。 |
| **`rangeMerge`** | **`boolean`**，默认 **`false`** | **仅当** `is-range` / `isRange` 为 **`true`** 时生效；否则无效果。 |
| **`mergeDelimiter`** | **`string`**，默认 **`','`** | **仅用于存储串**的拼接与拆分；**与 EP `range-separator`（面板「起—止」展示）语义分离**。若传入 **`null`**、**`undefined`** 或 **空字符串**，**有效分隔符回退为 `','`**（与 **`C7DatePicker`** 对 **`mergeDelimiter`** 的回退规则一致）。 |

---

## 6. 默认 `format` / `valueFormat`

- 当 **`format`** 未定义，且 **`valueFormat` 与 `value-format` 均未定义** 时，向 EP 注入 **`format: 'HH:mm:ss'`**、**`valueFormat: 'HH:mm:ss'`**（占位符与 EP / dayjs 约定一致即可）。
- **任一已传**：不注入，行为与裸 **`ElTimePicker`** 一致。

实现可参考 **`C7DatePicker`** 的 **`pickerAttrs` `computed`**：从 **`attrs`** 拷贝后删 **`onUpdate:modelValue` 等监听键**，再按需合并默认值。

---

## 7. 范围模式判定

**范围模式**以根上传入的 **`is-range` 或 `isRange` 为 `true`** 为准（含 Vue 布尔空属性等价为 true 的写法以实现为准，与 **`C7TimePicker`** 现实现一致）。**非范围**时：**`rangeMerge` / `mergeDelimiter`** 无效果，**`outerToInner` / `innerToOuter`** 对单值直透传（除清空归一化见第 9 节）。

---

## 8. `outerToInner`（外向 → EP 内向）

记 **`effectiveDelimiter`**：与 **`mergeDelimiter`** 规则一致，空则 **`','`**。

**非范围**：直接返回 **`outer`**，与 **`C7DatePicker`** 非范围分支一致（**不**对 **`''`** 做额外归一化，由 EP 处理）。

**范围 + `rangeMerge`：**

| 外向 `outer` | 内向结果 |
|--------------|----------|
| **`null` / `undefined` / `''`** | **`null`** |
| **`string`** | 若不包含 **`effectiveDelimiter`** → **`null`** + **`console.warn`**（键去重，避免刷屏）。否则 **`split(delim)` → 两段 `trim`**：恰好两段且均非空 → **`[a, b]`**；否则 **`null` + warn**。 |
| **`Array`** | 长度 **2** → 原样返回；否则 **`null` + warn**。 |
| **其它类型** | **`null` + warn**。 |

**范围 + 非 `rangeMerge`（与 `C7DatePicker` 的差异点，已定稿 4A）：**

- **`C7DatePicker`**：只要是 **范围 `type`**，外向为 **`string`** 时 **一律**按 **`effectiveDelimiter`** 拆成两段数组供 EP 回显（与 **`rangeMerge` 取值无关**；**`rangeMerge` 仅影响 `innerToOuter` 是否合并回单串**）。
- **`C7TimePicker`**：**仅接受长度 2 的数组**；外向为 **`string`** 时 **不**按存储串拆分 → **`null` + `console.warn`**（避免在「外向应为数组」的契约下静默误解析）。业务若需从合并串回显，**必须** **`rangeMerge=true`**（或在上层先拆成数组再传入）。

---

## 9. `innerToOuter`（EP 内向 → 外向）

**非范围**：返回 **`inner`**。

**范围**：若 **`inner == null`** → 外向 **`null`**。若 **非数组** 或 **长度不为 2** → 外向 **`null`**（与 **`C7DatePicker`** 一致，不额外 warn，避免与 EP 内部瞬时状态打架）。

**范围 + `rangeMerge`：** 设两段为 **`a`**、**`b`**（与 **`C7DatePicker`** 相同逻辑）：

- 若 **`a == null && b == null`** → **`null`**；
- 否则 **`String(a 或 '') + effectiveDelimiter + String(b 或 '')`**（**`null` 段以空串参与拼接**，与 **`C7DatePicker`** 的 **`innerToOuter`** 一致）。

**范围 + 非 `rangeMerge`**：返回 **`inner`** 数组。

---

## 10. 事件

| 事件 | 约定 |
|------|------|
| **`update:modelValue`** | 载荷为 **`innerToOuter`** 后的外向值。 |
| **`change`** | 载荷与 **`update:modelValue`** **同形态**（含清空为 **`null`**）。 |
| **`blur` / `focus`** | 与 EP 一致，原样 **`emit`**。 |

---

## 11. 日志与调试

非法合并串、非法数组长度、不支持的类型：使用 **`console.warn`**，前缀 **`[C7TimePicker]`**，并对 **`warnParseOnce`** 采用与 **`C7DatePicker`** 类似的 **key 去重**，避免 **`computed`** 重复触发刷屏。

---

## 12. 测试建议（实现阶段）

1. **默认格式**：不传 **`format` / `value-format` / `valueFormat`** 时，行为符合 **`HH:mm:ss`** 注入预期。  
2. **范围 + `rangeMerge`**：外向 **`"08:00:00,18:00:00"`** 回显；修改后仍为合并串；**`mergeDelimiter`** 非逗号时拆拼正确。  
3. **范围 + 非 `rangeMerge`**：二元数组与 EP 一致；**误传合并串** → 内向 **`null`** 且 warn（与第 8 节一致）。  
4. **非法合并串 / 非法数组**：内向 **`null`**，warn 可观测且不过度重复。  
5. **非范围**：**`rangeMerge=true`** 不产生字符串合并。  
6. **与 `range-separator`**：面板展示分隔符与 **`mergeDelimiter`** 存储串互不影响。

---

## 13. 范围与非目标

- **本期不包含**：日期时间混合选择（由 **`C7DatePicker`** 等承担）。  
- **不修改**：**`C7DatePicker`** 源码；本文档 **显式记录** 时间侧与日期侧在 **范围外向 `string` 解析** 上的差异，避免读者误以为两组件 **`outerToInner` 完全一致**。

---

## 14. 原始需求 · OpenSpec · 实现 — 对照（澄清 1B）

| 维度 | 原始需求 `C7时间选择器.md` | OpenSpec `openspec/changes/ui-c7-time-picker/specs/...` | 实现 `C7TimePicker/index.vue` |
|------|---------------------------|--------------------------------------------------------|------------------------------|
| 默认格式 | `HH:mm:ss` | 未传 format/valueFormat 时注入 **`HH:mm:ss`** | **一致** |
| 范围 + 合并外向 | `rangeMerge=true` 逗号串等 | `rangeMerge` + `mergeDelimiter`，拆分/拼接规则 | **一致** |
| 范围 + 非合并外向 | 「否则输出数组」 | `rangeMerge=false` 时数组；**外向 `string` 不拆分** | **一致** |
| 事件 | `update:modelValue`、`change`/`blur`/`focus` | 同设计 + `change` 与 v-model 同形态 | **一致** |
| 与 `C7DatePicker` | 未写细 | Spec 写明 **`rangeMerge=false` 不解析 `string`** | **一致**；与 **日期组件实现** 在「范围 + 外向 `string`」上 **不同**（见第 8 节） |

**结论**：当前实现与 OpenSpec、原始需求验收口径 **一致**；与 **`C7DatePicker`** 的差异为 **已定稿的有意行为**，评审与文档以本文第 8 节为准。

---

## 15. 后续流程

设计文档经 **你审阅确认无修改** 后，使用 **`writing-plans`** 产出可执行实现计划（若实现已全部完成，则计划可退化为「核对测试与文档链接」类条目）。实现完成后按需补充 **`docs/docs/frontend/components`** 与 OpenSpec 归档流程（若仓库流程要求）。

---

**自检（占位 / 矛盾 / 范围）**：无 TBD；第 8 节与第 14 节相互支撑、无「与日期侧相同」的误导表述；范围聚焦 **`C7TimePicker`**，不含后端与其它组件改造。
