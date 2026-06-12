# 工作流设计器 UI 改造（对标 Dify）— 设计说明

**日期**：2026-06-07  
**状态**：已定稿（brainstorming 澄清结论）  
**前置**：`add-ai-workflow` 已实现 MVP 设计器（Vue Flow + 三栏 + BaseWorkflowNode）  
**澄清结论**：1A、2ABC、3A

---

## 1. 背景与目标

### 1.1 背景

当前 `views/workflow/design/index.vue` 为功能可用的 MVP：

- 左：12 节点文字列表拖拽  
- 中：Vue Flow 画布 + 通用 `BaseWorkflowNode`  
- 右：`el-form` + 部分 JSON 文本框  
- 调试：底部 Drawer 时间线  

用户反馈「不够好看、不好用」，希望体验接近 **Dify 工作流编辑器**（企业 AI 工作台风格），而非 Coze 的 Bot 广场风格。

### 1.2 目标（本期）

| 优先级 | 能力 | 说明 |
|--------|------|------|
| **P1** | 节点卡片视觉升级 | 分类型图标、色条、画布摘要；分支边标签 |
| **P2** | 配置交互升级 | 变量选择器、知识库下拉、结构化条件/分类表单 |
| **P3** | Debug 画布高亮 | 运行态节点着色、逐步 Trace、I/O 侧栏 |
| **布局** | **全屏画布** | 进入设计器隐藏系统侧栏/顶栏 Tags，Dify 式沉浸 |

### 1.3 非目标（本期不做）

- 更换画布引擎（仍用 **Vue Flow**）  
- 嵌入 Dify 开源 React 前端  
- Coze 式 Bot 对话预览侧栏（P5 可选）  
- 后端 DSL / API 契约变更（仅前端展示层增强）  
- 移动端适配  

---

## 2. 已定稿产品决策

| 题号 | 选项 | 结论 |
|------|------|------|
| 1 | A | 视觉与交互 **对标 Dify** 企业工作台 |
| 2 | ABC | **P1 节点 + P2 配置 + P3 调试** 一期全做 |
| 3 | A | **全屏设计器**，隐藏左侧系统菜单 |

---

## 3. 整体布局（全屏 Dify 风）

### 3.1 进入/退出全屏

| 项 | 方案 |
|----|------|
| 路由 | 保持 `/workflow/design/:id`，`meta.fullScreen: true` |
| Layout | 设计器路由不走 `Layout`（与现有 hidden 路由一致），或 Layout 内根据 meta 隐藏 Sidebar + Navbar + TagsView |
| 高度 | `100vh` 占满视口，不再 `calc(100vh - 84px)` |
| 退出 | 顶栏左侧「← 返回工作流列表」 |

**实现建议**：在 `router/index.js` 设计器路由增加 `meta: { fullScreen: true, activeMenu: '/workflow/list' }`；`layout/index.vue` 或 `AppMain.vue` 检测 `route.meta.fullScreen` 时渲染纯 `<router-view />`。

### 3.2 线框图

```text
┌──────────────────────────────────────────────────────────────────────────┐
│ ← 返回   工作流名称 · 草稿 v3          [未保存●]  [校验] [▶ 测试运行] [发布] │
├──────────┬─────────────────────────────────────────────┬───────────────┤
│ 🔍 搜索   │                                             │  节点配置      │
│          │              画布（点阵背景）                  │  ┌───────────┐ │
│ 基础      │    ┌─────────┐      ┌─────────┐             │  │ LLM       │ │
│  ○ 开始   │    │🟢 开始   │─────▶│🔵 知识库 │             │  │ 模型/温度  │ │
│  ○ 回答   │    │ question │      │ KB: xxx │             │  │ Prompt    │ │
│ 逻辑      │    └─────────┘      └────┬────┘             │  │ [+变量]   │ │
│  ○ 分支   │                          │                  │  └───────────┘ │
│ AI       │                    ┌─────▼─────┐            │               │
│  ○ LLM   │                    │🟣 LLM     │            │  （未选节点时  │
│  ○ 知识库 │                    │ deepseek  │            │   显示画布说明）│
│ 工具      │                    └───────────┘            │               │
│  ○ HTTP  │         [MiniMap]  [缩放控件]               │               │
├──────────┴─────────────────────────────────────────────┴───────────────┤
│ ▶ 测试运行面板（可折叠）：输入 Start 参数 │ 步骤 Trace │ LLM 流式输出        │
└──────────────────────────────────────────────────────────────────────────┘
```

### 3.3 顶栏工具收敛（对标 Dify）

| 按钮 | 行为 | 说明 |
|------|------|------|
| 校验 | 调用 `validateGraph` | 失败 toast + 画布定位首个问题节点 |
| **▶ 测试运行** | 主按钮，展开底部运行面板 + Debug | 合并原「Debug / 异步」为一步：默认同步 Debug，长任务自动走 async+SSE |
| 发布 | 确认后 `publish` | 保留 |
| 保存 | **自动保存**（debounce 3s）+ 手动保存快捷键 Ctrl+S | 顶栏显示「已保存 / 保存中 / 未保存」 |

