## 1. 后端常量与工具

- [x] 1.1 在 `WfNodeType` 新增 `JSON_DESERIALIZE = "json-deserialize"`
- [x] 1.2 新增 `JsonDeserializeUtil`：parse、嵌套深度校验（≤3）、点路径 extract
- [x] 1.3 新增 `JsonDeserializeUtilTest`：整包解析、path 提取、超深/非法 JSON、path 缺失

## 2. 后端 Handler 与校验

- [x] 2.1 新增 `JsonDeserializeNodeHandler`：解析输入、组装 output、FAILED/SUCCESS 分支
- [x] 2.2 Handler trace：`inputPreview`、`outputKeys`、`fieldCount`
- [x] 2.3 新增 `JsonDeserializeNodeHandlerTest`
- [x] 2.4 新增 `JsonDeserializeDataUtil.validate`（输入必填、outputFields key 不重复）
- [x] 2.5 `WorkflowGraphValidator.validateNodeData` 接入 `json-deserialize`

## 3. 前端 nodeMeta 与注册

- [x] 3.1 `nodeMeta.js` 注册 `json-deserialize`（defaults、outputs、summarize、hasValidationWarning、tool 分类）
- [x] 3.2 `resolveNodeOutputs`：返回 `output`（object）；可选展开 `output.{key}`

## 4. 前端配置表单

- [x] 4.1 新增 `JsonDeserializeForm.vue`：单参数输入 + outputFields 表格
- [x] 4.2 实现「导入 JSON 示例」弹窗与字段自动生成（`jsonDeserializeUtils.js`）
- [x] 4.3 `NodeConfigPanel.vue` 注册 `'json-deserialize': JsonDeserializeForm`

## 5. 联调与验证

- [x] 5.1 构建：`mvn -pl quickboot-workflow install -DskipTests`、相关单测、`pnpm build:prod`
- [ ] 5.2 Debug 联调：HTTP body → 反序列化 → `{{json_deser_1.output.name}}`
- [x] 5.3 校验联调：无输入 / 重复 key 的 `json-deserialize` 节点 publish 失败
- [ ] 5.4 按 design spec §10 核对 TC_JSON_DES_001–010（手工）
