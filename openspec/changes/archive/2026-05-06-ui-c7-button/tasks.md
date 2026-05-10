## 1. 组件与注册

- [x] 1.1 新增 `quick-ui/src/packages/C7Button/index.vue`：`btnType` 预设、`clickFunction`、防抖（leading）、`busy` / `internalLoading` 语义、`validateRef`、`confirm` / `confirmFn`、`beforeClick`、`checkSuccess`、成功/失败提示 props
- [x] 1.2 `defineOptions({ inheritAttrs: false })`，透传非事件 attrs；过滤 `on*` 避免与内部点击重复
- [x] 1.3 `main.js` 通过 `@/packages` 的 `installPackages` 全局注册 `C7Button`

## 2. 与规格对齐校验

- [x] 2.1 对照 `openspec/changes/ui-c7-button/specs/ui-c7-button/spec.md` 核对 props/emit 命名与流水线顺序；若有偏差，改组件或改 spec（二选一保持一致）
- [x] 2.2 验收：`btnType=delete` + `confirm=true` → 先确认再执行 `clickFunction`
- [x] 2.3 验收：`clickFunction` 未完成前 **loading** 且 **`busy` 阻止重入**
- [x] 2.4 验收：`validate=true` 且校验失败 → **不**进入确认与 `clickFunction`，**无**错误 toast，`after-click(false)`

## 3. 工程与健康（可选）

- [x] 3.1 `pnpm run build:prod`：若因缺少 `terser` 失败，在 `package.json` 增加 `devDependency terser` 或将 `vite.config.js` 的 `minify` 改为 `esbuild`（与组件无关的构建修复）

## 4. 推广（可选）

- [x] 4.1 在任一现有页面（如 `views/system/user/index.vue`）将 1～2 个典型按钮替换为 `C7Button` 作为用法示例
