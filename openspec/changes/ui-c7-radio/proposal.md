## Why

单选场景在业务表单与字典展示中重复实现「静态列表 / 挂载后异步字典 / 字段映射与样式切换」，与 **`C7Select`** 的数据语义分裂会导致调用方心智成本上升。原始说明见 **`原始需求/前端/C7单选框.md`**。

## What Changes

- 在 **quick-ui** 提供 **`C7Radio`**：基于 **Element Plus `ElRadioGroup` + `ElRadio` / `ElRadioButton`**，统一 **数据来源与解析链**（与 **`C7Select`** 对齐：**`dataList` / `options` 别名、`dataList` 优先**、**`fetchData(mergedParams)`**、**`fetchParams`、`resultKey`、`dataFormatter`**；并支持原始需求中的 **`labelKey` / `valueKey`** 以适配非 `{ label, value }` 行结构）。
- **外观**：**`radioStyle`** = **`default` | `button` | `border`**，分别映射普通 **`ElRadio`**、**`ElRadioButton`**、带 **`border`** 的 **`ElRadio`**。
- **首次加载**：不提供 **`remote` / `query`** 语义（单选组无搜索）；在 **`fetchData`** 路径下由 **`autoLoad`** 控制是否在挂载后自动拉取 **不含 `query` 键** 的 **`fetchData({ ...fetchParams })`**（默认值见 **design**）。
- **可配置行为**（见 **spec**）：**`fetch` 失败**、**空选项展示**、**当前值不在选项中** 等边界均由 **props / 插槽** 显式配置，避免隐式魔法。
- **根级透传**：除保留字段外，**`$attrs` 透传至 `ElRadioGroup`**（**`size`、`disabled`、`fill`、`text-color`** 等与 EP 文档一致），与 **`C7Select`** 的 **`inheritAttrs: false` + 计算转发** 模式一致。
- **表单**：与 **Element Plus `el-form` / `el-form-item`** 的常规用法一致（**`v-model` 挂在 `C7Radio` 上**；**`el-form-item` 的 `prop` 指向同一字段**；校验规则与 **`ElRadioGroup`** 无差异）。
- **事件**：**`update:modelValue`**、**`change(value)`**；可选 **`loading-change(loading)`** 与 **`defineExpose({ loading, reload })`** 与 **`C7Select`** 对齐，便于表单页统一监听加载态。

## Capabilities

### New Capabilities

- **`ui-c7-radio`**：**`C7Radio`** 的数据来源优先级、**`fetchData` / `fetchParams`** 合并规则、**`response.data` → `resultKey` → `dataFormatter` → `labelKey`/`valueKey` 行映射**、**`radioStyle`**、**attrs 透传**、**`autoLoad`**、**失败 / 空列表 / 非法当前值** 的可配置策略与验收要点。

### Modified Capabilities

- （无）新增前端 packages 能力；不修改后端规格。

## Impact

- **代码**：新增 **`quick-ui/src/packages/C7Radio/index.vue`**；修改 **`quick-ui/src/packages/index.js`**（导出 + **`installPackages`** 注册）。
- **文档**：本目录 **`proposal` / `design` / `tasks` / `specs/ui-c7-radio/spec.md`**；实现阶段可增补 Dev 演示页（见 **tasks**）。
