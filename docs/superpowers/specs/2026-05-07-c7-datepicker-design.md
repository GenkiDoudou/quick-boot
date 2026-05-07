# C7DatePicker（C7 日期选择器）设计说明

**日期**：2026-05-07  
**状态**：已定稿（经 brainstorming 澄清与确认）  
**依据**：`原始需求/前端/C7日期选择器.md` + Q&A：**1:B 2:C 3:B 4:A 5:A 6:C 7:B**；实现结构：**路线Ⅰ**（薄封装）

---

## 1. 背景与目标

Element Plus **`ElDatePicker`** 在不同 **`type`** 下默认 **`format` / `value-format`** 不统一；范围选择时业务常需 **单字符串存储**（如 `2024-01-01,2024-12-31`），亦需可选 **数组输出**。目标：提供 **`C7DatePicker`**，在 **`ElDatePicker`** 上完成 **type → 默认格式映射**、**范围值合并/拆分**、**`update:modelValue` 与 `change` / `blur` / `focus` 透传**。

---

## 2. 组件边界与对外 API

### 2.1 命名与注册

- 组件名：**`C7DatePicker`**
- 位置：`quick-ui/src/packages/C7DatePicker/index.vue`（与其它 C7 包一致）
- 在 **`quick-ui/src/packages/index.js`** 中注册并导出。

### 2.2 根节点与属性透传（路线Ⅰ）

- 根节点为 **`ElDatePicker`**。
- **`defineOptions({ name: 'C7DatePicker', inheritAttrs: false })`**
- 根 **`ElDatePicker` 上使用 `v-bind="$attrs"`**  
  文档说明：**除下文「显式 props」外，其余属性与 Element Plus `el-date-picker` 一致**（如 **`type`**、**`disabled`**、**`placeholder`**、**`rangeSeparator`**（**仅**面板展示「起—止」文案）、**`shortcuts`** 等）。

### 2.3 显式 props（不占 attrs）

| Prop | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| **`rangeMerge`** | `boolean` | **`false`**（**Q1:B**） | 范围类 **`type`** 下：为 **`true`** 时，对外 **`modelValue`** 为 **单字符串**（两段值用 **`mergeDelimiter`** 连接）；为 **`false`** 时，对外与 EP 一致（一般为 **二元数组**，元素形态由 **`valueFormat`** / EP 行为决定）。非范围 **`type`** 时该 prop **无效果**，实现 **MUST** 在 JSDoc 说明。 |
| **`mergeDelimiter`** | `string` | **`','`**（**Q2:C**） | **仅**用于 **存储用合并串** 的拼接与拆分；**与** EP 的 **`rangeSeparator`（UI 文案）** **语义分离**，**不得**用 **`rangeSeparator`** 兼作存储分隔符。 |

调用方若显式传入 **`format`** 或 **`valueFormat`**（经 attrs 或等价绑定），**MUST 优先于** 本节内置映射（覆盖映射结果）。

### 2.4 事件

- **`update:modelValue`**：载荷为 **归一化后的对外值**（见第 4 节）。
- **`change`**、**`blur`**、**`focus`**：**透传** EP；**`change`** 载荷 **MUST** 与 **当前对外语义**一致（即与 **`update:modelValue`** 将提交的值 **同形态**），须在 JSDoc 写明与 EP 原始 **`change`** 在边界场景（如清空）下的对齐方式。

---

## 3. `type` → 默认 `format` / `valueFormat`（**Q4:A**、**Q5:A**、**Q6:C**）

以下映射仅在调用方 **未** 传入对应 **`format` / `valueFormat`** 时注入。字符串格式采用 **dayjs 风格**（与 Element Plus 2.x 常见约定一致）。

| `type` | 默认 `format`（展示） | 默认 `valueFormat`（绑定值） |
|--------|------------------------|------------------------------|
| **`date`** | `YYYY-MM-DD` | `YYYY-MM-DD` |
| **`daterange`** | `YYYY-MM-DD` | `YYYY-MM-DD` |
| **`datetime`** | `YYYY-MM-DD HH:mm:ss` | `YYYY-MM-DD HH:mm:ss` |
| **`datetimerange`** | `YYYY-MM-DD HH:mm:ss` | `YYYY-MM-DD HH:mm:ss` |
| **`month`** | `YYYY-MM` | `YYYY-MM` |
| **`monthrange`** | `YYYY-MM` | `YYYY-MM` |
| **`year`** | `YYYY` | `YYYY` |
| **`yearrange`** | `YYYY` | `YYYY` |
| **`week`** | 与 **EP 文档**中 **`week`** 类型推荐展示格式对齐（实现时以当前 **Element Plus** 版本文档为准，在 JSDoc 写死实际字符串） | 与 **EP** 在 **`value-format`** 下对 **`week`** 的返回值约定对齐（**MUST** 在 JSDoc 写死） |

