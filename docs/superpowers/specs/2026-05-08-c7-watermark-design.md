# C7Watermark（C7 水印）设计说明

**日期**：2026-05-08  
**状态**：已定稿（经 brainstorming 澄清与「整稿确认」）  
**依据**：`原始需求/前端/C7水印.md` + Q&A 结论（1B、2C、3B、4C、5A）及路径 **B**（`index.vue` + 同目录纯函数模块）

---

## 1. 背景与目标

后台系统需在**页面区域或全屏**叠加水印（用户名 / 部门 / 时间等），降低截图泄露风险；支持**文本与图片**、可配置旋转 / 间距 / 透明度 / `z-index`，并可选**防删除**（DOM 被改后自动恢复）。

**非目标**：水印密码学强度、防专业截屏工具、服务端鉴权；本期不承诺「水印上再放可点击子元素」。

---

## 2. 组件边界

- **职责**：在**容器内**或**视口 / 文档级全屏**上叠一层**可穿透点击**的水印图案（Canvas 生成重复 tile 后应用到水印层）。
- **与 Element Plus**：**不自研替代 `ElWatermark` 的对外 API**；本期 **5A** 为**自研 Canvas 拼 tile**，不依赖 EP 水印组件封装。
- **集成（1B）**：交付组件 + **VitePress 文档**（含 **Layout 级全屏水印示例片段**）；**不强制**修改项目默认 Layout，由业务按需接入。

---

## 3. 技术选型与文件结构

- **实现路径**：**路径 B** — `quick-ui/src/packages/C7Watermark/index.vue` + 同目录 **`buildWatermarkPattern.js`**（或等价命名）承载**纯函数**（根据参数生成图案 `dataURL`、tile 尺寸等），便于阅读与单测；**不**引入独立 composable 目录以免 YAGNI。
- **绘制策略**：**离屏 Canvas** 生成**小块重复图案**，导出 **`dataURL`**，水印层使用 **`background-image` + `repeat`**（或等价）铺满；避免整页单张超大 Canvas。
- **图片与跨域（3B）**：通过 prop 暴露 **`crossOrigin`**（`''` / `'anonymous'` / `undefined` 等由业务传入）；加载或绘制失败时**回落为 `text`**。文档须说明：外链需服务端正确 CORS，否则可能无法读像素。

---

## 4. 布局模式（含 2C）

### 4.1 容器模式（默认）

- 根节点 **`position: relative`**，**默认插槽**为业务内容。
- 水印层子元素：**`position: absolute; inset: 0`**，叠在内容之上；**`pointer-events: none`**、**`user-select: none`**（默认定稿；若与极少数业务冲突，实现阶段可再评估是否增加 prop，**本设计默认不写**）。

### 4.2 全屏模式

- **`fullscreen === true`**：水印层 **`position: fixed`**，**`z-index`** 来自 prop。
- **`fullscreenScope`**（新增，对应 **2C**）：
  - **`'viewport'`**：覆盖当前视口（如 `inset: 0` 或等价 `100dvh` / `100vw` 语义），不随文档超出视口部分延伸。
  - **`'document'`**：覆盖**整份可滚动文档**高度（长页不露底）；水印层高度随 **`document.documentElement`（或约定根）滚动高度**更新，并监听 **`resize`**，必要时对文档根使用 **`ResizeObserver`** 与窗口尺寸变化合并更新。

---

## 5. 内容与绘制规则

- **水印内容优先级**：若配置了 **`image`** 且加载 / 绘制成功，则使用**图片 tile**；否则使用 **`text`**（`string` 或 **`string[]`** 多行）。
- **`disabled === true`**：**不渲染**水印层，**不**注册 `MutationObserver` 或其它副作用监听。
- **Props 变化**：依赖图案的 props 变化时**重绘**；实现侧使用 **`requestAnimationFrame` 合并**或短**防抖**，避免窗口连续 `resize` 时频繁 **`toDataURL`**。
- **`image` URL 变化**：通过递增 **token** 或等价方式忽略**过期异步回调**，避免竞态覆盖。

---

## 6. 防删除（4C）

