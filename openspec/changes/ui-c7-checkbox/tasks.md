## 1. 包结构与入口



- [x] 1.1 在 **`quick-ui/src/packages/C7Checkbox`** 新增 **`index.vue`**（或同目录拆分子组件，导出默认 **`C7Checkbox`**），补齐 **JSDoc**（props、emit、`defineExpose` 契约）

- [x] 1.2 在 **`quick-ui/src/packages/index.js`** 导出并 **`installPackages`** 全局注册 **`C7Checkbox`**



## 2. 数据与解析链（对齐 C7Select）



- [x] 2.1 实现 **`dataList` 优先**；否则 **`fetchData(mergedParams)`** + **`autoLoad`** 挂载拉取；无 **`fetchData` 且 `autoLoad=true`** 时按 spec 策略处理（**no-op + dev warn**）

- [x] 2.2 实现 **`response.data` → `resultKey` → `dataFormatter` → `labelKey`/`valueKey`**，**value 一律 `String()`**；必要时复用或对照 **`C7Select`** 实现避免行为漂移

- [x] 2.3 实现 **`loading` / `loading-change` / `reload()``**；**存在静态 `dataList` 时 `reload()`** 行为与 **JSDoc** 一致



## 3. v-model 与事件



- [x] 3.1 实现 **`joinValue`** 下 **逗号串 ↔ `string[]`** 与 **空值**策略（与 **`C7Select` separator 空值**二选一对齐并注释）

- [x] 3.2 **`update:modelValue`** 与 **`joinValue` 对齐**；**`change` 始终 `string[]`**；外部 **`modelValue`** 支持 **串/`string[]`** 规范化



## 4. 全选、约束与样式



- [x] 4.1 实现 **`showSelectAll`** 与 **`indeterminate` 别名**（语义以 **design** 为准：**`showSelectAll || indeterminate`**）

- [x] 4.2 实现 **半选**与 **「全选」仅选中未禁用项**；当 **`selectableCount > max`** 时 **「全选」disabled**

- [x] 4.3 实现 **`min`/`max`/`disabled`** 与选项级禁用对全选的影响

- [x] 4.4 实现 **`checkboxStyle`**（**`default`/`button`/`border`**）到 **Element Plus** 的映射



## 5. 验证与演示



- [x] 5.1 新增 **`quick-ui` Dev 页**（如 **`src/views/dev/C7CheckboxE2E.vue`**）及 **`router`** 条目，覆盖：**逗号串回显**、**异步解析链**、**全选/半选**、**`max` 下全选禁用**、**`change` 为数组**

- [x] 5.2 **本地手动验收**：对照 **`openspec/changes/ui-c7-checkbox/specs/ui-c7-checkbox/spec.md`** 中 **ADDED** 场景逐项勾选（可选：后续再补 Playwright 与 **`openspec-test-cases`** 清单）；本次已 **`pnpm build:prod`** 通过作为构建侧验收


