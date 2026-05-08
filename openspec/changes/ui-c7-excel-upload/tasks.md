## 1. 组件 `C7ExcelUpload`

- [ ] 1.1 新建 **`quick-ui/src/packages/C7ExcelUpload/index.vue`**：**`defineProps` / `defineEmits`** 对齐 **`specs/ui-c7-excel-upload/spec.md`**（**`accept`、`maxSizeMb`、`duplicateStrategy` + `update:duplicateStrategy`、`overwriteLabel`、`ignoreLabel`、`uploadFn`、`notify`、`uploading` + `update:uploading`**）；隐藏 **`input[type=file]`**、选文件与导入用 **`ElButton`**、**`inheritAttrs: false`**、**`v-bind="$attrs"`** 与 **`class` / `style`** 合并策略同 **`C7ExcelDownload`**。
- [ ] 1.2 实现 **`change`** 流程：扩展名与 **`maxSizeMb`** 校验；失败路径 **`notify` / `ElMessage.error`**，**不**调用 **`uploadFn`**，**不** **`emit('error')`**；通过后暂存 **`File`** 并重置 **`input.value`**。
- [ ] 1.3 实现策略区：按 **`design.md` 决策 1** 选定 **`ElRadioGroup`** 或 **`ElSegmented`**，绑定 **`v-model:duplicateStrategy`**，默认 **`ignore`**；**`reset()`** 将策略复位为 **`ignore`**。
- [ ] 1.4 实现导入：**`uploading` try/finally**、**`emit('update:uploading')`** 与 **`v-model:uploading`** 同步；**`uploading === true`** 时导入点击 **no-op**；成功 **`emit('success', result)`** 并更新结果区；**`uploadFn` reject** 时 **`notify` + `emit('error', err)`**。
- [ ] 1.5 结果区：展示 **`total` / `successCount` / `failCount`**；**`failCount > 0` 且 `errorFileUrl` trim 非空** 时渲染 **`<a target="_blank" rel="noopener noreferrer">`**。
- [ ] 1.6 同文件底部（或紧邻区块）抽取 **纯函数**：扩展名判断、**MB→字节** 上限比较；**JSDoc（简体中文）** 覆盖 **`props` / `emit` / `expose` / `uploadFn` 契约** 与 **`reset`** 语义。

## 2. 注册与验证

- [ ] 2.1 修改 **`quick-ui/src/packages/index.js`**：**`export { C7ExcelUpload }`**（与现网导出风格一致）并在 **`installPackages`** 中 **`app.component('C7ExcelUpload', ...)`**。
- [ ] 2.2 在 **`quick-ui`** 目录执行 **`pnpm build:prod`**，无新增类型/构建错误。

## 3. 文档（可选）

- [ ] 3.1（可选）在 **`docs/`** 或 Dev 演示页增加 **`C7ExcelUpload`** 最小用法示例；若无集中文档习惯，在本条备注 **「跳过」** 并依赖代码评审。
