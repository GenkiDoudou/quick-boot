## 1. 核心组件与注册

- [x] 1.1 新增 `quick-ui/src/packages/C7Watermark/buildWatermarkPattern.js`：实现离屏 Canvas **tile** 生成、**`dataURL`** 与尺寸元数据；**文本多行**、**旋转/间距/透明度** 等参数；导出 **JSDoc** 完备的纯函数
- [x] 1.2 新增 `quick-ui/src/packages/C7Watermark/index.vue`：**容器 / 全屏** 布局、**`fullscreenScope`**（**`viewport` / `document`**）、**`image` 优先 + `crossOrigin` + 失败回落 `text`**、**`disabled`**、**`effectiveTamperResistant`**（**`tamperResistant` 显式优先于 `editable`**）与 **MutationObserver** 恢复、**rAF/防抖** 合并重绘、**image load token**、卸载清理 **`$attrs` 根透传 + 水印层 `pointer-events: none`**
- [x] 1.3 修改 `quick-ui/src/packages/index.js`：**export `C7Watermark`** 并在 **`installPackages`** 中全局注册

## 2. 与规格对齐校验

- [x] 2.1 对照 `openspec/changes/ui-c7-watermark/specs/ui-c7-watermark/spec.md` 自测：**文本/图片/回落**、**两种 `fullscreenScope` 长页**、**防删开/关**、**`tamperResistant=false` + `editable=false` 不恢复**、**`disabled` 无 observer**、**卸载无泄漏**
- [x] 2.2 `pnpm build:prod`（在 `quick-ui` 目录）通过

## 3. 文档与 Dev

- [x] 3.1 VitePress：新增 **C7Watermark** 说明页（**props 表**、**`crossOrigin`**、**防删优先级**、**容器/全屏**、**Layout 示例片段**）；更新 `docs/.vitepress/config/sidebar.ts`（或项目等价配置）侧栏入口
- [x] 3.2 可选：新增 `quick-ui/src/views/dev/C7WatermarkE2E.vue` 并注册 dev 路由，覆盖 **文本/图片**、**`fullscreenScope`**、**防删** 手动验收
