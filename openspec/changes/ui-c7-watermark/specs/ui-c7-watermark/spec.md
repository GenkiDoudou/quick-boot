# ui-c7-watermark

## Purpose

为 **quick-ui** 提供 **`C7Watermark`**：在 **容器** 或 **全屏** 上叠加 **Canvas 生成的重复水印图案**；支持 **文本/图片**、**`fullscreenScope`**、**`crossOrigin`**、**防删除** 与 **`packages/index.js`** 注册。需求来源 **`原始需求/前端/C7水印.md`**，设计见同变更 **`design.md`** 与 **`docs/superpowers/specs/2026-05-08-c7-watermark-design.md`**。

## ADDED Requirements

### Requirement: 组件路径与全局注册

**`C7Watermark`** MUST 位于 **`quick-ui/src/packages/C7Watermark/`**，且 MUST 至少包含 **`index.vue`** 与 **`buildWatermarkPattern.js`**（或同职责等价文件名）之一套拆分。

组件 MUST 通过 **`quick-ui/src/packages/index.js`** **export** 并在 **`installPackages(app)`** 中注册为全局组件 **`C7Watermark`**。

#### Scenario: 全局注册后模板可用

- **WHEN** 应用已执行 **`installPackages(app)`**
- **THEN** 模板中 MUST 能使用 **`<C7Watermark>`**

### Requirement: 容器模式布局

当 **`fullscreen` 为 `false`**（或未启用全屏的默认策略）时，组件根节点 MUST **`position: relative`**。默认插槽内容 MUST 作为 **主内容** 渲染。水印层 MUST **`position: absolute`** 且 **铺满根节点内容框**（如 **`inset: 0`** 等价语义），并叠在 **默认插槽** 之上。

水印层 MUST **`pointer-events: none`** 且 MUST **`user-select: none`**，以不拦截点击、减少误选。

#### Scenario: 插槽内按钮可点击

- **WHEN** 默认插槽内存在可点击控件且 **`disabled` 不为 `true`**
- **THEN** 用户 MUST 能正常点击该控件（水印层不阻挡）

### Requirement: 全屏模式与 `fullscreenScope`

当 **`fullscreen` 为 `true`** 时，水印层 MUST **`position: fixed`**，且 MUST 使用 prop **`zIndex`**（或 **`z-index`** 同义实现）控制叠放顺序。

组件 MUST 支持 **`fullscreenScope`**，取值为 **`'viewport'`** 或 **`'document'`**；当 **`fullscreen` 为 `false`** 时，**`fullscreenScope`** MUST **不产生与全屏冲突的布局**（可忽略或等价于不生效，须在 JSDoc 说明）。

- 当 **`fullscreenScope` 为 `'viewport'`** 时，水印层 MUST **覆盖当前视口**（不随文档超出视口部分单独延伸高度语义）。
- 当 **`fullscreenScope` 为 `'document'`** 时，水印层 MUST **覆盖整份可滚动文档高度**（长页不露底），并 MUST 在 **窗口尺寸变化** 与 **文档根尺寸变化** 时更新覆盖范围（**`resize`** 与 **`ResizeObserver` on `document.documentElement`** 或 **design** 约定的单一根元素，至少满足其一组合以实现更新）。

**`fullscreenScope`** 的默认值 MUST 为 **`'viewport'`**。

#### Scenario: document 模式长页铺满

- **WHEN** **`fullscreen=true`**、**`fullscreenScope='document'`** 且文档 **`scrollHeight` 大于视口高度**
- **THEN** 水印层覆盖区域 MUST **延伸至文档底部**（用户滚动到底部仍可见水印图案）

### Requirement: `disabled` 不渲染

当 **`disabled` 为 `true`** 时，组件 MUST **不渲染水印层**（或等价于无任何可见水印图案），且 MUST **不**注册 **`MutationObserver`** 与 **仅服务于水印绘制的尺寸监听**。

#### Scenario: disabled 无 observer

- **WHEN** **`disabled=true`**
- **THEN** DOM 中 MUST **无水印图案层**，且 **MUST NOT** 为防删目的挂载 **MutationObserver**

### Requirement: 文本与图片内容及回落

组件 MUST 接受 **`text`**，类型为 **`string` 或 `string[]`**（多行）。

组件 MUST 接受 **`image`**（URL 字符串）。当 **`image`** 存在且 **加载并绘制成功** 时，水印图案 MUST **优先使用图片** tile。当 **图片加载失败、绘制失败或无法读像素** 时，组件 MUST **回落为使用 `text`** 生成图案（若 **`text`** 为空或无效，实现可渲染空图案或透明 tile，**JSDoc** 须说明）。

#### Scenario: 图片失败后可见文本水印

- **WHEN** **`image`** 指向无效资源且 **`text`** 为非空字符串
- **THEN** 用户 MUST **仍能看到基于 `text` 的重复水印**

### Requirement: `crossOrigin` 与图片加载

组件 MUST 支持 **`crossOrigin`** prop，并将其应用于 **`HTMLImageElement.crossOrigin`**（含 **`''` / `'anonymous'` / `undefined`** 等调用方传入值），以便业务配合 CDN CORS。

#### Scenario: 调用方设置 anonymous

