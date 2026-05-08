## 1. 工具模块与单测

- [ ] 1.1 新增 **`quick-ui/src/packages/C7Preview/parseUrls.ts`**：实现 **`parseUrls`**（**split / trim / 过滤空串**），附 **JSDoc**
- [ ] 1.2 新增 **`quick-ui/src/packages/C7Preview/inferMediaKind.ts`**：实现 **`inferMediaKind`**（pathname、去 query/hash、扩展名集合、大小写不敏感），附 **JSDoc**
- [ ] 1.3 为上述两模块增加 **Vitest / 项目既定单测框架** 用例：**逗号空格**、**空段**、**query 不误判**、**大写扩展名**

## 2. C7Preview 组件核心

- [ ] 2.1 新增 **`quick-ui/src/packages/C7Preview/index.vue`**：**`defineOptions({ name: 'C7Preview', inheritAttrs: false })`**；**props**：**`urls`**、**`displayType`**、**`autoDetect`**（默认 **`true`**）、**`coverType`**、**`width`/`height`**、**`previewText`**（默认 **`预览`**）、**`videoDialogTitle`**（默认 **`视频预览`**）、**`onPreview`**
- [ ] 2.2 实现 **`coverType=none`**：**image**（**`el-image` + `preview-src-list` + `initial-index`**）、**video**（占位 + 播放、**`C7Dialog` `footer=false`** + **`<video controls autoplay>`**）、**file**（图标+名、**`window.open`**）
- [ ] 2.3 实现 **`coverType=button`**：**`el-badge`** 数量；分支 **图优先 / 否则视频弹窗+多视频上一条下一条 / 否则 file 索引 0**；**隐藏 `el-image`** 触发大图（与当前 **EP** API 对齐）
- [ ] 2.4 实现 **`coverType=file`**：**`el-table`** 极简列 + 行点击与同索引 **`none`** 等价逻辑
- [ ] 2.5 实现 **`onPreview`**：**`boolean | Promise<boolean>`**；**先 await 钩子 → 通过则 `emit('preview')` → 再打开**；拦截时 **不** **`emit('preview')`**；视频关闭 **pause + 归零 + `emit('close')`**
- [ ] 2.6 **空 `urls` 解析结果**：在代码与 PR 说明中 **写死一种**（**不渲染** 或 **`button` 禁用**），并满足 **spec「空 urls」**

## 3. 注册与构建

- [ ] 3.1 修改 **`quick-ui/src/packages/index.js`**：**import / export / `installPackages`** 注册 **`C7Preview`**
- [ ] 3.2 运行 **`pnpm -C quick-ui build:prod`**（或仓库既定生产构建命令）通过

## 4. 规格对齐与 E2E（可选但推荐）

- [ ] 4.1 对照 **`openspec/changes/ui-c7-preview/specs/ui-c7-preview/spec.md`** 做手工或自动核对清单（多图、视频关闭、file、`onPreview` false、混合 **button** 优先图）
- [ ] 4.2 （可选）新增 **`C7PreviewE2E`** Dev 页与 Playwright 用例，与其它 C7 E2E 同模式

## 5. 文档（可选）

- [ ] 5.1 （可选）在 **`docs`** 侧增加 **`C7Preview`** 组件说明页并挂侧边栏（若仓库惯例要求同步）
