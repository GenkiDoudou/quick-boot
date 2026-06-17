## Context

- 依据：`docs/superpowers/specs/2026-06-15-intent-recognition-node-design.md`（brainstorming 定稿 1:B–6:B）。
- 现状：`question-classifier` 使用 `classes[{id,name,description}]`、字符串 `sourceHandle`、输出 `classId`/`className`；Handler 使用全局 ChatModel，无模式/兜底策略。
- 约束：DSL `type` 保持 `question-classifier`；Chat API only；节点级 `modelId` 对接 `WorkflowAiGuard`（与 `LlmForm` 一致）。

## Goals / Non-Goals

**Goals:**

- 对齐扣子意图识别核心：极速/完整模式、意图名称+示例、数字 ID 分支、固定兜底、失败走兜底。
- 前后端统一 data 模型与 handle 语义；旧图可加载并迁移。
- 图校验强制兜底边与意图数量上限。

**Non-Goals:**

- 新 type `intent-recognition` 并存
- 对话历史、会话轮数（P1）
- Responses API、意图评测/A/B

## Decisions

### 1. 方案 A：原地增强 `question-classifier`（采用）

**理由**：改动面小，复用分支路由与 Handler 注册；与澄清选项 1:B 一致。  
**备选**：新 type 并存（零破坏但双套逻辑，已否决）。

### 2. 数字 `classificationId` 与 handle

- 运行时 ID = `intents` 数组下标 + 1；未命中/失败 = 0。
- 引擎 `successWithBranch(outputs, String.valueOf(classificationId))`。
- 画布固定兜底桩 `sourceHandle="0"`，标签「其他」。

**理由**：与扣子及澄清 3:A、4:A 一致；便于下游数值比较。

### 3. data 结构与兼容

新结构：

```json
{
  "mode": "fast",
  "modelId": null,
  "query": "{{start_1.question}}",
  "systemPrompt": "",
  "intents": [{ "name": "售前咨询", "examples": ["想买一台笔记本"] }]
}
```

归一化：`classes[].description` → `examples`（按行拆分）；丢弃旧 `id`；边 handle 按 `classes` 顺序映射 `1`/`2`/…。

**落点**：后端校验/执行前 + 前端加载 graph 时各做一次（前端保证编辑体验，后端保证执行安全）。

### 4. Prompt 与解析

- 内置系统指令（fast/full 共用）：只能从给定序号选择；无法匹配返回 0；须返回 JSON `{ classificationId, reason }`。
- `full` 模式在内置指令后追加 `systemPrompt`。
- 解析失败、越界、模型异常 → `classificationId=0`，`reason` 含摘要，节点 **SUCCESS**（澄清 6:B）。
- 未配置任何可用模型 → 节点 **FAILED**（配置错误）。

### 5. 图校验（`WorkflowGraphValidator`）

对 `question-classifier`：

1. `intents.length` ≥ 1；fast ≤10，full ≤50
2. 至少一条意图边（handle ∈ `1..N`）
3. **必须**存在 `sourceHandle="0"` 兜底边
4. 所有出边须带 `sourceHandle`

### 6. 前端表单

由 `QuestionClassifierForm.vue` 演进（可重命名 `IntentRecognitionForm.vue`）：

| 区块 | 控件 |
|------|------|
| 运行模式 | `el-radio-group` fast/full |
| 大模型 | `el-select`，复用 `LlmForm` modelOptions |
| 输入 | `TemplateField` → `query` |
| 意图匹配 | `WfVariableTableSection`：名称 + 示例 textarea |
| 系统提示词 | textarea，仅 `full` 显示 |

画布：`WorkflowNodeCard` / `BaseWorkflowNode` 用 `"1".."N"` + `"0"`；`WorkflowEdge` 标签取 `intents[i].name` 或「其他」。

### 7. 实施顺序

1. 后端 data 归一化 + Handler + 校验  
2. 前端表单  
3. 画布 Handle/边标签  
4. nodeMeta 文案  
5. 旧图迁移联调

## Risks / Trade-offs

| 风险 | 缓解 |
|------|------|
| 旧图无兜底边，发布校验失败 | 加载时提示；文档说明须补「其他」连线 |
| 旧字符串 handle 映射错误 | validate 给出明确警告；按 classes 顺序映射 |
| 模型返回非 JSON | 走兜底 0，不中断流程 |
| fast/full 意图上限与 UI 不一致 | 前后端双重校验 |

## Migration Plan

1. 部署后旧草稿/已发布图仍可加载；前端打开设计器时执行 `classes`→`intents` 与 handle 映射。
2. 用户保存或发布前须补兜底边并满足意图数量限制。
3. 回滚：还原 Handler/校验/前端即可；graph_json 中新字段对旧版无害（旧版忽略新字段）。

## Open Questions

（无。设计 spec 已定稿。）