- **WHEN** 调用方设置 **`crossOrigin='anonymous'`** 且图片服务器返回允许跨域头
- **THEN** 组件 MUST **能完成基于 Canvas 的图片 tile 绘制**（在其它条件满足时）

### Requirement: 画布与样式参数

组件 MUST 支持以下 props（名称与语义与 **`原始需求/前端/C7水印.md`** 一致），并 MUST 影响 **离屏 Canvas** 生成结果或水印层样式：**`fontSize`、`fontColor`、`fontFamily`、`opacity`、`rotate`、`gapX`、`gapY`、`width`、`height`、`offsetX`、`offsetY`**。

各 prop 的 **默认值** MUST 在 **组件 JSDoc** 与 **VitePress 文档** 中列出且 **一致**。

#### Scenario: rotate 与 gap 生效

- **WHEN** 调用方设置 **`rotate`** 与 **`gapX`/`gapY`** 为非默认值且 **`disabled` 不为 `true`**
- **THEN** 呈现的重复图案 MUST **体现旋转与间距变化**（与默认值可区分）

### Requirement: 防删除与 `tamperResistant` / `editable` 优先级

组件 MUST 支持 **`tamperResistant`**（`boolean`）与 **`editable`**（`boolean`，兼容原始需求）。

**有效防删标志** MUST 按以下规则计算：

- **若 `tamperResistant` 被显式传入**（**`undefined` 以外**），**有效防删** MUST **等于 `tamperResistant` 的布尔值**。
- **否则**，当 **`editable === false`** 时 **有效防删** MUST 为 **`true`**；当 **`editable === true` 或未传 `editable`** 时 **有效防删** MUST 为 **`false`**（**`editable` 缺省语义** 与 **design** 一致并在 JSDoc 写明）。

当 **有效防删** 为 **`true`** 时，组件 MUST 使用 **`MutationObserver`**（或等价机制）监视约定 DOM 范围，**在水印层被移除或破坏挂载关系** 时 **自动恢复水印**（重新挂载并重绘）。

当 **有效防删** 为 **`false`** 时，组件 MUST **不**为防删目的保持常驻 **MutationObserver**（允许无 observer）。

#### Scenario: 仅 editable false 启用防删

- **WHEN** **`editable=false`** 且 **`tamperResistant` 未传入**
- **THEN** 用户从 DevTools **删除水印层节点**后，组件 MUST **在合理时间内恢复水印**

#### Scenario: tamperResistant 显式优先

- **WHEN** **`tamperResistant=false`** 且 **`editable=false`**
- **THEN** **有效防删** MUST 为 **`false`**，用户删除水印层后 MUST **不自动恢复**

### Requirement: 卸载清理

组件在卸载时 MUST **disconnect** 所有 **`MutationObserver`** 实例，并 MUST **移除** **`window` resize**、**`ResizeObserver`**、以及用于合并重绘的 **`requestAnimationFrame` / 定时器** 等监听（凡在实现中注册的 **MUST** 清理）。

#### Scenario: 路由切换后无泄漏

- **WHEN** 组件从 DOM 卸载
- **THEN** 后续 **DOM 变更** MUST **不**再触发该实例的水印恢复逻辑（无活跃 observer）

### Requirement: props 变化重绘

当影响水印图案的 props 变化时，组件 MUST **更新水印图案**。实现 MUST 使用 **`requestAnimationFrame` 合并**或 **短防抖** 策略，避免在连续 **`resize`** 下极高频调用 **`toDataURL`**。

当 **`image` URL** 变化时，实现 MUST **忽略过期异步加载结果**（例如递增 **load token**），以避免 **竞态覆盖**。

#### Scenario: 连续 resize 不卡死主线程

- **WHEN** 用户在短时间内连续改变窗口尺寸
- **THEN** 重绘调用频率 MUST **被合并或限制**（不要求具体帧数，但 MUST **明显低于每像素事件一次同步 `toDataURL`** 的朴素实现）

### Requirement: 文档与 Layout 示例

**VitePress** MUST 新增 **`C7Watermark`** 说明页，包含 **props 表**、**`fullscreenScope`**、**`crossOrigin`**、**防删语义与优先级**、**容器/全屏示例**。

文档 MUST 包含一段 **Layout 级全屏水印** 的 **可复制示例**（从 store/用户信息拼接 **`text`** 等），且 MUST **不**将修改默认业务 Layout 作为 **本变更的强制交付**（业务按需粘贴）。

#### Scenario: 侧栏可导航到文档

- **WHEN** 维护者打开文档站点侧栏
- **THEN** MUST 存在指向 **C7Watermark** 说明页的入口（路径与现有 **C7** 文档组织一致）

### Requirement: Dev 演示页（可选但推荐）

若项目惯例存在 **`quick-ui/src/views/dev/C7*E2E.vue`** 类页面，本变更 SHOULD 新增 **`C7WatermarkE2E.vue`**（或等价命名）并在 **dev 路由** 注册，用于 **文本/图片**、**两种 `fullscreenScope`**、**防删** 的手动验收。

#### Scenario: 路由可打开演示页

- **WHEN** 开发环境访问该演示路由
- **THEN** 页面 MUST 渲染 **`C7Watermark`** 的多种示例区块