---

## 4. P1 — 节点卡片视觉

### 4.1 设计原则

- 每种 `type` 独立 Vue 组件（或统一 `WorkflowNodeCard` + `type` 插槽）  
- 节点宽度固定 **240px**，圆角 **12px**，左侧 **4px 色条** + **SVG 图标**  
- 画布上展示 **1 行摘要**（非 nodeId），减少技术感  
- 选中：蓝色外发光；运行成功：绿色边框；失败：红色边框；运行中：脉冲动画  

### 4.2 各节点摘要规则

| type | 图标色 | 摘要示例 |
|------|--------|----------|
| start | `#67c23a` | `输入: question` |
| answer | `#409eff` | `输出模板已配置` |
| llm | `#0a2463` | `temperature: 0.3 · 流式` |
| knowledge-retrieval | `#e6a23c` | `知识库: {name或kbId}` |
| if-else | `#909399` | `条件: 1 条` |
| template-transform | `#626aef` | 模板前 24 字预览 |
| variable-assign | `#626aef` | `N 个变量` |
| variable-aggregator | `#626aef` | `合并 N 项` |
| http-request | `#f56c6c` | `GET https://...` |
| question-classifier | `#b88230` | `N 个分类` |
| parameter-extractor | `#b88230` | `schema: N 字段` |
| list-operator | `#909399` | `filter / first / ...` |

### 4.3 连线与分支

- 默认边：贝塞尔曲线，色 `#94b8ff`，hover 加深  
- **if-else**：出口 Handle 旁画布标签 `IF` / `ELSE`（边 `label` 或 `EdgeLabel` 组件）  
- **question-classifier**：每条边显示 `className`  
- 连线中点「+」插入节点（Phase 2 可选，本期可不做）  

### 4.4 左栏节点面板

- 分组：**基础 / 逻辑 / AI / 工具**（见 `nodeMeta.js` 增加 `category`）  
- 顶部 **搜索框** 过滤  
- 每项：**图标 + 名称 + 一行描述**，hover 轻微上浮  
- 拖拽时 ghost 预览为迷你卡片  

### 4.5 文件结构（前端）

```text
views/workflow/design/
├── index.vue                 # 全屏壳 + 布局
├── components/
│   ├── DesignToolbar.vue
│   ├── NodePalette.vue
│   ├── NodeConfigPanel.vue
│   ├── RunPanel.vue
│   ├── VariablePicker.vue
│   └── edges/WorkflowEdge.vue
├── nodes/
│   ├── WorkflowNodeCard.vue  # 统一外壳
│   ├── StartNode.vue …       # 或 type→summary 函数
│   └── nodeIcons.js
├── composables/
│   ├── useAutoSave.js
│   ├── useWorkflowRun.js     # Debug + 画布高亮状态
│   └── useUpstreamVariables.js
├── nodeMeta.js
└── graphConverter.js
```

---

## 5. P2 — 配置交互升级

### 5.1 右侧配置面板结构

对标 Dify：**节点标题区 + Tab（设置 | 上次运行）**（「上次运行」P3 填充）。

**设置 Tab** 按 type 渲染专用表单组件，**禁止**让用户直接编辑 JSON（`conditions` / `classes` / `schema` 改为 UI）。

### 5.2 变量选择器 `VariablePicker`

| 项 | 说明 |
|----|------|
| 触发 | Prompt / 模板 / 条件左值等输入框旁「插入变量」按钮 |
| 数据源 | 从当前 graph **拓扑上游** 节点收集输出字段（静态表 + `nodeMeta.outputs`） |
| 展示 | 树形：`start_1` → `question`；`kb_1` → `contextText` … |
| 插入 | 光标处插入 `{{nodeId.field}}` |
| sys | 固定项：`sys.kbId`、`sys.runId`、`sys.userId` |

需在 `nodeMeta.js` 为每种 type 声明 `outputs: [{ key, label, type }]`。

### 5.3 业务控件替换

| 字段 | 现况 | 改造 |
|------|------|------|
| `kbId` | 文本 | `el-select` 远程搜索，调用 `listKnowledgeBase` |
| LLM `temperature` | number | 滑块 0–1 + 数字框 |
| `if-else.conditions` | JSON textarea | **条件行列表**：左值（可插变量）、运算符下拉、右值 |
| `question-classifier.classes` | JSON | **分类行列表**：id、名称、描述；连线 Handle 与 id 联动 |
| `parameter-extractor.schema` | JSON | **字段行列表**：key、type、description、required |
| `http-request` | 分散字段 | Method 下拉 + URL + Headers 键值表 + Body |

### 5.4 校验反馈

