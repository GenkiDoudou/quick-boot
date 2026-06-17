## Context

- 依据：`docs/superpowers/specs/2026-06-14-json-serialize-node-design.md`（brainstorming 定稿 1:A–6:A）。
- 现状：工作流无 JSON 序列化节点；用户需用 `code` 节点手写 `JSON.stringify`。已有 `InputParameterTemplateRenderer`（代码/文本处理/意图识别等复用）、`JSONUtil`（Hutool）。
- 约束：纯新增 `type=json-serialize`；单输入参数；输出仅 `output`；异常不阻断流程。

## Goals / Non-Goals

**Goals:**

- 新增 `json-serialize` 节点：将 Object/Array 等转为紧凑 JSON 字符串。
- 合法 JSON 字符串输入透传，避免 HTTP body 等二次转义。
- 设计器 `tool` 分类、单参数表单、固定输出展示。
- 图校验：须配置有效输入。

**Non-Goals:**

- JSON 反序列化节点（另开 change）
- pretty print / 多输入 / `FAILED` 状态 / 并入 `text-process`

## Decisions

### 1. 独立 type `json-serialize`（采用）

**理由**：与扣子一致、语义清晰、边界小。  
**备选**：并入 `text-process`（已否决，输出语义不同）。

### 2. 单参数 `inputVariables`

```json
{ "inputVariables": [{ "key": "input", "value": "{{llm_1.output}}" }] }
```

- UI 最多 1 行（有行时隐藏「添加」）
- Handler 取第一个有效 `key` 对应解析值

**理由**：对齐澄清 2:A；扣子为单变量处理。

### 3. 序列化规则 `serialize(value)`

| 输入 | 输出 |
|------|------|
| null / 缺失 | `""` |
| 空 String | `""` |
| 合法 JSON 文本 | 原样透传（trim） |
| 普通 String | `JSONUtil.toJsonStr` |
| Map/List/Number/Boolean | `JSONUtil.toJsonStr` 紧凑 |
| 其他 | `""` |

合法 JSON：`JSONUtil.isTypeJSON(str)` 或 `JSONUtil.parse` 成功。

节点始终 **SUCCESS**；不在 outputs 加 `isSuccess`/`errorBody`。

### 4. 后端落点

| 组件 | 职责 |
|------|------|
| `WfNodeType.JSON_SERIALIZE` | 常量 |
| `JsonSerializeNodeHandler` | 解析 input → serialize → `NodeResult.success` |
| `WorkflowGraphValidator.validateNodeData` | `inputVariables` 至少一行且 key/value 非空 |

Trace inputs：`inputKey`、`inputPreview`（截断 500 字符）。

### 5. 前端落点

| 文件 | 变更 |
|------|------|
| `JsonSerializeForm.vue` | 输入区（对齐 CodeForm 单行模式）+ 只读输出区 |
| `nodeMeta.js` | defaults、outputs、summarize、hasValidationWarning |
| `NodeConfigPanel.vue` | 表单注册 |

分类 `tool`，色 `#409eff`，图标 `Document`。允许主画布、循环体、批处理体。

## Risks / Trade-offs

| 风险 | 缓解 |
|------|------|
| 「合法 JSON」判定与业务预期不一致 | 文档与单测覆盖 object/array/字面量/普通文本 |
| 透传导致下游收到未规范化 JSON | 本期按产品决策 3:A；反序列化节点负责解析 |
| 单参数限制未来扩展 | 本期 YAGNI；多输入另开需求 |

## Migration Plan

- 纯新增 type，无数据迁移。
- 发布顺序：先后端 Handler，再前端 nodeMeta/表单；旧图不受影响。

## Open Questions

（无。brainstorming 已全部定稿。）
