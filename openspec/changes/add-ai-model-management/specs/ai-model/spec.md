## ADDED Requirements

### Requirement: 大模型元数据（ai_model）

系统 MUST 在 MySQL 持久化大模型配置（表 `ai_model`），至少包含：`model_id`、`name`、`code`（唯一）、`description`、`model_type`（`CHAT`/`EMBEDDING`）、`provider`（`OPENAI_COMPAT`/`OLLAMA`）、`base_url`、`api_key_type`（`PLAIN`/`SECRET`/`ENV_REF`）、`api_key`、`model_name`、`completions_path`、`embeddings_path`、`dimensions`（EMBEDDING 必填）、`temperature`、`max_tokens`、`request_timeout_ms`（默认 60000）、`default_slot`（`CHAT`/`EMBEDDING`/`WORKFLOW_CHAT`，可为空）、`status`（0 正常 / 1 停用）、`last_test_status`（`SUCCESS`/`FAILED`/`UNTESTED`）、`last_test_msg`、`last_test_time`、标准审计字段与 `deleted`。

#### Scenario: 新增 OpenAI 兼容 Chat 模型成功
- **WHEN** 具备 `ai:model:add` 的用户提交唯一 `code`、合法 `base_url` 与 `model_name`
- **THEN** 数据库新增 `deleted=0` 记录且返回 `modelId`

#### Scenario: code 唯一约束
- **WHEN** 用户提交已存在的 `code`
- **THEN** 请求 MUST 失败并返回可识别业务错误

#### Scenario: EMBEDDING 必须填写 dimensions
- **WHEN** 用户新增 `model_type=EMBEDDING` 但未填 `dimensions`
- **THEN** 请求 MUST 失败并提示 dimensions 必填

### Requirement: API Key 密钥安全

`api_key_type=SECRET` MUST 以 SM4 密文存储；`ENV_REF` MUST 仅存环境变量名。列表接口 MUST 对 SECRET 脱敏为 `******`；详情 `/getInfo` 默认 `revealSecrets=false` 脱敏，`revealSecrets=true` 需 `ai:model:query` 且记 operlog。

#### Scenario: SECRET 入库加密
- **WHEN** 用户新增 `api_key_type=SECRET` 的模型
- **THEN** 库中 `api_key` MUST 以 `{sm4:...}` 形式存储且列表返回脱敏值

#### Scenario: ENV_REF 运行时解析
- **WHEN** 连接测试或业务调用时存在 `ENV_REF` 且进程环境变量已设置
- **THEN** 系统 MUST 使用 `System.getenv` 解析后的值发起 API 请求

### Requirement: 大模型管理 API

系统 SHALL 提供以下接口（前缀 `/ai/model`），修改/删除使用 `@PostMapping`：

| 路径 | 方法 | 权限 |
|------|------|------|
| `/list` | GET | `ai:model:list` |
| `/getInfo` | GET | `ai:model:query` |
| `/add` | POST | `ai:model:add` |
| `/update` | POST | `ai:model:edit` |
| `/remove` | POST | `ai:model:remove` |
| `/test` | POST | `ai:model:test` |
| `/setDefault` | POST | `ai:model:edit` |
| `/clearDefault` | POST | `ai:model:edit` |
| `/export` | GET | `ai:model:export` |
| `/options` | GET | `ai:model:list` |
| `/importFromYaml` | POST | `ai:model:add` |

`/update` 时 SECRET 类型 `api_key` 空串 MUST 表示不修改原值。`/remove` MUST 逻辑删除并驱逐 Registry 缓存；若模型占 `default_slot` 或被 KB/WF 引用 MUST 拒绝删除。

#### Scenario: 分页列表按类型筛选
- **WHEN** 用户请求 `/list?modelType=CHAT`
- **THEN** 仅返回 `model_type=CHAT` 的未删除记录

#### Scenario: 下拉选项按类型过滤
- **WHEN** 用户请求 `/options?modelType=EMBEDDING`
- **THEN** 仅返回启用状态的 EMBEDDING 模型

### Requirement: 大模型连接测试

`POST /ai/model/test` MUST 对指定 `modelId` 执行 probe 并更新 `last_test_*`。CHAT MUST 发送极简 prompt 并验证非空回复；EMBEDDING MUST 执行单向量 embed 且返回维度等于配置的 `dimensions`。失败 MUST NOT 修改 `status`，仅写 `last_test_status=FAILED`。

