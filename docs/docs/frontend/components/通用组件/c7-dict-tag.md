# C7DictTag 字典标签

只读组件：将 **`modelValue`**（单值、数组或含分隔符的字符串）按 **`options`** 映射为 **`ElTag`** 列表，支持 **`max` 折叠（`+N`）**、**`collapse` 与 Tooltip/Popover**、**未匹配展示** 与 **`dictType` → `ElTag.type`**。

**源码**：`quick-ui/src/packages/C7DictTag/index.vue`  
**Dev 页**：开发服务器启动后访问 `/dev/c7-dict-tag-e2e`

## 功能概要

- **`modelValue`**：**`number` / `string` / `array`**；**`string`** 在 **包含 `separator`** 时拆分（默认 **`,`**），**不去重**。
- **`options`**：行形态 **`{ label, value }`**，匹配 **`String(opt.value) === String(原子)`** 的首项。
- **`max > 0`**：仅对 **已匹配** 项编号；前 **`max`** 枚展示字典色 tag，溢出 **`+N`**（**`N = 已匹配总数 − max`**）；**`mk > max + 1`** 的位置不占位。
- **`showValue`**：未匹配时 **`true`** → **`type=info`** 展示原始值；**`false`** → 该位 **`-`**。
- **`collapse`**：**`true`** → **`+N`** 使用 **`ElTooltip`**；**`false`** → **`ElPopover`（click）** 展示溢出 **`label`** 列表。
- **`dictType`**：映射 **`ElTag.type`**；未知回退 **`primary`**。
- **`size` / `effect` / `round`**：透传至相关 **`ElTag`**。

## 与全局注册

在 `main.js` 已调用 `installPackages(app)` 时，模板中可直接使用 `<c7-dict-tag />`（或 `<C7DictTag />`）。

## 相关规格

OpenSpec 变更：`openspec/changes/ui-c7-dict-tag/specs/ui-c7-dict-tag/spec.md`
