# 工作流意图识别节点 — 设计说明

**日期**：2026-06-15  
**状态**：已定稿（brainstorming 澄清结论）  
**依据**：`原始需求/意图识别节点.md`（扣子编程意图识别节点）  
**澄清结论**：1:B、2:C、3:A、4:A、5:B、6:B  
**关联**：`docs/superpowers/specs/2026-06-07-ai-workflow-design.md`（工作流引擎）、`docs/superpowers/specs/2026-06-07-ai-model-management-design.md`（节点级 modelId）

---

## 1. 背景与目标

### 1.1 背景

项目已有 `question-classifier`（问题分类）节点：通过 LLM + JSON 输出 `classId`/`className` 做分支路由，能力上与扣子「意图识别」重叠，但缺少：

- 极速 / 完整两种运行模式
- 数字 `classificationId`（1..N / 0 兜底）与 `reason` 输出
- 节点级模型选择、完整模式系统提示词
- 固定兜底出口与模型失败走兜底策略

### 1.2 目标（本期）

在**不新增节点 type** 的前提下，将 `question-classifier` **增强并重命名展示为「意图识别」**，对齐扣子核心行为：

| 能力 | 说明 |
|------|------|
| 运行模式 | `fast`（极速，≤10 意图）/ `full`（完整，≤50 意图，可追加系统提示词） |
| 输入 | `query` 模板字段，引用上游变量 |
| 意图匹配 | 意图名称 + 典型示例列表 |
| 模型 | 节点级 `modelId`（空则工作流/全局默认，复用 `WorkflowAiGuard`） |
| 输出 | `classificationId`、`reason`、`classificationName` |
| 分支 | 每意图一条出线（handle `1`..`N`）+ 固定兜底（handle `0`） |
| 异常 | 模型调用失败 / 解析失败 → 兜底分支，非中断流程 |

### 1.3 非目标（本期不做）

- 对话历史、会话轮数（扣子「对话流」能力，P1）
- 新 type `intent-recognition` 与旧节点并存
- Responses API（仅 Chat API，与需求文档一致）
- 意图识别准确率自动评测 / A/B 测试

---

## 2. 已定稿产品决策

| 题号 | 选项 | 结论 |
|------|------|------|
| 1 | B | 在 `question-classifier` 上增强，展示名改为「意图识别」 |
| 2 | C | P0：核心 + 节点模型 + 极速/完整模式（完整含系统提示词） |
| 3 | A | `classificationId` 从上到下 1..N，未命中=0，画布 handle 用数字字符串 |
| 4 | A | 固定「兜底」出口，`sourceHandle="0"` |
| 5 | B | UI：意图列表 + 每意图典型示例 + 完整模式系统提示词区 |
| 6 | B | 模型调用失败 → 兜底分支（`classificationId=0`）+ `reason` 说明 |

---

## 3. 方案对比与选型

| 方案 | 概要 | 优点 | 缺点 |
|------|------|------|------|
| **A（采用）** | 原地增强 `question-classifier`，保留 type 兼容旧图 | 改动面小；复用分支路由/校验/画布 | 旧图字符串 classId 需迁移 |
| B | 新 type `intent-recognition` 并存 | 零破坏旧 DSL | 双套逻辑，与 1:B 冲突 |
| C | LLM + if-else 组合 | 无开发 | 不符合产品形态 |

**采用方案 A。**

---

## 4. 节点数据模型

### 4.1 节点 type 与展示

| 项 | 值 |
|----|-----|
| DSL `type` | `question-classifier`（不变） |
| 设计器展示名 | **意图识别** |
| 分类 | `logic` |
| Handler | `QuestionClassifierNodeHandler`（逻辑增强，类注释更新） |

### 4.2 `data` 字段（新结构）

