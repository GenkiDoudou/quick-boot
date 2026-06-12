## ADDED Requirements

### Requirement: MCP 服务元数据（kb_mcp_server）

系统 MUST 在 MySQL 持久化外部 MCP 配置（表 `kb_mcp_server`），至少包含：`mcp_id`、`name`、`code`（唯一，导出 `mcpServers` 的 key）、`description`、`transport`（`STDIO`/`SSE`/`STREAMABLE_HTTP`）、`command`、`args_json`、`url`、`headers_json`、`request_timeout_ms`（默认 30000）、`status`（0 正常 / 1 停用）、`last_test_status`（`SUCCESS`/`FAILED`/`UNTESTED`）、`last_test_msg`、`last_test_time`、标准审计字段与 `deleted`。

#### Scenario: 新增 STDIO MCP 成功
- **WHEN** 具备 `knowledge:mcp:add` 的用户提交唯一 `code`、合法 `command` 与 `args_json`
- **THEN** 数据库新增 `deleted=0` 记录且返回 `mcpId`

#### Scenario: code 唯一约束
- **WHEN** 用户提交已存在的 `code`
- **THEN** 请求 MUST 失败并返回可识别业务错误

### Requirement: MCP 环境变量（kb_mcp_env）

系统 MUST 为每个 MCP 持久化环境变量行（表 `kb_mcp_env`），包含 `env_key`、`value_type`（`PLAIN`/`SECRET`/`ENV_REF`）、`env_value`、`sort_order`。`SECRET` MUST 以 SM4 密文存储；`ENV_REF` MUST 仅存环境变量名。

#### Scenario: SECRET 入库加密
- **WHEN** 用户新增 `value_type=SECRET` 的环境变量
- **THEN** 库中 `env_value` MUST 以 `{sm4:...}` 形式存储且列表接口返回脱敏值

#### Scenario: ENV_REF 运行时解析
- **WHEN** 连接测试或 RAG 调用时存在 `ENV_REF` 且进程环境变量已设置
- **THEN** 系统 MUST 使用 `System.getenv` 解析后的值发起 MCP 连接

### Requirement: MCP 管理 API

系统 SHALL 提供以下接口（前缀 `/knowledge/mcp`），修改/删除使用 `@PostMapping`：

| 路径 | 方法 | 权限 |
|------|------|------|
| `/list` | GET | `knowledge:mcp:list` |
| `/getInfo` | GET | `knowledge:mcp:query` |
| `/add` | POST | `knowledge:mcp:add` |
| `/update` | POST | `knowledge:mcp:edit` |
| `/remove` | POST | `knowledge:mcp:remove` |
| `/test` | POST | `knowledge:mcp:test` |
| `/export` | GET | `knowledge:mcp:export` |
| `/options` | GET | `knowledge:mcp:list` |

`/getInfo` MUST 支持 `revealSecrets` 查询参数（默认 false）。`/update` 时 `SECRET` 字段空串 MUST 表示不修改原值。`/remove` MUST 逻辑删除并驱逐客户端缓存。

#### Scenario: 分页列表按传输方式筛选
- **WHEN** 用户请求 `/list?transport=STDIO`
- **THEN** 仅返回 `transport=STDIO` 的未删除记录

### Requirement: MCP 连接测试

`POST /knowledge/mcp/test` MUST 对指定 `mcpId` 执行 MCP `initialize` 与 `tools/list`，更新 `last_test_*` 字段，并返回 `success`、`toolCount`、`tools`（含 name、description）、`message`。失败 MUST NOT 修改 `status`，仅写 `last_test_status=FAILED`。

#### Scenario: 测试成功记录 tool 数量
- **WHEN** MCP Server 可达且返回 3 个 tool
- **THEN** `last_test_status=SUCCESS` 且响应 `toolCount=3`

#### Scenario: 远程内网 URL 测试失败
- **WHEN** 配置的 `url` 解析为 RFC1918 或 loopback 地址
- **THEN** 测试 MUST 失败且 `message` 含 SSRF 相关说明

### Requirement: MCP 导出

`GET /knowledge/mcp/export` MUST 返回 Cursor / Claude Desktop 兼容 JSON（顶层 `mcpServers` 对象）。默认 MUST 将 `SECRET` 导出为 `${env_key}` 占位符。`includeSecrets=true` MUST 需 `knowledge:mcp:export:secrets` 权限。

#### Scenario: 默认导出不含明文密钥
- **WHEN** 用户无 `export:secrets` 权限导出含 SECRET 的 STDIO MCP
- **THEN** 响应 `env` 中对应值为 `${ENV_KEY}` 形式且无 SM4 密文

### Requirement: 动态 MCP 客户端管理

当 `qc.knowledge.mcp.enabled=true` 时，系统 MUST 提供 `McpClientManager`：按 `mcp_id` 缓存 `McpSyncClient`；配置变更时驱逐缓存；STDIO 子进程数 MUST NOT 超过 `qc.knowledge.mcp.max-stdio-processes`；远程 URL MUST 经 SSRF 校验。

#### Scenario: 更新配置后缓存失效
- **WHEN** 管理员修改 MCP 的 `args_json` 并保存
- **THEN** 后续连接测试 MUST 使用新参数而非旧缓存

#### Scenario: STDIO 超并发拒绝
- **WHEN** 活跃 STDIO 客户端数已达上限且再次请求建连
- **THEN** 请求 MUST 失败并提示子进程限额

### Requirement: MCP 管理端页面

系统前端 MUST 提供 `views/knowledge/mcp/index.vue`（知识管理菜单下，路由 `/knowledge/mcp`），使用 `C7JsonTable` 实现列表、新增/编辑（基本/连接/环境变量 Tab）、连接测试、导出所选。Flyway MUST 插入菜单及 `knowledge:mcp:*` 按钮权限。

#### Scenario: 管理员可见 MCP 菜单
- **WHEN** 用户具备 `knowledge:mcp:list` 且 `qc.knowledge.enabled=true`
- **THEN** 侧边栏「知识管理」下 MUST 展示「MCP 管理」菜单项

### Requirement: 功能开关 qc.knowledge.mcp.enabled

当 `qc.knowledge.mcp.enabled=false` 时，系统 MUST NOT 注册 `McpClientManager` 与 `/knowledge/mcp/**` 端点（或返回功能未启用错误）。

#### Scenario: 关闭 MCP 开关后无 MCP 端点
- **WHEN** `qc.knowledge.mcp.enabled=false` 且应用启动成功
- **THEN** `/knowledge/mcp/**` MUST 不可用或返回功能未启用错误
