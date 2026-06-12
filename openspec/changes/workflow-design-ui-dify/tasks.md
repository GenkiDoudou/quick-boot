## 1. 全屏布局与路由

- [x] 1.1 设计器路由增加 `meta.fullScreen: true`、`activeMenu: '/workflow/list'`
- [x] 1.2 Layout 检测 `fullScreen` 时隐藏 Sidebar/Navbar/TagsView，渲染纯 router-view
- [x] 1.3 设计器根容器改为 `100vh`，移除 `calc(100vh - 84px)`

## 2. 组件骨架拆分（迭代 1）

- [x] 2.1 创建 `DesignToolbar.vue`（返回、名称、保存状态、校验、测试运行、发布）
- [x] 2.2 创建 `NodePalette.vue`（分组+搜索+拖拽）
- [x] 2.3 重构 `index.vue` 为全屏三栏壳，接入 Toolbar/Palette/画布

## 3. P1 节点卡片与边

- [x] 3.1 扩展 `nodeMeta.js`：`category`、`outputs`、`summary()`、`description`
- [x] 3.2 实现 `WorkflowNodeCard.vue` 替换 `BaseWorkflowNode`（图标、色条、摘要、runStatus 样式）
- [x] 3.3 实现 `WorkflowEdge.vue` 或边 label（if-else IF/ELSE、classifier 分类名）
- [x] 3.4 更新 `graphConverter.js` 兼容新节点 data 字段

## 4. 自动保存

- [x] 4.1 实现 `composables/useAutoSave.js`（debounce 3s、Ctrl+S、状态枚举）
- [x] 4.2 Toolbar 展示已保存/保存中/未保存

## 5. P2 配置面板

- [x] 5.1 实现 `NodeConfigPanel.vue`（设置 | 上次运行 Tab）
- [x] 5.2 实现 `VariablePicker.vue` + `useUpstreamVariables.js`
- [x] 5.3 实现分 type 表单：`forms/LlmForm.vue`、`KnowledgeForm.vue`、`IfElseForm.vue` 等
- [x] 5.4 知识库 `el-select` 接入 `listKnowledgeBase`
- [x] 5.5 移除 conditions/classes/schema 的 JSON textarea 主路径

## 6. P3 运行调试

- [x] 6.1 实现 `composables/useWorkflowRun.js`（debug/async+SSE、runStatus 写回 nodes）
- [x] 6.2 实现 `RunPanel.vue`（底部三列：输入、Trace、流式输出）
- [x] 6.3 Trace 点击 → `fitView`/`setCenter` 定位节点 + 「上次运行」Tab 展示 I/O
- [x] 6.4 Start 入参根据 `start.inputs` 动态渲染（替换写死 question/kbId）
- [x] 6.5 合并顶栏 Debug/异步为「▶ 测试运行」
- [x] 6.6 LLM 流式区 Markdown 渲染（引入 `marked` 或等价）

## 7. 校验与体验

- [x] 7.1 校验失败时画布节点红框 + 右侧字段标红
- [x] 7.2 未填必填项节点摘要橙色警告点
- [x] 7.3 确认所有 API 调用 workflowId 为字符串（无 Number 转换）

## 8. 验证

- [x] 8.1 `pnpm build:prod` 通过
- [ ] 8.2 手工验收 TC_WF_UI_001～031（见 design spec）
- [ ] 8.3 全屏下从列表进入/返回无布局残留