**未命中上表**的 **`type`**（**Q4:A**）：**不**向 EP 注入 **`format` / `valueFormat`**，完全沿用 **EP 默认**。

---

## 4. 范围值的合并、拆分与脏数据（**Q2:C**、**Q3:B**）

### 4.1 输出（`rangeMerge`）

- **`rangeMerge === true`**：对外 **`modelValue`** 为 **`string`**，形如 **`start + mergeDelimiter + end`**（两段均为已按 **`valueFormat`** 格式化后的字符串；若 EP 在某种配置下返回 `null` 空一端，**MUST** 在 JSDoc 定义拼接规则，建议空端用空串且仍保留分隔符或整体 `''`，**二选一写死**）。
- **`rangeMerge === false`**：不合并；对外为 **EP 原生范围值**（通常为长度 2 的数组）。

### 4.2 输入（回显）

- **`daterange`（及同类范围 type）** 下，若父级传入 **单字符串** 且包含 **`mergeDelimiter`**（或当 **`mergeDelimiter` 为默认逗号** 时，兼容 **`,`**），**MUST** 拆分为 **EP 可接受的内部数组** 以正确回显并可编辑（对齐原始需求验收：**`"a,b"`**）。
- 拆分后 **无法解析为合法范围**（非法日期、缺一段、多于两段等）（**Q3:B**）：对外可视为 **空范围**；内部归一为 **`null`** 或 EP 接受的清空形态；**`console.warn`** 一次（文案含原始串摘要即可），**不**向父组件抛专用错误事件。

### 4.3 `mergeDelimiter` 约束

- **MUST NOT** 为空字符串（实现可在 **JSDoc** 声明非法时回退 **`','`** 或拒绝更新，**二选一写死**）。

---

## 5. 方案结论（实现结构）

- **路线Ⅰ**：单文件 **`C7DatePicker/index.vue`** 为主；**`modelValue` / `update:modelValue`** 与 EP 之间做 **归一化/反归一化**；**映射表**可为模块内常量或同目录 **`defaults.ts`**（若单文件过长再抽离）。

---

## 6. 测试与验收

- **`daterange`** + **`rangeMerge=false`**：外部 **`"2024-01-01,2024-12-31"`**（或按 **`mergeDelimiter` 自定义**）可 **回显** 并可编辑；输出为 **数组**。
- **`daterange`** + **`rangeMerge=true`**：选择范围后对外为 **单字符串**，分隔符为 **`mergeDelimiter`**。
- **`format`/`valueFormat` 显式传入**：**覆盖** 内置映射。
- **未映射 `type`**：**不**注入默认格式，行为与裸 **`ElDatePicker`** 一致。
- **非法合并串**：归空 + **`console.warn`**（**Q3:B**）。
- **`change` / `blur` / `focus`**：可触发且与 EP 文档不冲突。

---

## 7. 文档与 OpenSpec（**Q7:B**）

- 本文件：**`docs/superpowers/specs/2026-05-07-c7-datepicker-design.md`**
- 变更跟踪：**`openspec/changes/ui-c7-datepicker/`**（**`proposal.md` / `design.md` / `tasks.md` / `specs/ui-c7-datepicker/spec.md`**）
- 实现阶段：按需补 **`docs/docs/frontend/components/通用组件/c7-datepicker.md`** 与 **Dev 演示**。

---

## 8. 澄清记录（Q&A）

| 题号 | 结论 |
|------|------|
| 1 | **`rangeMerge` 默认 `false`**，范围默认对外 **数组**（与 EP 一致）。 |
| 2 | **存储分隔符**：**`mergeDelimiter`（默认 `,`）**；**`rangeSeparator` 仅 UI**，不与存储混用。 |
| 3 | **非法合并串**：**`null`/空范围 + `console.warn`**。 |
| 4 | **未命中映射的 `type`**：**不注入** `format`/`valueFormat`，用 EP 默认。 |
| 5 | **`datetime` / `datetimerange`** 默认 **`valueFormat`：`YYYY-MM-DD HH:mm:ss`**（及对应 `format`）。 |
| 6 | 映射表含 **常见 type + `week`**。 |
| 7 | **superpowers 设计说明 + OpenSpec 变更** `ui-c7-datepicker`。 |
| 结构 | **路线Ⅰ**：薄封装，根 **`ElDatePicker`** + **`modelValue` 归一化**。 |

---

## 9. 自检说明（定稿前已核对）

- **无 TBD**：**`week`** 的具体格式字符串在实现时以 **EP 版本文档**为准并在 JSDoc 写死。
- **一致性**：**`rangeSeparator`** 与 **`mergeDelimiter`** 职责分离；**`change`** 与对外值形态一致。
- **范围**：单组件 + 注册导出；不修改 **EP 源码**。
- **歧义**：**非法串**不抛业务事件，仅 **warn**；**`rangeMerge` 在非范围 type** 上无效并已说明。
