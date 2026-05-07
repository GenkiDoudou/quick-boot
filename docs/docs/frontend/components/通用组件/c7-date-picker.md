# C7DatePicker 日期选择器

在 **`ElDatePicker`** 上统一 **`type` → 默认 `format` / `valueFormat`**（未传时）、范围 **`rangeMerge`**（合并为单字符串或保持 EP 数组）与 **`mergeDelimiter`**（存储分隔符，与 EP **`rangeSeparator`** 分离）。

**源码**：`quick-ui/src/packages/C7DatePicker/index.vue`  
**Dev 页**：开发服务器启动后访问 `/dev/c7-datepicker-e2e`

## 功能概要

- **默认格式**：命中内置表（含 **`week`** → **`gggg-wo`**）时注入；父传入 **`format` / `value-format`** 则 **覆盖**；未命中 **`type`** **不注入**。
- **范围 `type`**：**`daterange` / `datetimerange` / `monthrange` / `yearrange`**。
- **`rangeMerge`**（默认 **`false`**）：**`true`** 时 **`v-model`** 为 **`mergeDelimiter`** 拼接的 **单字符串**；**`false`** 时为 **EP 范围数组**。
- **回显**：父级传 **`"开始,结束"`**（或自定义 **`mergeDelimiter`**）时拆成数组供面板编辑；非法串 **`console.warn`** 并清空为 **`null`**。
- **事件**：**`update:modelValue`**、**`change`**（载荷与对外 **`v-model`** 形态一致）、**`blur`**、**`focus`**。

## 与全局注册

在 `main.js` 已调用 `installPackages(app)` 时，模板中可直接使用 `<c7-date-picker />`（或 `<C7DatePicker />`）。

## 相关规格

- 设计说明：`docs/superpowers/specs/2026-05-07-c7-datepicker-design.md`
- OpenSpec：`openspec/changes/ui-c7-datepicker/specs/ui-c7-datepicker/spec.md`
