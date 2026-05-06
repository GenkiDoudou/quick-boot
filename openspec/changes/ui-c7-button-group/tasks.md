## 1. 组件与注册



- [x] 1.1 新增 `quick-ui/src/packages/C7ButtonGroup/index.vue`：实现 **`mode`**（`inline` / `dropdown` / `auto`）、**`maxVisible`**、**`buttons[]`** 与默认插槽双模式、**`spacing`** / **`size`** / **`responsive`** class、**「更多」** 配置（**`moreText`** / **`moreIcon`** / **`moreButtonType`** / **`moreButtonPlain`** / **`trigger`**）

- [x] 1.2 折叠区内 MUST 使用 **`C7Button`** 触发命令（同 **`clickFunction`** 与确认链）；禁止仅用下拉菜单项绕过 **`C7Button`** 流水线

- [x] 1.3 暴露 **`forceUpdate()`**（`defineExpose`），并在 JSDoc 中说明插槽动态变更时的调用约定

- [x] 1.4 **`packages/index.js`**：导出并 **`installPackages`** 全局注册 **`C7ButtonGroup`**



## 2. 事件与规格对齐



- [x] 2.1 实现 **`before-command(item)`**、**`after-command({ item, success })`**（载荷形状须与 `openspec/changes/ui-c7-button-group/specs/ui-c7-button-group/spec.md` 一致）

- [x] 2.2 验证组级事件与 **`C7Button`** 的 **`before-click` / `success` / `error` / `after-click`** 均能被子按钮触发且不被吞掉

- [x] 2.3 固定 **emit 顺序**（建议：**`before-command` → `before-click` → … → `after-click` → `after-command`**），与 design 一致（依赖 **`C7Button.beforePipeline`**）



## 3. 验收与构建



- [x] 3.1 **`mode=auto`**、**`maxVisible=2`**、数据模式：多余按钮进「更多」，点击执行原 **`clickFunction`**（含 **`confirm`** 场景抽检）— Dev 页 `tc-grp-data-auto`

- [x] 3.2 插槽模式：3 个 **`<C7Button />`** 折叠与展示正确；动态增删后 **`forceUpdate()`** 生效 — Dev 页 `tc-grp-slot-auto`

- [x] 3.3 `vite build`（`quick-ui`）通过（使用 Node 22 运行 `node ./node_modules/vite/bin/vite.js build`；默认 PATH 下 `pnpm` 若绑定 Node 14 需升级或改用上述方式）



## 4. 可选：Dev / E2E



- [x] 4.1 扩展 Dev 页 `C7ButtonE2E.vue` 分区（`tc-grp-data-auto` / `tc-grp-slot-auto`），路由仍为 `/dev/c7-button-e2e`



