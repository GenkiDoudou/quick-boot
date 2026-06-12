## Why

`add-ai-workflow` 已交付可用的 MVP 工作流设计器（Vue Flow + 三栏 + 通用节点），但用户反馈**视觉与配置体验**明显弱于 Dify 等企业 AI 工作台：节点仅显示类型名与 nodeId、属性面板大量 JSON 文本框、调试与画布割裂。在不改后端 DSL/API 的前提下，需要一期 UI 改造以提升可用性与专业感。

## What Changes

- **全屏沉浸布局**：设计器路由 `meta.fullScreen`，隐藏系统侧栏/Tags，画布 `100vh`。
- **P1 节点卡片**：分类型图标、色条、画布摘要；左栏分组+搜索；分支边标签；自动保存。
- **P2 配置交互**：变量选择器、知识库下拉、结构化 if-else/分类器/schema 表单；移除 JSON 手填。
- **P3 Debug 体验**：画布节点运行态高亮；底部 RunPanel 替代 Drawer；Trace 点击定位节点；「上次运行」Tab。
- **顶栏收敛**：合并 Debug/异步为「▶ 测试运行」；保存状态指示。
- **不修改** `quickboot-workflow` 后端 API 与 DSL 契约。

## Capabilities

### New Capabilities

- `workflow-design-ui`：工作流设计器 Dify 风全屏 UI、节点卡片、配置面板、变量选择器、运行调试与高亮

### Modified Capabilities

（无。`workflow-engine` 后端规范不变。）

## Impact

- **前端**：`quick-ui/src/views/workflow/design/**` 重构与新增组件；`nodeMeta.js` 扩展 `category`/`outputs`。
- **布局**：`router/index.js` 设计器 `meta.fullScreen`；`layout/index.vue` 或等价处支持全屏路由。
- **依赖**：可选 `marked`（LLM 流式 Markdown）；沿用 `@vue-flow/*`。
- **API 复用**：`listKnowledgeBase`、`saveGraph`、`validateGraph`、`run/debug`、`run/async`、`run/stream`。
- **后端**：无变更。
