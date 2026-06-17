# 工作流 JSON 序列化节点 — 设计说明

**日期**：2026-06-14  
**状态**：已定稿（brainstorming 澄清结论）  
**依据**：`原始需求/JSON 序列化节点.md`（扣子编程 JSON 序列化节点）  
**澄清结论**：1:A、2:A、3:A、4:A、5:A、6:A  
**关联**：`docs/superpowers/specs/2026-06-07-ai-workflow-design.md`（工作流引擎与节点体系）

---

## 1. 背景与目标

### 1.1 背景

低代码工作流中，上游节点常输出 Object、Array 等结构化数据；下游节点（如数据库写入、HTTP 请求体、文本模板）往往需要 **JSON 格式的字符串**。目前用户需借助**代码节点**手写 `JSON.stringify`，可视化程度低、门槛高。

扣子编程已提供独立的 **JSON 序列化节点**：将变量转为 JSON 字符串，固定输出 `output`（String）。

### 1.2 目标（本期）

新增独立节点 type **`json-serialize`**，对齐扣子核心行为：

| 能力 | 说明 |
|------|------|
| 输入 | 单个输入参数（`inputVariables` 表格，参数名可编辑，值可引用上游或填固定内容） |
| 输出 | 固定 `output`（String），紧凑 JSON 字符串 |
| 字符串透传 | 输入已是合法 JSON 文本时，**原样输出**，不二次 stringify |
| 异常 | 空值 / 不可序列化 → `output=""`，节点 **SUCCESS**（不阻断工作流） |
| 分类 | 节点库 `tool` 分类，展示名「JSON 序列化」 |

### 1.3 非目标（本期不做）

- JSON **反序列化**节点（见 `原始需求/JSON 反序列化节点.md`，另开 spec）
- pretty print、Unicode 转义、缩进等格式开关
- 多输入参数合并为一个 JSON 对象
- 失败时节点 `FAILED` 或输出 `isSuccess` / `errorBody`
- 并入 `text-process` 作为子模式

---

## 2. 已定稿产品决策

| 题号 | 选项 | 结论 |
|------|------|------|
| 1 | A | 本期仅 JSON 序列化节点 |
| 2 | A | `inputVariables` 单参数表格 + `ConditionValueField`（对齐代码节点输入区） |
| 3 | A | 已是合法 JSON 字符串 → 原样透传 |
| 4 | A | 空 / null / 不可序列化 → `output=""`，SUCCESS |
| 5 | A | `type=json-serialize`，分类 `tool`，展示名「JSON 序列化」 |
| 6 | A | 紧凑单行 `JSONUtil.toJsonStr`，无 pretty print |

---

## 3. 方案对比与选型

| 方案 | 概要 | 优点 | 缺点 |
|------|------|------|------|
| **A（采用）** | 独立 `json-serialize` type + 专用 Handler + 表单 | 语义清晰；与扣子一致；边界小、易测 | 多维护一个 type |
| B | 并入 `text-process` 第三模式 | 少一个 palette 项 | 与拼接/分隔混杂；输出语义不同 |
| C | 代码节点预置模板 | 零后端 | 非可视化；不符合产品定位 |

**采用方案 A。**

---

## 4. 节点数据模型

### 4.1 节点 type 与展示

| 项 | 值 |
|----|-----|
| DSL `type` | `json-serialize` |
| 设计器展示名 | **JSON 序列化** |
| 分类 | `tool` |
| Handler | `JsonSerializeNodeHandler` |
| 图标 / 色 | `#409eff`，`Document`（或与工具类节点统一的 `Connection`） |

### 4.2 节点 `data` 结构

