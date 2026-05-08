# C7Watermark 水印

在 **容器** 或 **全屏** 上叠加水印图案（离屏 Canvas 生成平铺 PNG，`background-repeat` 铺满）。**`image`** 优先，失败回落 **`text`**；可选 **MutationObserver** 在 DOM 被删后恢复水印层。

**源码**：`quick-ui/src/packages/C7Watermark/index.vue`、`quick-ui/src/packages/C7Watermark/buildWatermarkPattern.js`  
**Dev 页**：`/dev/c7-watermark-e2e`

## 功能概要

- **模式**：`fullscreen=false`（默认）为 **容器模式**：根节点 `position: relative`，默认插槽为业务内容，水印层 `absolute` 铺满且 **`pointer-events: none`**。`fullscreen=true` 时水印层 **`position: fixed`**，由 **`zIndex`** 控制层级。
- **`fullscreenScope`**：仅 **`fullscreen=true`** 时生效；**`viewport`**（默认）铺满视口；**`document`** 使用 **`document.documentElement.scrollHeight`** 作为水印层高度，并监听 **`resize` / `ResizeObserver`** 更新。
- **内容**：**`text`** 支持 `string` 或 `string[]`（多行）；**`image`** 为 URL 字符串，加载或 Canvas 导出失败时回落 **`text`**。
- **`crossOrigin`**：传给内部 `Image.crossOrigin`（如 **`'anonymous'`**、**`''`**）；外链需服务端正确 **CORS**，否则可能无法绘制图片。
- **`disabled`**：`true` 时不渲染水印层，不挂载防删与文档尺寸监听。
- **防删**：**`tamperResistant`** 显式传入时 **优先**；未传时 **`editable === false`** 等价开启防删；**`tamperResistant=false` + `editable=false`** 仍为关闭。

## 默认样式参数（与组件 JSDoc 一致）

| Prop | 默认值 | 说明 |
|------|--------|------|
| `fontSize` | `16` | 字号（px） |
| `fontColor` | `'rgba(0, 0, 0, 0.15)'` | 文本颜色 |
| `fontFamily` | `'PingFang SC, Microsoft YaHei, sans-serif'` | 字体栈 |
| `opacity` | `1` | 画布全局透明度 0~1 |
| `rotate` | `-22` | 旋转角（度） |
| `gapX` / `gapY` | `100` / `100` | 平铺单元间距（px） |
| `width` / `height` | `160` / `80` | 单格内容区（px） |
| `offsetX` / `offsetY` | `0` / `0` | 单元内平移（px） |
| `zIndex` | `4100` | 叠放层级 |
| `fullscreenScope` | `'viewport'` | 见上文 |

## 与全局注册

在 `main.js` 已调用 `installPackages(app)` 时，模板中可使用 `<c7-watermark />`（或 `<C7Watermark />`）。

## Layout 全屏示例（按需粘贴）

以下片段仅作参考：**不**强制修改业务 Layout；将 `userName` / `dept` 替换为实际 store 字段即可。

```vue
<template>
  <div id="app">
    <c7-watermark
        v-if="userStore.name"
        :fullscreen="true"
        fullscreen-scope="document"
        :text="watermarkLines"
        :editable="false"
        :z-index="4100"
    />
    <router-view />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/store/modules/user' // 路径按项目调整

const userStore = useUserStore()
const watermarkLines = computed(() => {
  const name = userStore.name || ''
  const dept = userStore.deptName || ''
  const t = new Date().toLocaleString()
  return [name, dept, t].filter(Boolean)
})
</script>
```

若仅需视口高度铺满，将 **`fullscreen-scope`** 改为 **`viewport`**。

## 相关规格

OpenSpec 变更：`openspec/changes/ui-c7-watermark/specs/ui-c7-watermark/spec.md`  
设计说明：`docs/superpowers/specs/2026-05-08-c7-watermark-design.md`
