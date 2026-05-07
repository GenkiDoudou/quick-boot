## 1. 包结构与入口

- [x] 1.1 新增 **`quick-ui/src/packages/C7Title/index.vue`**：根 **`div.c7-title`**、**`defineOptions({ name: 'C7Title', inheritAttrs: true })`**；完整 **JSDoc**（props、插槽、**`resolvedTag`** 计算规则、**`tag` 默认 `undefined`** 与 **`h4`** 回退说明）

- [x] 1.2 修改 **`quick-ui/src/packages/index.js`**：导出并 **`installPackages`** 注册 **`C7Title`**

## 2. 布局与样式

- [x] 2.1 实现标题行 **flex**：可选图标区、**`ElText`**（**`:tag="resolvedTag"`**）、**`#title`** 或 **`label`/`title`** 文案、默认插槽 **actions**（**`margin-left: auto`** 或等价）

- [x] 2.2 实现 **`labelSize`**：**`h1`~`h6`** 映射 **font-size/line-height**（对齐 EP 标题 token 或注释写明数值来源）；**自定义单位** 仅内联字号/行高；非法串 **dev warn** 与回退

- [x] 2.3 实现 **`resolvedTag`**：**`tag !== undefined`** → 用 **`tag`**；否则 **`labelSize`** 为 h 级 → 用 **`labelSize`**；否则 **`h4`**

- [x] 2.4 标题文本 **字重 600**；根节点 **`--c7-title-decoration-color`**（**`decorationColor` ?? `labelColor` ?? `var(--el-color-primary)`**）

- [x] 2.5 **`showBorder`**：为 **`true`** 时全宽底部线约 **2px**、颜色取 CSS 变量；**`false`** 不渲染

## 3. 图标与文案

- [x] 3.1 **`import * as ElementPlusIconsVue`**，按 **`icon`** 字符串解析；未知 **warn** 且不渲染；**`#icon`** 存在时完全覆盖

- [x] 3.2 **`label`/`title`**：**`label` 优先**；二者均非空时可选 **dev warn**；**`#title`** 覆盖字符串文案

## 4. 验证

- [x] 4.1 **`pnpm -C quick-ui build:prod`**（或项目等价构建）通过；必要时在现有 dev 演示页增加最小用例（若仓库已有 C7 演示路由则对齐其模式）