```json
{
  "inputVariables": [
    { "key": "input", "value": "{{llm_1.output}}" }
  ]
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `inputVariables` | `{ key, value }[]` | **仅允许 1 行**；`value` 为上游引用 `{{node.field}}` 或固定文本（含固定 JSON 文本） |

**默认值**：`[{ "key": "input", "value": "" }]`

### 4.3 输出

| 字段 | 类型 | 说明 |
|------|------|------|
| `output` | string | JSON 序列化结果；失败或空输入时为 `""` |

下游引用示例：`{{json_1.output}}`

---

## 5. 序列化规则

Handler 通过 `InputParameterTemplateRenderer.resolveInputVariables` 解析输入，取**唯一参数**（第一行）的值 `value`，再执行 `serialize(value)`：

| 输入 `value` | `output` |
|--------------|----------|
| `null` / 参数缺失 | `""` |
| `String`，trim 后为空 | `""` |
| `String`，且为**合法 JSON 文本** | **原样返回**（trim 后，不二次 stringify） |
| `String`，普通非 JSON 文本 | `JSONUtil.toJsonStr(value)` → 带引号的 JSON 字符串 |
| `Map` / `List` / `Number` / `Boolean` 等 | `JSONUtil.toJsonStr(value)`，紧凑单行 |
| 其他不可序列化类型 | `""` |

**合法 JSON 文本判定**（满足其一即可）：

1. `JSONUtil.isTypeJSON(str)` 为 true；或  
2. `JSONUtil.parse(str)` 不抛异常（用于 `"hello"`、`123`、`true`、`null` 等 JSON 字面量字符串）

**透传目的**：避免 HTTP `body` 等已是 JSON 字符串的字段被二次转义为 `"\"{...}\""`。

---

## 6. 前端设计器

### 6.1 表单 `JsonSerializeForm.vue`

- **输入**：`WfVariableTableSection` + `ConditionValueField`（复用 `CodeForm` 输入区模式）
  - 最多 **1** 个参数行：已有 1 行时隐藏「添加」按钮
  - 参数名默认 `input`，可编辑
- **输出**：只读区展示 `output (string)` 及简短说明
- **校验警告**（`hasValidationWarning`）：`key` 或 `value` 任一为空

### 6.2 注册与元数据

| 文件 | 变更 |
|------|------|
| `nodeMeta.js` | 新增 `json-serialize` 条目：defaults、outputs、summarize、hasValidationWarning |
| `NodeConfigPanel.vue` | `'json-serialize': JsonSerializeForm` |
| `resolveNodeOutputs`（`nodeMeta.js`） | 返回 `[{ key: 'output', type: 'string' }]` |

### 6.3 画布摘要

- 已配置：`序列化 · {{input}}` 或 `序列化 · input`
- 未配置：`序列化 · 未配置输入`

### 6.4 容器与 palette

- 出现在节点库 `tool` 分组
- 允许：主画布、循环体、批处理体（与 `text-process` 相同，无分支 handle）
- **不**加入 `LOOP_OUTPUT_NODE_TYPES`（非循环体专用输出节点）

---

## 7. 后端引擎

### 7.1 常量与 Handler

```java
// WfNodeType.java
public static final String JSON_SERIALIZE = "json-serialize";
```

`JsonSerializeNodeHandler`：

1. 读取并解析 `inputVariables`
2. 取第一个有效 `key` 对应值
3. 调用 `serialize(value)`
4. `NodeResult.success(Map.of("output", result))`

### 7.2 依赖

- `InputParameterTemplateRenderer`（输入解析）
- `cn.hutool.json.JSONUtil`（序列化与 JSON 合法性判断）

### 7.3 图校验（可选，建议 P0）

在 `WorkflowGraphValidator.validateNodeData` 中：

- `json-serialize` 节点至少 1 个 `inputVariables` 项，且 `key`、`value` 非空

### 7.4 Trace

`wf_run_step` trace inputs 建议包含：

- `inputKey`：参数名
- `inputPreview`：截断后的输入值预览（如 500 字符）

不在 outputs 增加 `reason`；异常信息仅写入 trace（若引擎支持）。

---

## 8. 数据流示例

```
[LLM] output: { "name": "张三", "age": 18 }   // Object
        ↓
[JSON 序列化] input = {{llm_1.output}}
        ↓
output: "{\"name\":\"张三\",\"age\":18}"

[HTTP] body: "{\"code\":0}"                   // 已是 JSON 字符串
        ↓
[JSON 序列化] input = {{http_1.body}}
        ↓
output: "{\"code\":0}"                         // 透传
```

---

## 9. 测试用例

| ID | 场景 | 期望 |
|----|------|------|
| TC_JSON_SER_001 | Object 输入 | 紧凑 JSON 字符串 |
| TC_JSON_SER_002 | Array 输入 | 紧凑 JSON 数组字符串 |
| TC_JSON_SER_003 | 已是 JSON 对象字符串 `{"a":1}` | 原样透传 |
| TC_JSON_SER_004 | 普通文本 `hello` | `"\"hello\""` |
| TC_JSON_SER_005 | null / 空字符串 | `""`，SUCCESS |
| TC_JSON_SER_006 | 固定值配置 `{"x":1}` | 合法 JSON 透传 |
| TC_JSON_SER_007 | 设计器保存/加载 | `inputVariables` 正确回显 |
| TC_JSON_SER_008 | 图校验：无输入配置 | 保存/发布时报错（若启用校验） |

---

## 10. 实现清单（供 writing-plans 拆分）

### 后端

- [ ] `WfNodeType.JSON_SERIALIZE`
- [ ] `JsonSerializeNodeHandler` + 单元测试（序列化规则表）
- [ ] `WorkflowGraphValidator` 可选校验

### 前端

- [ ] `nodeMeta.js` 注册
- [ ] `JsonSerializeForm.vue`
- [ ] `NodeConfigPanel.vue` 映射

### 验证

- [ ] `mvn -pl quickboot-workflow -am compile`
- [ ] `pnpm build:prod`
- [ ] 手工：Object → 序列化 → 下游引用 `{{node.output}}`

---

## 11. 自检记录

| 检查项 | 结果 |
|--------|------|
| TBD / TODO 占位 | 无 |
| 章节自相矛盾 | 无（单输入、单输出、透传与空值规则一致） |
| 范围是否可单 plan 完成 | 是，约 6–8 个文件 |
| 歧义点 | 已明确：仅 1 个 inputVariables 行；透传仅针对合法 JSON 文本 |
