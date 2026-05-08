## Why

后台系统需在页面区域或全屏叠加水印（用户名、部门、时间等），降低截图泄露风险。仓库 **`quick-ui`** 的 C7 业务组件族中尚缺少统一水印能力；原始说明见 **`原始需求/前端/C7水印.md`**，已定稿设计见 **`docs/superpowers/specs/2026-05-08-c7-watermark-design.md`**。

## What Changes

- 新增 **`C7Watermark`**：容器模式（包裹内容叠加水印层）与全屏模式（`fixed`）；全屏支持 **`fullscreenScope`**：`viewport` | `document`。
- **绘制**：自研离屏 Canvas 生成重复 tile，导出 **`dataURL`** 应用于水印层背景（**不**封装 Element Plus `ElWatermark`）。
- **内容**：**`image`** 优先，失败回落 **`text`**（`string` | `string[]`）；**`crossOrigin`** 由业务配置以应对跨域图片。
- **行为**：**`disabled`** 时不渲染水印、不挂载监听；**`tamperResistant`** 与兼容 **`editable`**（**`editable === false`** 等价防删；二者同时存在时 **`tamperResistant` 优先**）；卸载时断开 **MutationObserver** 与 **resize / ResizeObserver** 等。
- **集成**：在 **`quick-ui/src/packages/index.js`** 导出并 **`installPackages`** 注册；VitePress 增加组件说明与 Layout 级全屏示例片段（**不强制**修改默认 Layout）。

## Capabilities

### New Capabilities

- **`ui-c7-watermark`**：**`C7Watermark`** 的布局模式、Canvas tile、文本/图片优先级与回落、样式与画布参数、**`fullscreenScope`**、**`crossOrigin`**、**`disabled` / `tamperResistant` / `editable`** 语义与优先级、防删恢复、生命周期与性能约束（合并重绘）、**`packages/index.js`** 注册与文档/演示页验收。

### Modified Capabilities

- （无）新增 packages 能力；不修改后端契约与既有 `openspec/specs/` 中其它 UI 规范的语义。

## Impact

- **代码**：新增 **`quick-ui/src/packages/C7Watermark/`**（**`index.vue`** + **`buildWatermarkPattern.js`** 或等价纯函数模块）；修改 **`quick-ui/src/packages/index.js`**。
- **文档**：本变更 **proposal / design / tasks** 与 **`specs/ui-c7-watermark/spec.md`**；**`docs`** VitePress 侧栏与 **`docs/docs/frontend/components`** 下水印说明页。
- **依赖**：现有 **Vue 3**；不新增 npm 包除非 **design** 论证必要。
