## 1. 组件与注册



- [x] 1.1 新增 **`quick-ui/src/packages/C7Radio/index.vue`**：基于 **`ElRadioGroup`**，子项 **`ElRadio` / `ElRadioButton`** 由 **`radioStyle`** 决定；实现 **静态 `dataList` / `options`（`dataList` 优先）**、**`fetchData` + `fetchParams` + `resultKey` + `dataFormatter` + `labelKey` + `valueKey`**、**`autoLoad`**（挂载后 **无 `query`** 拉取）、**`fetchErrorBehavior` / `invalidModelBehavior` / `emptyDisplay` / `emptyText` / `suppressInvalidModelDevWarn`** 等 spec 所列配置项

- [x] 1.2 **根级透传**：**`inheritAttrs: false`**，除保留键外 **attrs → `ElRadioGroup`**

- [x] 1.3 **事件**：**`update:modelValue`**、**`change(value)`**、**`loading-change(loading)`**（与 **`C7Select`** 一致）

- [x] 1.4 **`defineExpose`**：**`loading`**、**`reload()`**；**`packages/index.js`** 导出并 **`installPackages`** 注册 **`C7Radio`**



## 2. 与规格对齐校验



- [x] 2.1 对照 **`openspec/changes/ui-c7-radio/specs/ui-c7-radio/spec.md`** 核对 **解析链、透传、枚举默认值、空态与失败态**

- [x] 2.2 验收：**`fetchData`** 成功后选项渲染；**切换选项** 与外部 **`v-model`** 同步

- [x] 2.3 验收：**`el-form-item` + `rules`** 下校验表现与 **`ElRadioGroup`** 官方用法一致



## 3. 工程与健康



- [x] 3.1 **`pnpm -C quick-ui build:prod`**（或仓库既定生产构建命令）通过



## 4. 可选：Dev



- [x] 4.1 增加 Dev 页：静态 / **`fetchData`** / **`radioStyle` 三种** / **失败与空列表** 配置演示，路由由实现者补充并在 PR 说明

