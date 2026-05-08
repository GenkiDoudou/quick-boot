## Why

列表与详情中附件常以 **逗号分隔 URL** 存储，需统一支持 **图片大图预览、视频弹窗播放、文件新窗口打开**，并支持 **直接铺开展示 / 按钮聚合 / 表格列表** 等触发方式；各页面自行拼 `el-image`、`el-dialog` 与 `window.open` 易分裂且难保证关闭暂停等行为一致。设计依据见 `docs/superpowers/specs/2026-05-08-c7-preview-design.md` 与 `原始需求/前端/C7预览.md`。

## What Changes

- 在 **quick-ui** 新增 **`C7Preview`**：解析 **`urls`**（逗号分隔、`trim`、过滤空串）；**`autoDetect` / `displayType`** 控制每条资源类型；**`coverType`**：`none` | `button` | `file`。
- **图片**：`el-image` + **`preview-src-list`** / **`initial-index`**；`button` 模式可用隐藏 `el-image` 触发预览（见 design）。
- **视频**：**`C7Dialog`**（`footer=false`）内 **`<video controls autoplay>`**；关闭时 **pause、`currentTime=0`、emit `close`**；多视频时在弹窗内 **上一条/下一条**（见 spec）。
- **文件**：**`window.open(..., '_blank', 'noopener,noreferrer')`**。
- **`coverType=file`**：**`el-table`** 极简行点击，与同索引 **`none`** 行为一致。
- **钩子**：**`onPreview(url, index)`** 支持 **`boolean | Promise<boolean>`**；**先钩子再 `emit('preview')` 再打开 UI**；拦截时不 **`emit('preview')`**。
- **工具模块**：同包 **`parseUrls.ts`**、**`inferMediaKind.ts`**（纯函数，可单测）。
- 在 **`quick-ui/src/packages/index.js`** 中 **export** 与 **`installPackages`** 注册 **`C7Preview`**。
- 可选：**`C7PreviewE2E`** 与文档站组件页（见 tasks）。

## Capabilities

### New Capabilities

- **`ui-c7-preview`**：**`C7Preview`** 的 **props（`urls`、`width`/`height`、`displayType`、`autoDetect`、`coverType`、`previewText`、`videoDialogTitle`、`onPreview` 等）**、**三种 `coverType` 与混合 URL（`autoDetect`）**、**图片列表顺序与 `initial-index`**、**视频弹窗与多视频导航**、**钩子/事件顺序**、**与 Element Plus / `C7Dialog` 的边界** 及验收标准。

### Modified Capabilities

- （无）新增前端 packages 能力；不修改既有主规格中的无关条目。

## Impact

- **代码**：新增 `quick-ui/src/packages/C7Preview/`（`index.vue`、`parseUrls.ts`、`inferMediaKind.ts`）；修改 `quick-ui/src/packages/index.js`。
- **文档**：本变更目录下 `proposal.md`、`design.md`、`tasks.md`、`specs/ui-c7-preview/spec.md`。
- **依赖**：现有 **element-plus**、项目内 **`C7Dialog`**；不新增 npm 包（除非实现评审发现缺口）。
