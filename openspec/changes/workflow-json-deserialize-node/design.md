## Context

- 依据：`docs/superpowers/specs/2026-06-14-json-deserialize-node-design.md`（brainstorming 定稿 1:A–7:B）。
- 现状：已实现 `json-serialize`；无 JSON 反序列化节点。可复用 `InputParameterTemplateRenderer`、`JSONUtil`、`JsonSerializeForm` 输入区模式。
- 约束：单输入参数；输出仅 `output`（object/array）；失败 **FAILED**；点路径最深 3 层。

## Goals / Non-Goals

**Goals:**

- 新增 `json-deserialize`：JSON 字符串 → `output` 对象/数组。
- 可选 `outputFields`（key + path + type）；导入 JSON 示例生成字段表。
- 嵌套深度 ≤ 3；非法 JSON / 空输入 / 有 fields 但根非 object → FAILED。
- 设计器 `tool` 分类，与序列化节点对称。

**Non-Goals:**

- 数组下标路径、双模式合并、SUCCESS 失败兜底、`isSuccess`/`errorBody`

## Decisions

### 1. 独立 type `json-deserialize`（采用）

对称 `json-serialize`；失败策略与输出类型不同，不宜合并。

### 2. data 模型

```json
{
  "inputVariables": [{ "key": "input", "value": "{{http_1.body}}" }],
  "outputFields": [
    { "key": "name", "path": "data.user.name", "type": "string" }
  ]
}
```

- `outputFields` 为空 → `output` = 解析根值（object 或 array）
- `outputFields` 非空 → 根 MUST 为 object；`output` = `{ key: extract(path) }`；path 缺失 → `null`

### 3. 反序列化规则

| 场景 | 结果 |
|------|------|
| 空/非法 JSON | FAILED |
| 树深度 > 3 | FAILED |
| 有 fields，根为 array | FAILED |
| path 不存在 | SUCCESS，key=null |

非 String 输入：转 `String` 后按 JSON 文本解析；仍非法则 FAILED。

### 4. 后端落点

| 组件 | 职责 |
|------|------|
| `WfNodeType.JSON_DESERIALIZE` | 常量 |
| `JsonDeserializeUtil` | parse、depth、extractByPath |
| `JsonDeserializeNodeHandler` | 编排、FAILED/SUCCESS |
| `JsonDeserializeDataUtil` | 图校验 |
| `WorkflowGraphValidator` | 输入必填、key 不重复 |

Trace：`inputPreview`、`outputKeys`、`fieldCount`。

### 5. 前端落点

| 文件 | 变更 |
|------|------|
| `JsonDeserializeForm.vue` | 输入 + 字段表 + 导入示例弹窗 |
| `nodeMeta.js` | `#67c23a`，`DocumentCopy` |
| `NodeConfigPanel.vue` | 表单注册 |
| `resolveNodeOutputs` | `output` object；可选展开 `output.{key}` |

## Risks / Trade-offs

| 风险 | 缓解 |
|------|------|
| 深度计算与扣子理解不一致 | 单测覆盖 3 层边界 |
| 导入示例生成字段过多 | 仅叶子 primitive，深度 ≤ 3 |
| 与序列化失败策略不一致 | 文档与 nodeMeta 说明区分 |

## Migration Plan

- 纯新增 type，无迁移。
- 建议发布顺序：后端 Util/Handler → 前端表单 → 联调 HTTP body 场景。

## Open Questions

（无。）
