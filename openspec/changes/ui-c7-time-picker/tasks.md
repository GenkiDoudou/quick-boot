## 1. 包结构与注册

- [x] 1.1 新增 **`quick-ui/src/packages/C7TimePicker/index.vue`**：根 **`ElTimePicker`**、**`defineOptions({ name: 'C7TimePicker', inheritAttrs: false })`**，补齐 **`/** … */` JSDoc**（**`rangeMerge` / `mergeDelimiter` / `is-range`**、**`outerToInner` / `innerToOuter`**、非法值与 **`console.warn`** 去重策略）

- [x] 1.2 在 **`quick-ui/src/packages/index.js`** 中 **`import` / `export` / `app.component('C7TimePicker', …)`** 与现有 C7 包一致

## 2. 默认值与 attrs

- [x] 2.1 实现 **`pickerAttrs` `computed`**：从 **`useAttrs()`** 拷贝并移除 **`onUpdate:modelValue` / `onChange` / `onBlur` / `onFocus`**（及与包装层冲突的键），在 **`format` / `valueFormat` / `value-format` 均未定义**时注入 **`HH:mm:ss`**

- [x] 2.2 模板绑定：**`:model-value="innerModelValue"`**、**`@update:model-value` / `@change` / `@blur` / `@focus`** 按 **design** 与 **`C7DatePicker`** 对齐

## 3. 范围归一化

- [x] 3.1 实现 **`isRangeMode(attrs)`**（**`is-range` 或 `isRange` 为 true**）与 **`effectiveDelimiter()`**（空 **`mergeDelimiter`** 回退 **逗号**）

- [x] 3.2 实现 **`outerToInner` / `innerToOuter`** 及 **`warnParseOnce`**，行为与 **`openspec/changes/ui-c7-time-picker/specs/ui-c7-time-picker/spec.md`** 及 **`C7DatePicker`** 对照一致

- [x] 3.3 **`defineProps`**：**`modelValue`、** **`rangeMerge`、** **`mergeDelimiter`**；**`innerModelValue`** 为 **`computed(() => outerToInner(props.modelValue))`**

## 4. 验证与演示

- [x] 4.1 新增 Dev 页（如 **`quick-ui/src/views/dev/C7TimePickerE2E.vue`**）及 **`router/index.js`** 路由：覆盖 **默认格式**、**`is-range` + `rangeMerge` + `'08:00:00,18:00:00'` 回显**、**`mergeDelimiter` 非逗号**、**非法串 warn**、**非范围 + `rangeMerge` 无合并**

- [x] 4.2 本地执行 **`pnpm build:prod`**（在 **`quick-ui/`** 目录）作为构建验收；可选对照 **spec** 场景做手动勾选

## 5. 产物与已定稿设计对齐（文档）

- [x] 5.1 将 **`proposal.md` / `design.md` / `specs/ui-c7-time-picker/spec.md`** 与 **`docs/superpowers/specs/2026-05-07-c7-time-picker-design.md`** 对齐：修正「与 **`C7DatePicker`** 非 merge 不拆 **`string`**」等 **错误表述**；写明 **范围 + `rangeMerge=false`** 下 **`C7TimePicker`** **不**解析外向 **`string`**，及与 **`C7DatePicker`** 的 **有意差异**