- 保存/校验失败：右侧表单字段标红 + **画布节点红框** + 滚动到该节点  
- 未配置必填项：节点摘要显示橙色警告点  

---

## 6. P3 — Debug 画布高亮与 Trace

### 6.1 运行流程（合并测试运行）

1. 用户点「▶ 测试运行」→ 底部 `RunPanel` 展开  
2. 填写 Start 入参（根据 `start.inputs` 动态渲染，非写死 question/kbId）  
3. 可选勾选「流式输出」→ async + SSE  
4. 运行中：当前节点 **蓝色脉冲**；已完成 **绿色**；失败 **红色**  
5. 点击 Trace 某步 → 画布 focus 对应节点 + 右侧「上次运行」Tab 展示该步 inputs/outputs（脱敏摘要）  

### 6.2 状态映射

| 后端 step.status | 节点样式 |
|------------------|----------|
| RUNNING | `border-color: #409eff` + pulse |
| SUCCESS | `border-color: #67c23a` |
| FAILED | `border-color: #f56c6c` |
| 未执行 | 默认 |

实现：`nodes` 的 `data.runStatus` 由 `useWorkflowRun` 在 SSE `step_start` / `step_end` 时更新。

### 6.3 RunPanel（替代 Drawer）

- 默认 **底部可折叠面板**（高度 40%），非 modal Drawer  
- 三列：**输入** | **步骤 Trace（可点击）** | **LLM 流式 Markdown 渲染**  
- 运行结束保留结果，直到下次运行或关闭  

### 6.4 后端依赖

- **无需改 API**；沿用 `/run/debug`、`/run/async`、`/run/stream`  
- 可选增强（非必须）：`step_end.outputs` 返回摘要已足够  

---

## 7. 视觉规范（对齐 DESIGN.md + Dify）

| Token | 值 | 用途 |
|-------|-----|------|
| 画布背景 | `#f2f4f7` | 主背景 |
| 点阵 | `#dfe3ea` gap 20 | Vue Flow Background |
| 主色 | `#0a2463` | 标题、LLM 节点 |
| 强调 | `#409eff` | 选中、连线 |
| 面板背景 | `#ffffff` | 三栏 |
| 顶栏高度 | 56px | 固定 |
| 字体 | PingFang SC 13px / 标题 15px semibold | 与 DESIGN.md 一致 |

**不做**：渐变-heavy、过多阴影；节点卡片仅 `box-shadow: 0 2px 8px rgba(10,36,99,.08)`。

---

## 8. 实施分期与任务

### 迭代 1 — 全屏 + P1 节点（约 1 周）

1. `meta.fullScreen` + Layout 隐藏侧栏  
2. `DesignToolbar` / `NodePalette`（分类+搜索）  
3. `WorkflowNodeCard` + 12 类摘要 + 边标签  
4. 自动保存 debounce  

### 迭代 2 — P2 配置（约 1 周）

5. `NodeConfigPanel` + 分 type 表单组件  
6. `VariablePicker` + `nodeMeta.outputs`  
7. 知识库 `el-select`；if-else / classifier / schema 可视化表单  

### 迭代 3 — P3 调试（约 3–5 天）

8. `useWorkflowRun` + 节点 `runStatus` 高亮  
9. `RunPanel` 底部三列 + 「上次运行」Tab  
10. LLM 流式区 Markdown 渲染（`marked` 或轻量 div）  

### 验收标准

| ID | 场景 |
|----|------|
| TC_WF_UI_001 | 进入设计器为全屏，无系统左侧菜单 |
| TC_WF_UI_010 | 节点卡片显示图标与摘要，非仅 nodeId |
| TC_WF_UI_020 | LLM Prompt 可通过变量选择器插入 `{{start_1.question}}` |
| TC_WF_UI_021 | 知识库节点可选下拉库名，非手填 ID |
| TC_WF_UI_030 | 测试运行后画布节点按步骤绿/红/蓝高亮 |
| TC_WF_UI_031 | 点击 Trace 步骤定位到画布节点 |

---

## 9. 风险与缓解

| 风险 | 缓解 |
|------|------|
| 12 种节点表单工作量大 | 共用 `NodeConfigPanel` 框架 + 分文件 lazy 表单 |
| 上游变量推断复杂 | 先用静态 `nodeMeta.outputs`，不做动态 schema |
| 全屏与 TagsView 冲突 | 独立 hidden 路由 + meta.fullScreen 双保险 |
| 自动保存与手动发布 | 仅 saveGraph 自动；publish 仍显式确认 |

---

## 10. 与 OpenSpec 衔接

- 建议新变更：`workflow-design-ui-dify`（仅 `quick-ui` + 可选 layout 小改）  
- **不修改** `workflow-engine` 后端 spec  

---

**请评审本文档**。确认后可 `/opsx-propose workflow-design-ui-dify` 或直接进入实现。
