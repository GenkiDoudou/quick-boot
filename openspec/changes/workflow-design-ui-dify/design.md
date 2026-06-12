## Context

- 前置：`add-ai-workflow` 已实现 `views/workflow/design/index.vue`（Vue Flow + BaseWorkflowNode + 三栏 + Drawer Trace）。
- 已定稿设计：`docs/superpowers/specs/2026-06-07-workflow-design-ui-redesign.md`。
- 澄清：**1A 对标 Dify**、**2ABC（P1+P2+P3 一期全做）**、**3A 全屏画布**。
- 视觉遵循 `DESIGN.md`（主色 `#0a2463`、强调 `#409eff`、背景 `#f5f7fa`）。

## Goals / Non-Goals

**Goals:**

- 全屏 Dify 风三栏 + 底栏运行面板。
- P1：WorkflowNodeCard（240px、图标、摘要、runStatus 样式钩子）、NodePalette 分组搜索、WorkflowEdge 分支标签、useAutoSave。
- P2：NodeConfigPanel（设置 | 上次运行 Tab）、VariablePicker、知识库 el-select、可视化条件/分类/schema 表单。
- P3：useWorkflowRun 画布高亮、RunPanel 三列、Trace 定位、Markdown 流式区。

**Non-Goals:**

- 更换 Vue Flow 或嵌入 Dify React 前端。
- 后端 DSL/API 变更。
- Coze Bot 对话预览、移动端、连线中点插入节点。

## Decisions

### D1：全屏实现

- **选择**：路由 `meta.fullScreen: true`；Layout 检测后仅渲染 `<router-view />`，高度 `100vh`。
- **备选**：CSS 覆盖隐藏侧栏 — 易与 TagsView 冲突，不采纳。

### D2：组件拆分

将 `index.vue` 拆为：`DesignToolbar`、`NodePalette`、`NodeConfigPanel`、`RunPanel`、`WorkflowNodeCard`、`VariablePicker`、`useAutoSave`、`useWorkflowRun`、`useUpstreamVariables`。

### D3：nodeMeta 扩展

- 增加 `category`：`basic` | `logic` | `ai` | `tool`
- 增加 `outputs: [{ key, label }]`
- 增加 `summary(nodeData)` 函数生成画布摘要

### D4：变量选择器

- 上游节点：对 graph 做 BFS 从 start 到选中节点，收集 `nodeMeta.outputs`。
- 插入语法：`{{nodeId.field}}`；sys：`sys.kbId` 等。

### D5：测试运行合并

- 顶栏单一「▶ 测试运行」：默认 `run/debug`；勾选流式时 `run/async` + `subscribeRunStream`。
- Start 入参根据 `start.inputs` 动态渲染。

### D6：workflowId 传递

- **禁止** `Number(workflowId)`（雪花 ID 精度丢失）；路由参数与 API 均用字符串。

## Risks / Trade-offs

| 风险 | 缓解 |
|------|------|
| index.vue 拆分工作量大 | 分 3 迭代落地，每迭代可独立验收 |
| 12 种节点表单重复 | NodeConfigPanel 框架 + `forms/*.vue` 按 type lazy |
| 全屏与权限菜单 | hidden 路由 + activeMenu 保持列表高亮 |
| marked 依赖 | 可选轻量实现或 devDependency |

## Migration Plan

- 直接替换现有 `design/index.vue` 实现；列表/运行记录页不变。
- 无数据迁移；发布后用户刷新即可。

## Open Questions

- `marked` 是否引入：建议迭代 3 引入，体积可接受则采用。
