# C7Switch 开关

在 **`ElSwitch`** 上封装 **切换前校验**、**二次确认**、**可选异步提交**（失败保持原值）与 **字典优先** 的内外文案映射。

**源码**：`quick-ui/src/packages/C7Switch/index.vue`  
**Dev 页**：开发服务器启动后访问 `/dev/c7-switch-e2e`

## 功能概要

- **值体系**：**`v-model`**（**`modelValue`**）与 **`activeValue` / `inactiveValue`**。
- **文案**：**`dictList`** 与 **`activeValue`/`inactiveValue`** 使用 **`===`** 匹配；**字典命中优先**，否则 **`activeText` / `inactiveText`** 兜底。
- **流水线**：**`beforeChange(newVal)`** → **确认**（**`confirmFn` 优先**；否则 **`confirmMessage`（trim 非空）** 弹 **`ElMessageBox.confirm`**）→ 可选 **`asyncChange(newVal)`**（**loading**；失败 **不**更新值、**不** `emit('cancel')`）→ 可选 **`afterChange`**（**无 `asyncChange` 的同步成功也会调用**）。
- **静默**：**`beforeChange` 返回严格 `false`** 时 **完全静默**（**不** `emit('cancel')`）。
- **颜色**：**`activeColor` / `inactiveColor`** 映射为 **`--el-switch-on-color` / `--el-switch-off-color`**（Element Plus 2.10+）。
- **事件**：成功提交时 **先** **`update:modelValue`** **再** **`change(newVal, oldVal)`**；中止确认 **emit `cancel`**。

## 与全局注册

在 `main.js` 已调用 `installPackages(app)` 时，模板中可直接使用 `<c7-switch />`（或 `<C7Switch />`）。

## 相关规格

OpenSpec 变更：`openspec/changes/ui-c7-switch/specs/ui-c7-switch/spec.md`
