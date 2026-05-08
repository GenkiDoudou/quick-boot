## 1. 组件与注册

- [ ] 1.1 新增 **`quick-ui/src/packages/C7ViewSwitch/index.vue`**：**`defineOptions({ name: 'C7ViewSwitch' })`**；**`views` / `showIndexs`** 归一（**`views` 优先**）；**`modelValue`**、**`defaultView`**、**`showPageHeader`**、**`transition`**（**`boolean | string`**）；完整 **JSDoc**（props、emit、插槽、`defineExpose`、父级 **`v-model` 不入栈**、首次不 **`change`**）
- [ ] 1.2 实现历史栈（**`ref<string[]>`**）、**`switchTo`**（**`not-found`**、同值 **no-op**、压栈后 **`update:modelValue` + `change`**）、**`goBack`**（**`back`** / 空栈 **`closeIndex` / `defaultView` / `back-empty`**）
- [ ] 1.3 **`watch(modelValue)`**：父级驱动切换时**不**改栈；**`onMounted`/`nextTick`** 或等价机制抑制**首次** **`change`**
- [ ] 1.4 模板：**具名 scoped 插槽** **`#<name>`**（**`config, switchTo, goBack`**）、**`#empty`**、**`#header-content`**；可选 **`ElPageHeader`**（**`title`**、**`@back` → goBack**）；**`<Transition>`** 三态（默认 **`name=c7-view-switch`**）
- [ ] 1.5 **`defineExpose({ switchTo, goBack, currentConfig, viewHistory })`**：**`viewHistory`** 返回只读快照（新数组或 **`readonly`**）
- [ ] 1.6 修改 **`quick-ui/src/packages/index.js`**：**export** 与 **`installPackages`** 注册 **`C7ViewSwitch`**

## 2. 文档

- [ ] 2.1 新增 **`docs/docs/frontend/components/通用组件/c7-view-switch.md`**：基础用法、**`views`/`showIndexs`**、栈与 **`v-model`** 说明、事件表
- [ ] 2.2 更新 **`docs/.vitepress/config/sidebar.ts`**（与其它 C7 条目同级加入 **C7ViewSwitch** 链接）

## 3. 与规格对齐校验

- [ ] 3.1 对照 **`openspec/changes/ui-c7-view-switch/specs/ui-c7-view-switch/spec.md`** 自测：**`switchTo`**、**`goBack`**、**`not-found`**、**`back-empty`**、父级改 **`v-model`**、首次无 **`change`**、**`transition` 三态**、无匹配 **`#empty`**
- [ ] 3.2 核对当前 **`element-plus`** 版本中 **`ElPageHeader`** 的插槽与返回事件名，与实现一致并在注释中标明

## 4. 工程与健康

- [ ] 4.1 **`pnpm -C quick-ui build:prod`**（或仓库既定生产构建命令）通过
