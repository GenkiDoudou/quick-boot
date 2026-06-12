## ADDED Requirements

### Requirement: 知识库 MCP 绑定（kb_knowledge_base_mcp）

系统 MUST 通过关联表 `kb_knowledge_base_mcp` 支持知识库与 MCP 的多对多绑定（`kb_id`、`mcp_id`、`order_num`），唯一约束 `(kb_id, mcp_id)`。仅 `status=0` 的 MCP MAY 被绑定。

#### Scenario: 创建知识库时绑定 MCP
- **WHEN** 用户提交 `mcpIds=[1,2]` 创建知识库
- **THEN** `kb_knowledge_base_mcp` 中存在两条对应记录

#### Scenario: 停用 MCP 不参与 RAG
- **WHEN** 已绑定 MCP 被设为 `status=1`
- **THEN** RAG 问答 MUST NOT 加载该 MCP 的工具

## MODIFIED Requirements

### Requirement: 知识库 CRUD API

系统 SHALL 提供以下接口（前缀 `/knowledge/base`），并使用 Sa-Token 权限校验：

| 路径 | 方法 | 权限 |
|------|------|------|
| `/list` | GET | `knowledge:base:list` |
| `/getInfo` | GET | `knowledge:base:query` |
| `/add` | POST | `knowledge:base:add` |
| `/update` | POST | `knowledge:base:edit` |
| `/remove` | POST | `knowledge:base:remove` |

`/add`、`/update`、`/getInfo` MUST 支持可选字段 `mcpIds`（数组）。删除知识库 MUST 级联删除其文档的 PGVector 向量、关联 MySQL 记录（逻辑删）及 `kb_knowledge_base_mcp` 绑定。

#### Scenario: 删除知识库清除向量
- **WHEN** 管理员删除某知识库
- **THEN** 该库下所有文档的向量不可再被检索到

#### Scenario: 详情返回绑定的 MCP
- **WHEN** 用户查询已绑定 MCP 的知识库详情
- **THEN** 响应 MUST 包含 `mcpIds` 数组

### Requirement: RAG 问答 API

系统 SHALL 提供 `POST /knowledge/chat`，接收 `kbId`、`question` 及可选 `useMcpTools`（默认 `true`），权限 `knowledge:chat`。系统 MUST 使用检索增强生成（RAG）返回答案字符串、`citations` 数组（含文档名、片段摘要、相似度）及可选 `mcpToolsUsed`（实际调用的 MCP 工具名列表）。当 `useMcpTools=true` 且知识库绑定了启用的 MCP 且 `qc.knowledge.mcp.enabled=true` 时，系统 MUST 将绑定 MCP 的工具注册为 Spring AI `ToolCallback` 并与向量检索顾问并存。P0 MUST NOT 要求 SSE 流式。

#### Scenario: 问答附带引用
- **WHEN** 知识库存在相关内容且用户提问
- **THEN** 响应中 `citations` 至少包含一条引用记录

#### Scenario: 无相关内容时明确告知
- **WHEN** 检索结果为空或低于阈值
- **THEN** 答案 MUST 明确说明无法从知识库找到依据，且 MUST NOT 捏造引用

#### Scenario: 绑定 MCP 且启用工具时可能调用工具
- **WHEN** 知识库绑定了可达的 MCP 且 `useMcpTools=true`
- **THEN** 响应 MAY 包含非空 `mcpToolsUsed` 且答案可结合工具结果

#### Scenario: 关闭 MCP 工具时保持纯 RAG
- **WHEN** 用户提交 `useMcpTools=false`
- **THEN** 系统 MUST NOT 调用 MCP 工具且行为与未接入 MCP 前一致

### Requirement: 管理端四页

系统前端 MUST 提供以下页面，并遵循 `DESIGN.md` 与列表页模板（默认参照 `views/system/config/index.vue`）：

- `views/knowledge/base/index.vue` — 知识库 CRUD（含默认分段/预处理配置及 MCP 多选绑定）
- `views/knowledge/library/index.vue` — 知识文档库（目录 + 文件）
- `views/knowledge/document/index.vue` — 文档列表、添加文档向导、状态展示、重索引、删除、对话测试（含「启用 MCP 工具」开关）
- `views/knowledge/search/index.vue` — 选库 + 检索 + 片段结果
- `views/knowledge/chat/index.vue` — 选库 + 问答 + 引用展示
- `views/knowledge/mcp/index.vue` — MCP 配置管理

文档状态为 `PENDING` 或 `PARSING` 时，前端 SHOULD 轮询任务进度或刷新列表直至终态。

#### Scenario: 文档入库进度可见
- **WHEN** 用户通过任一来添加入口创建文档并返回 taskId
- **THEN** 文档列表或任务查询可展示从 PENDING/PARSING 到 INDEXED/FAILED 的状态变化
