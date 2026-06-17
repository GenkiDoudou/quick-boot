## 1. 后端 data 归一化与工具

- [x] 1.1 新增或扩展 `question-classifier` data 归一化：将 `classes[]` 转为 `intents[]`（`description` 按行拆为 `examples`，丢弃旧 `id`）
- [x] 1.2 在校验与 Handler 执行入口调用归一化，确保旧图可加载执行
- [x] 1.3 更新节点 JSON Schema / `validateSchema()`：支持 `mode`、`modelId`、`query`、`systemPrompt`、`intents`；fast ≤10、full ≤50 意图

## 2. 后端 Handler 执行逻辑

- [x] 2.1 增强 `QuestionClassifierNodeHandler`：读取 mode/modelId/query/systemPrompt/intents，渲染 query 模板
- [x] 2.2 实现内置 prompt 构建（意图表：序号+名称+示例；要求仅返回 JSON `{ classificationId, reason }`）
- [x] 2.3 接入 `WorkflowAiGuard.requireChatModelInstance(workflowId, modelId)` 调用 Chat API
- [x] 2.4 解析 JSON 输出，校验 `classificationId` 范围；越界/解析失败/模型异常 → `classificationId=0` 且节点 SUCCESS
- [x] 2.5 组装 outputs：`classificationId`、`classificationName`、`reason`；`successWithBranch(outputs, String.valueOf(classificationId))`
- [x] 2.6 Trace：`traceInputs` 含 mode/modelId/query（可截断）/意图数；outputs 含三项输出字段

## 3. 后端图校验

- [x] 3.1 增强 `WorkflowGraphValidator`：`question-classifier` 须至少一条意图边（handle `1..N`）
- [x] 3.2 强制存在兜底边 `sourceHandle="0"`
- [x] 3.3 校验所有出边带 `sourceHandle`；意图数量与 mode 上限一致
- [x] 3.4 旧字符串 handle 映射为数字 handle 或给出明确校验警告

## 4. 前端 nodeMeta 与文案

- [x] 4.1 更新 `nodeMeta.js`：展示名「意图识别」、defaults（mode/query/intents）、outputs（classificationId/classificationName/reason）、summarize
- [x] 4.2 更新 `WfNodeType` 相关注释或常量文案（若前端有映射）

## 5. 前端配置表单

- [x] 5.1 重构 `QuestionClassifierForm.vue`（可重命名 `IntentRecognitionForm.vue`）：运行模式 radio、模型下拉（复用 LlmForm modelOptions）
- [x] 5.2 添加 `query` TemplateField、意图表格（WfVariableTableSection：名称+典型示例）
- [x] 5.3 完整模式显示 `systemPrompt` textarea；表单校验 fast≤10、full≤50、name 必填
- [x] 5.4 更新 `NodeConfigPanel.vue` 组件注册；加载 graph 时执行 `classes`→`intents` 迁移

## 6. 前端画布 Handle 与边标签

- [x] 6.1 更新 `WorkflowNodeCard.vue` / `BaseWorkflowNode.vue`：Handle 使用 `"1".."N"` + 固定 `"0"` 兜底桩（文案「其他」）
- [x] 6.2 更新 `WorkflowEdge.vue`：意图边标签取 `intents[i].name`，`0` →「其他」
- [x] 6.3 新增/删除意图时同步更新 Handle 列表

## 7. 旧图兼容与联调

- [x] 7.1 前端加载旧图：边 `sourceHandle` 按 `classes` 顺序映射为 `1`/`2`/…
- [ ] 7.2 Debug 运行联调：命中意图、未命中兜底、模拟模型失败走 `0` 分支
- [ ] 7.3 校验联调：无兜底边 publish 失败、fast 11 意图 validate 失败
- [x] 7.4 构建验证：`mvn -pl quickboot-workflow compile` 与 `pnpm build:prod`

## 8. 测试用例（可选文档）

- [ ] 8.1 按 design spec §9 核对 TC_INTENT_001–008 场景（手工或后续自动化）
