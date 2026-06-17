## Why

项目已有 `question-classifier`（问题分类）节点，通过 LLM 做分支路由，但与扣子「意图识别」相比缺少极速/完整模式、数字 `classificationId`、节点级模型选择、固定兜底出口及模型失败走兜底策略。在 AI 工作流设计器已具备基础编排能力后，增强该节点并统一展示为「意图识别」，可让业务方以更低成本完成多意图分流，且无需引入新节点 type 破坏既有 DSL。

## What Changes

- **原地增强** `question-classifier` 节点（DSL `type` 不变），设计器展示名改为「意图识别」。
- 新增节点 `data` 字段：`mode`（`fast`/`full`）、`modelId`、`query`、`systemPrompt`、`intents[{name, examples}]`；旧 `classes` 在加载/校验前归一化为 `intents`。
- 节点输出改为：`classificationId`（number，1..N 或 0 兜底）、`classificationName`、`reason`。
- 画布分支：每意图一条出线（`sourceHandle` 为 `"1"`..`"N"`）+ 固定兜底（`sourceHandle="0"`，标签「其他」）。
- 后端 `QuestionClassifierNodeHandler` 增强：节点级 `WorkflowAiGuard.requireChatModelInstance`、内置 prompt、JSON 解析、越界/失败走兜底（节点仍 SUCCESS）。
- `WorkflowGraphValidator` 增强：必须连兜底边；fast ≤10 意图、full ≤50 意图。
- 前端表单重构：运行模式、模型下拉、意图列表+典型示例、完整模式系统提示词；画布 Handle 与边标签同步更新。
- 旧图兼容：`classes`→`intents` 迁移；字符串 `sourceHandle` 按顺序映射为数字 handle，无法映射时校验警告。

**非本期**：对话历史/会话轮数、新 type `intent-recognition`、Responses API、准确率评测。

## Capabilities

### New Capabilities

（无。本变更在既有工作流引擎与设计器能力上增强单节点行为。）

### Modified Capabilities

- `workflow-engine`：`question-classifier` 节点 data 模型、输出字段、分支 handle 规则、图校验、Handler 执行与兜底策略。
- `workflow-design-ui`：意图识别节点表单、画布 Handle/边标签、nodeMeta 展示名与摘要。

## Impact

- **后端**：`quickboot-workflow` — `QuestionClassifierNodeHandler`、`WorkflowGraphValidator`、可选 data 归一化工具；复用 `WorkflowAiGuard`。
- **前端**：`quick-ui/src/views/workflow/design/` — `QuestionClassifierForm.vue`（或 `IntentRecognitionForm.vue`）、`nodeMeta.js`、`WorkflowNodeCard.vue`、`BaseWorkflowNode.vue`、`WorkflowEdge.vue`、`NodeConfigPanel.vue`。
- **数据库**：无表结构变更。
- **API**：无新接口；`validateGraph` / `publish` 校验规则收紧（须兜底边）。
- **兼容性**：保留 `type=question-classifier`；旧 `classes` 与字符串 handle 需迁移逻辑；发布前未补兜底边的旧图将校验失败。
