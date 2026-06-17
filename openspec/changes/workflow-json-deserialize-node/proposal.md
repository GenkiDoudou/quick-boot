## Why

低代码工作流中，HTTP 请求、LLM 等节点常输出 **JSON 格式字符串**，下游需提取字段供后续节点使用。当前用户只能借助**代码节点**手写 `JSON.parse`，可视化差、门槛高。扣子编程已提供 **JSON 反序列化节点**；项目已实现对称的 `json-serialize` 节点后，补齐反序列化可完成 JSON 数据桥接闭环。

## What Changes

- 新增节点 type **`json-deserialize`**，设计器展示名「JSON 反序列化」，分类 `tool`。
- 节点 `data`：`inputVariables`（**仅 1 行**）+ 可选 `outputFields`（key、点路径 path、type）。
- 节点输出：固定 `output`（object/array）；未配置 `outputFields` 时整包解析；配置后按 path 提取子集。
- 支持「导入 JSON 示例」自动生成 `outputFields`（前端）。
- 后端新增 `JsonDeserializeNodeHandler`：解析 JSON 字符串、嵌套深度 ≤ 3、非法/空输入 → **FAILED**（与序列化节点策略不同）。
- `WorkflowGraphValidator`：`json-deserialize` 须配置有效 `inputVariables`；`outputFields` 的 `key` 不重复。
- 前端：`JsonDeserializeForm.vue`、`nodeMeta.js`、`NodeConfigPanel` 注册。

**非本期**：数组下标路径 `items[0].id`、与序列化合并双模式、失败时 SUCCESS + 空对象、`isSuccess`/`errorBody`。

## Capabilities

### New Capabilities

（无。本变更在既有工作流引擎与设计器能力上新增单节点类型。）

### Modified Capabilities

- `workflow-engine`：新增 `json-deserialize` Handler、反序列化/深度/提取规则、图校验。
- `workflow-design-ui`：节点库 `tool` 分组、配置表单（含导入 JSON 示例）、nodeMeta 与 `resolveNodeOutputs`。

## Impact

- **后端**：`quickboot-workflow` — `WfNodeType`、`JsonDeserializeUtil`、`JsonDeserializeNodeHandler`、`JsonDeserializeDataUtil`、`WorkflowGraphValidator`；复用 `InputParameterTemplateRenderer`、`JSONUtil`。
- **前端**：`quick-ui/src/views/workflow/design/` — `JsonDeserializeForm.vue`、`nodeMeta.js`、`NodeConfigPanel.vue`。
- **数据库**：无表结构变更。
- **API**：无新接口；`validateGraph` / `publish` 对未配置输入的 `json-deserialize` 节点校验失败。
- **兼容性**：纯新增 type；与 `json-serialize` 对称，无破坏性变更。
