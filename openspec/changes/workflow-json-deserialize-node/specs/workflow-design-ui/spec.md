## ADDED Requirements

### Requirement: JSON 反序列化节点配置表单

设计器 MUST 为 `json-deserialize` 提供专用表单（展示名「JSON 反序列化」），包含：

| 区块 | 说明 |
|------|------|
| 输入 | `WfVariableTableSection` + `ConditionValueField`；**最多 1 个参数行** |
| 输出字段 | 表格：key、path（点路径）、type（string/number/boolean/object/array）；支持增删 |
| 导入 JSON 示例 | 按钮打开弹窗；粘贴 JSON object 示例后自动生成 `outputFields`（仅叶子 primitive，深度 ≤ 3） |
| 输出说明 | 只读：`output`（object）；列出已配置 key |

表单 MUST 在 `hasValidationWarning` 时提示：输入未配置，或 `outputFields` 存在空/重复 `key`。

#### Scenario: 单参数输入

- **WHEN** 用户打开 JSON 反序列化配置面板
- **THEN** 显示一个输入参数行，且无第二行「添加」按钮

#### Scenario: 导入 JSON 示例生成字段

- **WHEN** 用户粘贴示例 `{"data":{"user":{"name":"张三","age":18}}}` 并确认导入
- **THEN** `outputFields` 自动填充含 `name`、`age` 等叶子字段及对应点路径

#### Scenario: 导入非 object 根失败

- **WHEN** 用户导入 JSON 数组示例 `[1,2]`
- **THEN** 提示根须为 JSON 对象，不修改现有 `outputFields`

### Requirement: JSON 反序列化节点元数据与节点库

`nodeMeta.js` MUST 注册 `json-deserialize`：

| 项 | 值 |
|----|-----|
| 展示名 | JSON 反序列化 |
| 分类 | `tool` |
| 默认 data | `inputVariables: [{ key: "input", value: "" }]`, `outputFields: []` |
| outputs | `[{ key: "output", type: "object" }]` |

左栏节点面板 MUST 在 **工具** 分组展示；与「JSON 序列化」相邻展示。

画布摘要 SHOULD：`反序列化 · N 个字段` 或 `反序列化 · 整包输出`；未配置输入时 `反序列化 · 未配置输入`。

`resolveNodeOutputs` SHOULD 在配置了 `outputFields` 时，为变量树提供 `output.{key}` 子项引用提示。

#### Scenario: 节点库可见

- **WHEN** 用户展开「工具」分组
- **THEN** 列表包含「JSON 反序列化」

#### Scenario: 下游引用 output 子字段

- **WHEN** 节点 `json_deser_1` 已配置 `outputFields` 含 key `name`
- **THEN** 下游变量选择器可引用 `json_deser_1.output.name`
