## 1. 后端常量与 Handler

- [x] 1.1 在 `WfNodeType` 新增 `JSON_SERIALIZE = "json-serialize"`
- [x] 1.2 新增 `JsonSerializeNodeHandler`：解析 `inputVariables`、实现 `serialize(value)` 规则表、返回 `output`
- [x] 1.3 Handler trace：`inputKey`、`inputPreview`（截断 500 字符）
- [x] 1.4 新增 `JsonSerializeNodeHandlerTest`（或等价单测）：Object、Array、JSON 透传、普通文本、null/空

## 2. 后端图校验

- [x] 2.1 在 `WorkflowGraphValidator.validateNodeData` 校验 `json-serialize`：`inputVariables` 至少一行且 `key`、`value` 非空

## 3. 前端 nodeMeta 与注册

- [x] 3.1 在 `nodeMeta.js` 注册 `json-serialize`：defaults、outputs、summarize、hasValidationWarning、分类 `tool`
- [x] 3.2 确认 `resolveNodeOutputs` 对 `json-serialize` 返回 `output`（string）

## 4. 前端配置表单

- [x] 4.1 新增 `JsonSerializeForm.vue`：单参数 `inputVariables` 表格 + 只读输出区（对齐 CodeForm 输入区，隐藏第二行添加）
- [x] 4.2 在 `NodeConfigPanel.vue` 注册 `'json-serialize': JsonSerializeForm`

## 5. 联调与验证

- [x] 5.1 构建：`mvn -pl quickboot-workflow -am compile`、`pnpm build:prod`
- [ ] 5.2 Debug 联调：Object → 序列化 → 下游 `{{json_1.output}}`；HTTP body JSON 字符串透传
- [x] 5.3 校验联调：未配置输入的 `json-serialize` 节点 save/publish 失败（`WorkflowGraphValidatorTest.validate_jsonSerializeMissingInput_throws`）
- [ ] 5.4 按 design spec §9 核对 TC_JSON_SER_001–008（手工）
