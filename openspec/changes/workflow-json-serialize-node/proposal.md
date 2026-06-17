## Why

低代码工作流中，上游节点常输出 Object、Array 等结构化数据，而下游（数据库 String 字段、HTTP 请求体、文本模板等）往往需要 **JSON 格式字符串**。当前用户只能借助**代码节点**手写 `JSON.stringify`，可视化差、门槛高。扣子编程已提供独立 **JSON 序列化节点**；在设计器节点体系已较完善后，补齐该能力可降低数据桥接成本，且无需用户编写脚本。

## What Changes

- 新增节点 type **`json-serialize`**，设计器展示名「JSON 序列化」，分类 `tool`。
- 节点 `data`：`inputVariables`（**仅 1 行**，参数名默认 `input`，值可引用上游或填固定文本）。
- 节点输出：固定 `output`（String），紧凑 JSON 字符串。
- 后端新增 `JsonSerializeNodeHandler`：解析输入 → 序列化规则（合法 JSON 字符串透传；Object/Array 等 `JSONUtil.toJsonStr`；空/异常 → `""` 且 SUCCESS）。
- `WorkflowGraphValidator`：`json-serialize` 须配置有效 `inputVariables`（`key`、`value` 非空）。
- 前端：`JsonSerializeForm.vue`、`nodeMeta.js` 注册、`NodeConfigPanel` 映射。

**非本期**：JSON 反序列化节点、pretty print 开关、多输入合并序列化、失败时 `FAILED` 状态。

## Capabilities

### New Capabilities

（无。本变更在既有工作流引擎与设计器能力上新增单节点类型。）

### Modified Capabilities

- `workflow-engine`：新增 `json-serialize` Handler、序列化规则、图校验（输入必填）。
- `workflow-design-ui`：节点库 `tool` 分组、配置表单、nodeMeta 元数据与输出声明。

## Impact

- **后端**：`quickboot-workflow` — `WfNodeType`、`JsonSerializeNodeHandler`、`WorkflowGraphValidator`；复用 `InputParameterTemplateRenderer`、`JSONUtil`。
- **前端**：`quick-ui/src/views/workflow/design/` — `JsonSerializeForm.vue`、`nodeMeta.js`、`NodeConfigPanel.vue`。
- **数据库**：无表结构变更。
- **API**：无新接口；`validateGraph` / `publish` 对未配置输入的 `json-serialize` 节点校验失败。
- **兼容性**：纯新增 type，不破坏既有图。
