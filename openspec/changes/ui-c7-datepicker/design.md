## Context

- **quick-ui**：Vue 3 + Element Plus；业务在日期范围上重复处理 **格式默认值** 与 **字符串 / 数组** 互转。
- **原始需求**：**`原始需求/前端/C7日期选择器.md`**。
- **已定稿设计**：**`docs/superpowers/specs/2026-05-07-c7-datepicker-design.md`**（Q&A：**1:B 2:C 3:B 4:A 5:A 6:C 7:B**；**路线Ⅰ**）。

## Goals / Non-Goals

**Goals**

- **薄封装**：根 **`ElDatePicker`**，**`inheritAttrs: false` + `v-bind="$attrs"`**；仅对 **`modelValue` / `update:modelValue`** 做归一化与默认格式注入。
- **默认映射**：覆盖 **date / daterange / datetime / datetimerange / month / monthrange / year / yearrange / week**；**`datetime*`** 默认 **`valueFormat`：`YYYY-MM-DD HH:mm:ss`**；**未命中映射**不注入 **`format`/`valueFormat`**。
- **范围**：**`rangeMerge` 默认 `false`**；**`mergeDelimiter` 默认 `','`** 专管存储串；**`rangeSeparator` 仅 EP 展示**。
- **脏数据**：非法合并串 → **空值 + `console.warn`**。

**Non-Goals**

- 不规定业务接口、时区策略（由调用方 **`valueFormat`** 与 EP 能力解决）。
- 不在 v1 引入独立于 EP 的日历算法。

## Decisions（已与需求方确认）

1. **`rangeMerge` 默认 `false`**：范围默认对外 **数组**；需单串存储时显式 **`rangeMerge=true`**。
2. **`mergeDelimiter` vs `rangeSeparator`**：**存储**用 **`mergeDelimiter`**；**UI「至」文案** 仍用 EP **`rangeSeparator`**，二者不混用。
3. **非法合并串**：归 **空** + **`console.warn`**；不新增专用错误 **`emit`**。
4. **未映射 `type`**：不覆盖 EP 默认 **`format`/`valueFormat`**。
5. **实现结构**：**路线Ⅰ**，单 SFC 为主；映射可抽小模块若文件过长。