```json
{
  "label": "意图识别",
  "mode": "fast",
  "modelId": null,
  "query": "{{start_1.question}}",
  "systemPrompt": "",
  "intents": [
    {
      "name": "售前咨询",
      "examples": ["想买一台笔记本", "价格多少"]
    },
    {
      "name": "售后问题",
      "examples": ["怎么退货", "保修多久"]
    }
  ]
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `mode` | `fast` \| `full` | 极速：内置 prompt，最多 10 个意图；完整：可追加 `systemPrompt`，最多 50 个意图 |
| `modelId` | Long \| null | 节点级 Chat 模型；空则 `WorkflowAiGuard.requireChatModelInstance(workflowId, null)` |
| `query` | string | 待识别文本模板 |
| `systemPrompt` | string | 仅 `full` 生效，追加到内置分类指令之后 |
| `intents` | array | 意图列表；**运行时 ID 按数组下标+1 生成**，不由用户填写 |

### 4.3 节点输出（outputs）

| 字段 | 类型 | 说明 |
|------|------|------|
| `classificationId` | number | 命中第 N 个意图时为 N（1..intents.length）；兜底为 **0** |
| `classificationName` | string | 命中意图名称；兜底时为空串或「其他」 |
| `reason` | string | 模型给出的分类依据；失败兜底时含错误摘要 |

**下游引用示例**：`{{intent_1.classificationId}}`、`{{intent_1.reason}}`

### 4.4 旧字段兼容（`classes` → `intents`）

加载或校验前归一化：

| 旧 `classes[]` | 新 `intents[]` |
|----------------|----------------|
| `name` | `name` |
| `description` | `examples`：非空时拆为单条示例（按行分割） |
| `id`（字符串） | **丢弃**；运行时用顺序生成数字 ID |

边的 `sourceHandle` 迁移：若旧图为 `a`/`b` 等字符串，按 `classes` 顺序映射为 `1`/`2`/…；无法映射的边在 validate 时给出警告。

---

## 5. 画布与分支路由

### 5.1 出口 Handle

```
意图1（intents[0]）  → sourceHandle = "1"
意图2（intents[1]）  → sourceHandle = "2"
...
兜底（固定）        → sourceHandle = "0"
```

- 画布右侧：每个意图一个 Handle + **末尾固定「其他」兜底 Handle**（id=`0`）
- 引擎：`NodeResult.successWithBranch(outputs, String.valueOf(classificationId))`
- 连线标签：意图边显示 `intents[i].name`；兜底边显示「其他」

### 5.2 图校验（`WorkflowGraphValidator`）

对 `question-classifier` 节点：

1. 至少 1 条意图边（`sourceHandle` ∈ `1..intents.length`）
2. **必须**存在兜底边（`sourceHandle = "0"`）
3. 每条从该节点出发的边须带 `sourceHandle`
4. `intents.length`：fast 模式 ≤10，full 模式 ≤50；为空则校验失败

### 5.3 前端改动点

| 文件 | 改动 |
|------|------|
| `nodeMeta.js` | label、defaults、outputs、summarize |
| `WorkflowNodeCard.vue` / `BaseWorkflowNode.vue` | Handle 用 `"1".."N"` + 固定 `"0"`；不再用 `cls.id` |
| `WorkflowEdge.vue` | 标签：按 index 取 `intents[i].name`；`0` →「其他」 |
| `QuestionClassifierForm.vue` | 重命名/重构为意图识别表单（见 §7） |
| `NodeConfigPanel.vue` | 组件注册名可更新 |

---

## 6. 后端执行

### 6.1 Handler 流程

```text
1. 读取 mode / modelId / query / systemPrompt / intents
2. 归一化 intents（兼容 classes）
3. 渲染 query
4. 构建 prompt：
   - 内置系统指令（仅能从列表中选择；返回 JSON）
   - full 模式追加 systemPrompt
   - 意图表：序号 + 名称 + 示例
   - 用户输入
