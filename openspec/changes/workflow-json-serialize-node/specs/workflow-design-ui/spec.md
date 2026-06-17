## ADDED Requirements

### Requirement: JSON 序列化节点配置表单

设计器 MUST 为 `json-serialize` 提供专用表单（展示名「JSON 序列化」），包含：

| 区块 | 说明 |
|------|------|
| 输入 | `WfVariableTableSection` + `ConditionValueField`；**最多 1 个参数行**（已有 1 行时隐藏「添加」）；参数名默认 `input`，可编辑 |
| 输出 | 只读展示固定字段 `output`（string）及说明 |

表单 MUST 在 `hasValidationWarning` 时于节点卡片提示：`key` 或 `value` 任一为空。

#### Scenario: 单参数输入配置

- **WHEN** 用户打开 JSON 序列化节点配置面板
- **THEN** 显示一个输入参数行（参数名 + 取值），且不显示「添加」第二行按钮

#### Scenario: 引用上游变量

- **WHEN** 用户在输入取值框通过变量选择器选择 `llm_1.output`
- **THEN** 取值写入 `{{llm_1.output}}` 并保存至节点 `inputVariables`

### Requirement: JSON 序列化节点元数据与节点库

`nodeMeta.js` MUST 注册 `json-serialize`：

| 项 | 值 |
|----|-----|
| 展示名 | JSON 序列化 |
| 分类 | `tool` |
| 默认 data | `inputVariables: [{ key: "input", value: "" }]` |
| outputs | `[{ key: "output", type: "string" }]` |

左栏节点面板 MUST 在 **工具** 分组展示该节点；搜索 MUST 可按名称「JSON 序列化」命中。

画布节点摘要 SHOULD 为 `序列化 · {参数名或取值预览}`；未配置输入时 SHOULD 为 `序列化 · 未配置输入`。

#### Scenario: 节点库工具分组可见

- **WHEN** 用户展开左栏「工具」分组
- **THEN** 列表包含「JSON 序列化」节点，可拖拽至画布

#### Scenario: 下游变量树含 output

- **WHEN** 画布存在已配置的 `json-serialize` 节点 `json_1`
- **THEN** 下游节点变量选择器可引用 `json_1.output`
