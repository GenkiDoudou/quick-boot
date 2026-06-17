# 工作流 JSON 反序列化节点 — 设计说明

**日期**：2026-06-14  
**状态**：已定稿（brainstorming 澄清结论）  
**依据**：`原始需求/JSON 反序列化节点.md`（扣子编程 JSON 反序列化节点）  
**澄清结论**：1:A、2:A、3:A、4:A、5:A、6:A、7:B  
**关联**：`docs/superpowers/specs/2026-06-14-json-serialize-node-design.md`（对称节点）、`docs/superpowers/specs/2026-06-07-ai-workflow-design.md`

---

## 1. 背景与目标

### 1.1 背景

低代码工作流中，HTTP 请求、LLM 等节点常输出 **JSON 格式字符串**；下游需要提取其中字段（如姓名、ID）供后续节点使用。目前用户需借助**代码节点**手写 `JSON.parse`，可视化程度低。

扣子编程提供 **JSON 反序列化节点**：将 JSON 字符串解析为对象，支持配置输出子字段、导入 JSON 示例自动填充字段表。

项目已实现对称的 **`json-serialize`** 节点（Object → JSON 字符串）。

### 1.2 目标（本期）

新增独立节点 type **`json-deserialize`**，对齐扣子核心行为：

| 能力 | 说明 |
|------|------|
| 输入 | 单个 JSON 字符串（`inputVariables` 单参数，可引用上游或固定值） |
| 输出 | 固定 `output`（object/array）；下游通过 `{{node.output}}` 或 `{{node.output.field}}` 引用 |
| 输出字段 | 可选 `outputFields`（key + 点路径 path + type）；未配置时输出整包解析结果 |
| JSON 示例 | 支持导入示例 JSON，自动生成 `outputFields` |
| 嵌套 | 点路径最深 **3 层**（如 `a.b.c`） |
| 异常 | 空输入 / 非法 JSON / 超深度 → 节点 **FAILED** |

### 1.3 非目标（本期不做）

- 数组下标路径（`items[0].id`）（P1）
- 与 `json-serialize` 合并为双模式单节点
- 失败时 SUCCESS + 空对象（与序列化节点策略不同）
- 批量反序列化（用批处理节点）
- `isSuccess` / `errorBody` 额外输出字段

---

## 2. 已定稿产品决策

| 题号 | 选项 | 结论 |
|------|------|------|
| 1 | A | 仅 `output` 对象；下游用 `{{node.output.xxx}}` 点路径引用 |
| 2 | A | 手动字段表 +「导入 JSON 示例」自动填充 |
| 3 | A | 未配置 `outputFields` 时，整包解析结果写入 `output` |
| 4 | A | 非法 JSON / 空输入 → 节点 **FAILED** |
| 5 | A | 嵌套深度最多 3 层 |
| 6 | A | `type=json-deserialize`，分类 `tool`，展示名「JSON 反序列化」 |
| 7 | B | 支持点路径（如 `data.user.name`） |

---

## 3. 方案对比与选型

| 方案 | 概要 | 优点 | 缺点 |
|------|------|------|------|
| **A（采用）** | 独立 `json-deserialize` type + Handler + 表单 | 与序列化对称；语义清晰；边界可控 | 多维护一个 type |
| B | 与 `json-serialize` 合并双模式 | 少一个 palette 项 | 输入/输出/失败策略差异大，表单复杂 |
| C | 代码节点预置 `JSON.parse` | 零后端 | 非可视化 |

**采用方案 A。**

---

## 4. 节点数据模型

### 4.1 节点 type 与展示

| 项 | 值 |
|----|-----|
| DSL `type` | `json-deserialize` |
| 设计器展示名 | **JSON 反序列化** |
| 分类 | `tool` |
| Handler | `JsonDeserializeNodeHandler` |
| 图标 / 色 | `#67c23a`，`DocumentCopy`（与序列化 `#409eff` 区分） |

### 4.2 节点 `data` 结构