#### Scenario: Chat 测试成功
- **WHEN** Chat 模型可达且返回有效回复
- **THEN** `last_test_status=SUCCESS` 且响应 `success=true`

#### Scenario: Embedding 维度不匹配
- **WHEN** embed 返回维度与库表 `dimensions` 不一致
- **THEN** 测试 MUST 失败且 `message` 说明维度不匹配

### Requirement: 全局默认 slot

系统 MUST 支持三种全局默认：`default_slot=CHAT`、`EMBEDDING`、`WORKFLOW_CHAT`。设新默认时 MUST 清除同 slot 旧记录的 `default_slot`。每种 slot 在启用模型中 MUST 至多一条。

#### Scenario: 设为默认 Chat
- **WHEN** 管理员对启用中的 Chat 模型调用 `setDefault` 且 `defaultSlot=CHAT`
- **THEN** 该模型 `default_slot=CHAT` 且原 CHAT 默认被清除

#### Scenario: 停用默认模型后解析回落
- **WHEN** 占 `default_slot=CHAT` 的模型被停用
- **THEN** `resolveChat` MUST 跳过该记录并回落 YAML Bean（若 `fallback-to-yaml=true`）

### Requirement: Embedding 维度校验

当 `model_type=EMBEDDING` 且（设置 `default_slot=EMBEDDING` 或被知识库 `embedding_model_id` 引用）时，`dimensions` MUST 等于 `qc.knowledge.vectorDimensions`，否则 MUST 拒绝保存。

#### Scenario: 维度与 PGVector 不一致拒绝保存
- **WHEN** `vectorDimensions=768` 且用户保存 `dimensions=1536` 的 Embedding 为全局默认
- **THEN** 请求 MUST 失败并提示维度不一致

### Requirement: 动态模型 Registry

当 `qc.ai.enabled=true` 时，系统 MUST 提供 `AiModelRegistry` 与 `AiModelResolver`：按 `model_id` 缓存 Spring AI 模型实例；配置变更时 `evict`；支持 OpenAI 兼容与 Ollama 两种 Provider 构建 ChatModel / EmbeddingModel。

#### Scenario: 更新配置后缓存失效
- **WHEN** 管理员修改模型的 `base_url` 并保存
- **THEN** 后续连接测试 MUST 使用新参数而非旧缓存

#### Scenario: YAML 兜底
- **WHEN** DB 无可用默认且 `qc.ai.registry.fallback-to-yaml=true`
- **THEN** Resolver MUST 返回 `spring.ai` 自动配置的 ChatModel / EmbeddingModel Bean

### Requirement: 大模型导出

`GET /ai/model/export` MUST 支持 `format=yaml` 与 `format=env`。默认 MUST 将 SECRET 导出为 `${CODE}_API_KEY` 或 ENV_REF 占位符。`includeSecrets=true` MUST 需 `ai:model:export:secrets` 权限。

#### Scenario: 默认导出不含明文密钥
- **WHEN** 用户无 `export:secrets` 权限导出含 SECRET 的模型
- **THEN** 响应中无 SM4 密文且 api-key 为占位符形式

### Requirement: 大模型管理端页面

系统前端 MUST 提供 `views/ai/model/index.vue`（「AI 能力」菜单下，路由 `/ai/model`），使用 `C7JsonTable` 实现列表、新增/编辑、连接测试、设默认、导出。Flyway MUST 插入「AI 能力」一级菜单、「大模型管理」子菜单及 `ai:model:*` 按钮权限。

#### Scenario: 管理员可见大模型菜单
- **WHEN** 用户具备 `ai:model:list` 且 `qc.ai.enabled=true`
- **THEN** 侧边栏「AI 能力」下 MUST 展示「大模型管理」菜单项

### Requirement: 功能开关 qc.ai.enabled

当 `qc.ai.enabled=false` 时，系统 MUST NOT 注册 `AiModelRegistry` 与 `/ai/model/**` 端点（或返回功能未启用错误）。

#### Scenario: 关闭 AI Registry 开关
- **WHEN** `qc.ai.enabled=false` 且应用启动成功
- **THEN** `/ai/model/**` MUST 不可用或返回功能未启用错误
