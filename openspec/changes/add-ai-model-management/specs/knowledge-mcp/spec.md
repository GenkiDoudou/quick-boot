## MODIFIED Requirements

### Requirement: MCP 管理 API

系统 SHALL 提供以下接口（前缀 **`/ai/mcp`**），修改/删除使用 `@PostMapping`：

| 路径 | 方法 | 权限 |
|------|------|------|
| `/list` | GET | `ai:mcp:list` |
| `/getInfo` | GET | `ai:mcp:query` |
| `/add` | POST | `ai:mcp:add` |
| `/update` | POST | `ai:mcp:edit` |
| `/remove` | POST | `ai:mcp:remove` |
| `/test` | POST | `ai:mcp:test` |
| `/export` | GET | `ai:mcp:export` |
| `/options` | GET | `ai:mcp:list` |

`/getInfo` MUST 支持 `revealSecrets` 查询参数（默认 false）。`/update` 时 `SECRET` 字段空串 MUST 表示不修改原值。`/remove` MUST 逻辑删除并驱逐客户端缓存。

#### Scenario: 分页列表按传输方式筛选
- **WHEN** 用户请求 `/list?transport=STDIO`
- **THEN** 仅返回 `transport=STDIO` 的未删除记录

### Requirement: MCP 连接测试

`POST /ai/mcp/test` MUST 对指定 `mcpId` 执行 MCP `initialize` 与 `tools/list`，更新 `last_test_*` 字段，并返回 `success`、`toolCount`、`tools`（含 name、description）、`message`。失败 MUST NOT 修改 `status`，仅写 `last_test_status=FAILED`。

#### Scenario: 测试成功记录 tool 数量
- **WHEN** MCP Server 可达且返回 3 个 tool
- **THEN** `last_test_status=SUCCESS` 且响应 `toolCount=3`

#### Scenario: 远程内网 URL 测试失败
- **WHEN** 配置的 `url` 解析为 RFC1918 或 loopback 地址
- **THEN** 测试 MUST 失败且 `message` 含 SSRF 相关说明

### Requirement: MCP 导出

`GET /ai/mcp/export` MUST 返回 Cursor / Claude Desktop 兼容 JSON（顶层 `mcpServers` 对象）。默认 MUST 将 `SECRET` 导出为 `${env_key}` 占位符。`includeSecrets=true` MUST 需 `ai:mcp:export:secrets` 权限。

#### Scenario: 默认导出不含明文密钥
- **WHEN** 用户无 `export:secrets` 权限导出含 SECRET 的 STDIO MCP
- **THEN** 响应 `env` 中对应值为 `${ENV_KEY}` 形式且无 SM4 密文

### Requirement: MCP 管理端页面

系统前端 MUST 提供 `views/ai/mcp/index.vue`（**「AI 能力」**菜单下，路由 **`/ai/mcp`**），使用 `C7JsonTable` 实现列表、新增/编辑（基本/连接/环境变量 Tab）、连接测试、导出所选。Flyway MUST 将 MCP 菜单迁入「AI 能力」并更新 `ai:mcp:*` 按钮权限。旧路由 `/knowledge/mcp` SHOULD redirect 至 `/ai/mcp`。

#### Scenario: 管理员可见 MCP 菜单
- **WHEN** 用户具备 `ai:mcp:list` 且 `qc.knowledge.enabled=true`
- **THEN** 侧边栏「AI 能力」下 MUST 展示「MCP 管理」菜单项

### Requirement: 功能开关 qc.knowledge.mcp.enabled

当 `qc.knowledge.mcp.enabled=false` 时，系统 MUST NOT 注册 `McpClientManager` 与 **`/ai/mcp/**`** 端点（或返回功能未启用错误）。

#### Scenario: 关闭 MCP 开关后无 MCP 端点
- **WHEN** `qc.knowledge.mcp.enabled=false` 且应用启动成功
- **THEN** `/ai/mcp/**` MUST 不可用或返回功能未启用错误

## MODIFIED Requirements

### Requirement: MCP 服务元数据（kb_mcp_server）

系统 MUST 在 MySQL 持久化外部 MCP 配置（表 `kb_mcp_server`），至少包含：`mcp_id`、`name`、`code`（唯一，导出 `mcpServers` 的 key）、`description`、`transport`（`STDIO`/`SSE`/`STREAMABLE_HTTP`）、`command`、`args_json`、`url`、`headers_json`、`request_timeout_ms`（默认 30000）、`status`（0 正常 / 1 停用）、`last_test_status`（`SUCCESS`/`FAILED`/`UNTESTED`）、`last_test_msg`、`last_test_time`、标准审计字段与 `deleted`。

#### Scenario: 新增 STDIO MCP 成功
- **WHEN** 具备 **`ai:mcp:add`** 的用户提交唯一 `code`、合法 `command` 与 `args_json`
- **THEN** 数据库新增 `deleted=0` 记录且返回 `mcpId`

#### Scenario: code 唯一约束
- **WHEN** 用户提交已存在的 `code`
- **THEN** 请求 MUST 失败并返回可识别业务错误