```json
{
  "inputVariables": [
    { "key": "input", "value": "{{http_1.body}}" }
  ],
  "outputFields": [
    { "key": "name", "path": "data.user.name", "type": "string" },
    { "key": "age", "path": "data.user.age", "type": "number" }
  ]
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `inputVariables` | `{ key, value }[]` | **仅 1 行**；待解析 JSON 字符串 |
| `outputFields` | `{ key, path?, type? }[]` | 可选；`path` 缺省等于 `key`；`type` 为 string/number/boolean/object/array |

**默认值**：

```json
{
  "inputVariables": [{ "key": "input", "value": "" }],
  "outputFields": []
}
```

### 4.3 输出

| 字段 | 类型 | 说明 |
|------|------|------|
| `output` | object 或 array | 反序列化结果或按字段提取后的对象 |

**引用示例**：

- `{{json_deser_1.output}}`
- `{{json_deser_1.output.name}}`
- `{{json_deser_1.output.data.user.name}}`（整包输出时）

---

## 5. 反序列化规则

Handler 流程：

1. `InputParameterTemplateRenderer.resolveInputVariables` 解析输入
2. 取唯一参数值 `text`（须为 String；若为 Map 则先 `JSONUtil.toJsonStr` 再解析，或 FAILED——**采用**：非 String 且非空时尝试 `String.valueOf` 后解析）
3. 校验非空、合法 JSON
4. `JSONUtil.parse` 得根值 `root`
5. 校验 JSON 文档嵌套深度 ≤ 3
6. 组装 `output`（见下表）
7. `NodeResult.success`

| `outputFields` | 根类型 | `output` |
|----------------|--------|----------|
| 空 | object 或 array | `root` 原样 |
| 非空 | **必须** object | `{ key: extract(root, path) }`；path 不存在 → 该 key 为 `null` |
| 非空 | array | **FAILED**（无法按字段表提取） |

**嵌套深度定义**：从根对象起算，点路径 `a.b.c` 为 3 层；校验解析后整棵 JSON 树最大深度 ≤ 3（object/array 嵌套层数）。

**点路径解析**：按 `.` 分段，在 Map 上逐级 `get`；中间非 object 则返回 `null`。

**类型字段 `type`**：表单展示与文档用途；运行时保留 JSON 原生类型，不做强制转型失败。

---

## 6. 前端设计器

### 6.1 表单 `JsonDeserializeForm.vue`

| 区块 | 控件 |
|------|------|
| 输入 | `WfVariableTableSection` + `ConditionValueField`（单参数，对齐 `JsonSerializeForm`） |
| 输出字段 | 表格：key、path（点路径）、type；增删行 |
| 导入 JSON 示例 | 按钮 → 弹窗 textarea → 解析 object 根 → 生成 `outputFields`（深度 ≤ 3） |
| 输出说明 | 只读：`output`（object）；列出已配置 key |

**导入示例规则**：

- 根须为 JSON object；否则提示错误
- 遍历 object 树，为深度 ≤ 3 的节点生成字段（叶子优先；object 中间节点可选生成 type=object 行——**采用**：仅生成叶子 primitive 字段，path 为完整点路径）

**校验警告**：输入未配置；`outputFields` 存在空 `key` 或重复 `key`。

### 6.2 注册

| 文件 | 变更 |
|------|------|
| `nodeMeta.js` | defaults、outputs、summarize、hasValidationWarning |
| `NodeConfigPanel.vue` | `'json-deserialize': JsonDeserializeForm` |
| `resolveNodeOutputs` | 返回 `[{ key: 'output', type: 'object' }]`；若已配置 `outputFields`，额外展开 `{ key: 'output.' + field.key, ... }` 供变量树（可选增强） |

### 6.3 画布摘要

- 已配置：`反序列化 · N 个字段` 或 `反序列化 · 整包输出`
- 未配置输入：`反序列化 · 未配置输入`

---

## 7. 后端引擎

### 7.1 组件

| 组件 | 职责 |
|------|------|
| `WfNodeType.JSON_DESERIALIZE` | 常量 |
| `JsonDeserializeUtil` | parse、depth 校验、path extract |
| `JsonDeserializeDataUtil` | 图校验：输入必填；outputFields key 不重复 |
| `JsonDeserializeNodeHandler` | 编排执行、FAILED/SUCCESS |
| `WorkflowGraphValidator` | 复用输入校验模式 |

### 7.2 失败场景

| 场景 | 节点状态 | 说明 |
|------|----------|------|
| 输入空 | FAILED | |
| 非法 JSON | FAILED | errorMessage 含解析异常摘要 |
| 嵌套 > 3 层 | FAILED | |
| 有 outputFields 但根非 object | FAILED | |
| path 缺失 | SUCCESS | 对应 key = null |

### 7.3 Trace

- `inputPreview`（截断 500 字符）
- `outputKeys`（output 顶层 key 列表）
- `fieldCount`

---

## 8. 数据流示例

```
[HTTP] body: "{\"data\":{\"user\":{\"name\":\"张三\",\"age\":18}}}"
        ↓