- **主语义**：**`tamperResistant`**，`boolean`，默认 **`false`**。为 **`true`** 时使用 **`MutationObserver`**（及必要时与**子树**相关的配置）监视水印节点被移除或从约定父节点脱离等情况，**自动恢复**（重新挂载节点并触发重绘流程）。
- **兼容别名**：**`editable`**（与原始需求文档一致）。**`editable === false`** 等价于开启防删逻辑。
- **同时传入**：若 **`tamperResistant` 显式传入**（含 `true` / `false`），**以 `tamperResistant` 为准**；**仅传 `editable`** 时按上条映射。文档表格中写明优先级，避免误用。
- **卸载**：**`onBeforeUnmount`** 中断开 **observer**、移除 **resize / ResizeObserver / rAF** 等监听。

---

## 7. 对外 API 概要

### 7.1 命名与注册

- 组件名：**`C7Watermark`**
- 路径：`quick-ui/src/packages/C7Watermark/index.vue`（+ `buildWatermarkPattern.js`）
- 在 **`quick-ui/src/packages/index.js`** 中 **export** 与 **`installPackages`** 注册。

### 7.2 Props（与原始需求对齐并扩展）

| Prop | 类型 / 约束 | 说明 |
|------|----------------|------|
| **`text`** | `string \| string[]` | 文本或多行；图片失败时回落。 |
| **`image`** | `string`（URL） | 优先；失败回落 `text`。 |
| **`fullscreen`** | `boolean`，默认 `false` | 全屏 vs 容器模式。 |
| **`fullscreenScope`** | `'viewport' \| 'document'` | 仅 **`fullscreen=true`** 时生效；默认 **`'viewport'`**（实现须在文档中写明）。 |
| **`disabled`** | `boolean` | `true` 不渲染水印。 |
| **`tamperResistant`** | `boolean` | 防删；与 `editable` 优先级见 §6。 |
| **`editable`** | `boolean` | 兼容：**`false` ⇒ 防删**；与 `tamperResistant` 同时存在时 **`tamperResistant` 优先**。 |
| **`zIndex`** | `number` | 全屏 / 叠层层级。 |
| **`crossOrigin`** | `string \| undefined` | 图片 **`HTMLImageElement.crossOrigin`**；业务按 CDN 配置。 |
| **画布 / 样式** | — | **`fontSize` / `fontColor` / `fontFamily` / `opacity` / `rotate` / `gapX` / `gapY` / `width` / `height` / `offsetX` / `offsetY`** 与原始需求一致；默认值在实现 JSDoc 与文档中列出。 |

根节点 **`class` / `style`** 透传策略与现有 C7 组件对齐（如 **`defineOptions({ inheritAttrs: true })`** 绑定根或水印层策略在实现时二选一并写入 JSDoc，**须**与「水印层不挡点击」不冲突）。

---

## 8. 验收标准（与原始需求对齐）

- **文本 / 图片**均可生成**可见的重复背景**；图片失败时可见**文本**回落。
- **`fullscreenScope`**：`viewport` 与 `document` 在长页场景下行为符合 §4.2。
- **`tamperResistant === true`** 或 **`editable === false`**（且未被 `tamperResistant` 覆盖）时，**手动删除水印层 DOM** 后**自动恢复**。
- **`disabled=true`** 时无水印层、无 observer。
- **卸载**后无残留监听。

---

## 9. 文档与示例（1B）

- **VitePress**：新增 C7Watermark 说明页 — props 表、`fullscreenScope`、`crossOrigin`、防删语义、容器 / 全屏示例。
- **Layout 片段**：文档中提供「在根 Layout 套一层全屏水印、从 store / 用户信息拼 `text`」的**可复制示例**，**不**作为仓库默认改动的硬性要求。

---

## 10. 测试建议（实现阶段）

- **Dev 演示页**（若项目惯例存在 `C7*E2E.vue`）：文本 / 图片、两种 `fullscreenScope`、防删手动验收。
- **自动化**：以 **E2E 或 smoke** 为主（例如删除水印节点后断言层仍存在或可恢复）；**不做**整页像素级 Canvas 断言（非必须）。

---

## 11. 修订记录

| 日期 | 说明 |
|------|------|
| 2026-05-08 | 初版：brainstorming 澄清与整稿确认后落盘。 |
