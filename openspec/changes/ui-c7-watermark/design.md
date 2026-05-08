## Context

- **quick-ui**：Vue 3 + Element Plus；**`packages`** 下已有多个 **`C7*`** 组件，经 **`installPackages`** 全局注册；尚无水印组件。
- **原始需求**：**`原始需求/前端/C7水印.md`**。
- **已定稿设计**：**`docs/superpowers/specs/2026-05-08-c7-watermark-design.md`**（澄清：1B 文档与 Layout 示例、2C **`fullscreenScope`**、3B **`crossOrigin`**、4C **`tamperResistant`/`editable`**、5A 自研 Canvas；实现路径 **B**：**`index.vue` + `buildWatermarkPattern.js`**）。

## Goals / Non-Goals

**Goals**

- 提供 **`C7Watermark`**：**容器模式**（根 `relative`、默认插槽为内容、水印层 `absolute` 铺满）与 **`fullscreen`**（水印层 `fixed` + **`zIndex`**）。
- **`fullscreenScope`**：**`viewport`**（视口）与 **`document`**（整页可滚动高度，随文档根尺寸更新）。
- **离屏 Canvas** 生成 **tile**，**`toDataURL`** 后水印层 **`background-repeat`** 铺满；**`image`** 优先，失败回落 **`text`**（**`string` | `string[]`**）。
- **`crossOrigin`** 暴露给业务；**`disabled`** 时不渲染、不监听；**`tamperResistant`** 与 **`editable`** 按 **spec** 优先级启用 **MutationObserver** 恢复水印。
- **性能**：图案相关 props 与窗口尺寸变化经 **`requestAnimationFrame` 合并**或短防抖，避免 **`toDataURL`** 风暴；**`image` URL** 变更用 **token** 忽略过期回调。

**Non-Goals**

- 密码学级防泄露、防专业截屏、服务端水印策略。
- 水印层上再放可穿透以外的可点击子元素（本期 **不**承诺）。
- 依赖 **Element Plus `ElWatermark`** 做封装（**5A** 为自研）。

## Decisions

### 1. 文件拆分：**`index.vue` + `buildWatermarkPattern.js`**

- **纯函数模块**负责：根据 **文本/图片/画布参数** 计算 **tile** 并返回 **`dataURL`** 与 **背景尺寸元数据**（或等价），**不**持有 DOM。
- **SFC** 负责：插槽与根布局、**`Image` 加载**、**observer**、**resize / ResizeObserver**、**watch** 合并重绘、**卸载清理**。

### 2. 防删语义：**`tamperResistant` 优先于 `editable`**

- **`effectiveTamperResistant`**：**若 `tamperResistant` 为 `boolean` 类型显式传入**（实现可用 **`!== undefined`** 判定「显式」），**以 `tamperResistant` 为准**；**否则** **`editable === false`** 时视为 **`true`**，其余为 **`false`**。
- **Observer 目标**：监视 **约定父节点**（组件根或包裹水印层的容器）子树，**水印层节点被移除或替换** 时 **重新插入并重绘**（具体选择以实现简洁为准，**JSDoc** 写明）。

### 3. **`attrs` 透传**：绑定 **根容器**

- **`defineOptions({ name: 'C7Watermark', inheritAttrs: false })`**，**根** **`div`** 上 **`v-bind="$attrs"`**（或与现有一致写法），保证 **`class`/`style`** 便于页面微调；水印层 **独立** **`pointer-events: none`**，**不**因根透传破坏点击穿透。

### 4. **`document` 全屏高度**

- 以 **`document.documentElement`**（或 **`document.body`** 与 design 一致的单选）**`scrollHeight`** 为主更新水印层高度；**`window` `resize`** + **`documentElement` `ResizeObserver`**（若可用）合并触发 **同一套** 布局更新逻辑。

## Risks / Trade-offs

| 风险 | 缓解 |
|------|------|
| **外链图片 CORS** 导致无法绘制 | **文档 + JSDoc** 说明 **`crossOrigin`**；失败 **回落文本** |
| **MutationObserver** 与业务频繁改 DOM **抖动** | 恢复路径 **debounce/rAF** 合并；**仅**在 **`effectiveTamperResistant`** 为真时启用 |
| **`toDataURL` 性能** | **rAF/防抖** 合并重绘；tile 保持 **小尺寸** |

## Migration Plan

- **新能力**：业务按需 **引入 `<C7Watermark>`** 或全局注册后使用；**无**数据迁移。

## Open Questions

- （无）默认值表在 **spec** 与实现 **JSDoc** 同步列出。
