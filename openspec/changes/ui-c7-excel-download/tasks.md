## 1. `request.js`：可选返回 Blob + headers

- [ ] 1.1 在 **`quick-ui/src/utils/request.js`** 响应成功拦截器中，对 **`responseType === 'blob'`**（及 **`arraybuffer`** 若与现网一致）分支增加**显式开关**（如 **`returnBlobWithHeaders: true`**）：为 true 时 **`return { data: res.data, headers: res.headers }`**，否则保持 **`return res.data`**；确认 **`downloadRequest`** 可将该开关并入 **`config`**。
- [ ] 1.2 为 **`downloadRequest`**（或新增薄封装）补充 **JSDoc / 注释**：说明默认返回 **`Blob`**、启用开关时返回 **`{ data, headers }`**，避免调用方误用。
- [ ] 1.3 **回归**：全局搜索 **`downloadRequest`** 调用点，确认未传开关时行为与变更前一致。

## 2. 组件 `C7ExcelDownload`

- [ ] 2.1 新建 **`quick-ui/src/packages/C7ExcelDownload/index.vue`**：**`defineProps` / `defineEmits`** 对齐 **`specs/ui-c7-excel-download/spec.md`**（**`downloadFn`、`fileName`、`defaultFileName`、`notify`、`downloading` + `update:downloading`**）；根 **`ElButton`**、**`inheritAttrs: false`**、**`v-bind="$attrs"`**、**`:loading="downloading"`**、默认插槽与可选默认文案。
- [ ] 2.2 实现 **`downloadFn` 结果归一**（**`Blob` vs `{ data, headers }`**）、**文件名解析**（**`fileName` → `filename*` → `filename=` → `defaultFileName`**）及纯函数（同文件底部或紧邻区块），含 **RFC5987 `decodeURIComponent`** 与失败顺延。
- [ ] 2.3 实现 **Blob 校验**：与 **`blobValidate`** 等价；**`application/json`** 路径 **`text` + `JSON.parse`**，**`msg` / `errorCode`** 提示策略与 **`download()`** 对齐；**`emit('error')` + `notify` / `ElMessage.error`**。
- [ ] 2.4 实现 **`objectURL` → `<a download>` → `click()` → `revokeObjectURL`（rAF 或 `setTimeout(0)`）**；**`downloading` 的 try/finally**；**`downloading === true` 时点击 no-op**；成功 **`emit('success', fileName)`**。
- [ ] 2.5 **JSDoc（简体中文）**：**`props` / `emit` / `v-model:downloading`**、**`downloadFn` 契约**、与 **`request`** 开关的配合示例（注释或 README 片段二选一，以评审为准）。

## 3. 注册与验证

- [ ] 3.1 修改 **`quick-ui/src/packages/index.js`**：**`export { C7ExcelDownload }`**（或与现网导出风格一致）并在 **`installPackages`** 中 **`app.component('C7ExcelDownload', ...)`**。
- [ ] 3.2 **`pnpm build:prod`**（在 **`quick-ui`** 目录）通过，无新增 lint/类型错误（若项目已启用）。
- [ ] 3.3（可选）在现有 Dev 演示页或最小沙盒页手动验证：**仅 Blob**、**`{ data, headers }` + Content-Disposition**、**JSON Blob**、**下载中重复点击**；无页面则在本条注明「跳过」并依赖代码评审。