[JSON 反序列化] input = {{http_1.body}}
  outputFields: [
    { key: "name", path: "data.user.name", type: "string" },
    { key: "age", path: "data.user.age", type: "number" }
  ]
        ↓
output: { "name": "张三", "age": 18 }

下游: {{json_deser_1.output.name}} → 张三
```

---

## 9. 与 JSON 序列化节点对比

| 项 | json-serialize | json-deserialize |
|----|----------------|------------------|
| 输入 | 任意结构 | JSON 字符串 |
| 输出 | `output` string | `output` object/array |
| 失败 | SUCCESS + `""` | **FAILED** |
| 字段配置 | 无 | 可选 `outputFields` |

---

## 10. 测试用例

| ID | 场景 | 期望 |
|----|------|------|
| TC_JSON_DES_001 | `{"name":"张三"}` 无 fields | `output` 为完整对象 |
| TC_JSON_DES_002 | path `name` | `output.name` = 张三 |
| TC_JSON_DES_003 | path `data.user.name`（3 层） | 正确提取 |
| TC_JSON_DES_004 | JSON 树深度 > 3 | FAILED |
| TC_JSON_DES_005 | 非法 JSON | FAILED |
| TC_JSON_DES_006 | 空输入 | FAILED |
| TC_JSON_DES_007 | 导入示例生成 fields | 表单回显正确 |
| TC_JSON_DES_008 | 有 fields 但根为 array | FAILED |
| TC_JSON_DES_009 | path 不存在 | SUCCESS，key = null |
| TC_JSON_DES_010 | 图校验：无输入 | save/publish 失败 |

---

## 11. 实现清单（供 OpenSpec / writing-plans）

### 后端

- [ ] `WfNodeType.JSON_DESERIALIZE`
- [ ] `JsonDeserializeUtil` + 单测（parse、depth、extract）
- [ ] `JsonDeserializeNodeHandler` + 单测
- [ ] `JsonDeserializeDataUtil` + `WorkflowGraphValidator`

### 前端

- [ ] `JsonDeserializeForm.vue`（输入 + 字段表 + 导入示例）
- [ ] `nodeMeta.js`、`NodeConfigPanel.vue`
- [ ] `resolveNodeOutputs` 增强（可选）

### 验证

- [ ] `mvn -pl quickboot-workflow -am compile`、单测
- [ ] `pnpm build:prod`
- [ ] 手工：HTTP body → 反序列化 → 下游 `{{node.output.xxx}}`

---

## 12. 自检记录

| 检查项 | 结果 |
|--------|------|
| TBD / TODO 占位 | 无 |
| 与序列化 spec 矛盾 | 无（失败策略差异已写明） |
| 范围可单 plan 完成 | 是 |
| 歧义点 | 已明确：有 fields 时根须 object；path 缺失为 null 非 FAILED；深度按整树与 path 双层约束 |