5. ChatModel = aiGuard.requireChatModelInstance(workflowId, modelId)
6. ChatClient 调用（Chat API）
7. 解析 JSON：{ "classificationId": number, "reason": string }
8. 校验 classificationId 范围；越界/无效 → 0
9. successWithBranch(outputs, String.valueOf(classificationId))
```

### 6.2 Prompt 约定

模型须返回**仅 JSON**：

```json
{
  "classificationId": 2,
  "reason": "用户询问退货流程，属于售后问题"
}
```

内置指令要点（极速/完整共用）：

- 只能从给定意图序号中选择
- 无法匹配任何意图时返回 `classificationId: 0`
- `reason` 简要说明依据

### 6.3 异常与兜底（6:B）

| 场景 | classificationId | reason | 节点状态 |
|------|------------------|--------|----------|
| 模型调用成功且命中 | 1..N | 模型输出 | SUCCESS |
| 模型输出无法解析 | 0 | 含「解析失败」说明 | SUCCESS（走兜底） |
| classificationId 越界 | 0 | 含「无效分类」说明 | SUCCESS |
| 模型超时/5xx/不可用 | 0 | 含异常摘要 | SUCCESS |
| 未配置任何可用模型 | — | — | **FAILED**（配置错误） |

### 6.4 Trace

`traceInputs` 建议包含：`mode`、`modelId`、`query`（可截断）、意图数量。  
`outputs` 含：`classificationId`、`classificationName`、`reason`。

---

## 7. 前端表单设计

由 `QuestionClassifierForm.vue` 演进（可重命名为 `IntentRecognitionForm.vue`）。

| 区块 | 控件 | 说明 |
|------|------|------|
| 运行模式 | `el-radio-group` | 极速 / 完整 |
| 大模型 | `el-select` | 复用 `LlmForm` 的 modelOptions 加载逻辑；可清空 |
| 输入 | `TemplateField` | `query` |
| 意图匹配 | `WfVariableTableSection` | 列：意图名称、典型示例（textarea，一行一例） |
| 系统提示词 | `textarea` | 仅 `mode=full` 显示 |

校验：

- fast：`intents.length` ≤ 10
- full：`intents.length` ≤ 50
- 每条意图 `name` 必填；`examples` 可选但推荐填写

---

## 8. 与 AI 模型配置的关系

- 节点 `modelId` 对接 `WorkflowAiGuard.requireChatModelInstance(workflowId, nodeModelId)`
- 下拉数据来自工作流已有模型选项 API（与 `LlmForm` 一致）
- 若 AI 模型管理模块尚未落地，可暂回退全局 `ChatModel` Bean

---

## 9. 测试要点

| ID | 场景 | 期望 |
|----|------|------|
| TC_INTENT_001 | 3 意图 + 兜底连线，输入命中第 2 意图 | 走 handle `2`，`classificationId=2` |
| TC_INTENT_002 | 输入与所有意图无关 | 走 handle `0`，`classificationId=0` |
| TC_INTENT_003 | 模拟模型 500 | 走 handle `0`，`reason` 含错误信息，节点 SUCCESS |
| TC_INTENT_004 | fast 模式 11 个意图 | validateGraph 失败 |
| TC_INTENT_005 | full + systemPrompt | prompt 含用户追加内容 |
| TC_INTENT_006 | 旧图 classes + 字符串 handle | 迁移后仍可运行或给出明确校验提示 |
| TC_INTENT_007 | 未配兜底边 | publish/validate 失败 |
| TC_INTENT_008 | 指定 modelId 运行 | 使用对应 ChatModel |

---

## 10. 实施顺序建议

1. **后端**：`data` 归一化工具 + Handler prompt/解析/兜底逻辑 + 校验增强  
2. **前端表单**：模式、模型、意图列表、系统提示词  
3. **画布**：数字 Handle + 固定兜底桩 + 边标签  
4. **nodeMeta / 文案**：「意图识别」  
5. **旧图迁移**：加载时 `classes`→`intents`，边 handle 映射  
6. **联调**：Debug 运行 + Trace 核对  

---

## 11. Spec 自检

- [x] 无 TBD / 占位段落  
- [x] 与澄清选项 1:B–6:B 一致  
- [x] type 保持 `question-classifier` 与旧 spec 兼容说明  
- [x] 兜底、数字 ID、模型失败策略无歧义  
- [x] 范围限定为单节点增强，可单独立项实现计划  

---

*文档版本：1.0*
