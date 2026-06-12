# 外部 MCP 管理 — 设计说明

**日期**：2026-06-07  
**状态**：已定稿（方案 A；OpenSpec `add-knowledge-mcp-management`）  
**依据**：brainstorming 澄清结论 — 1E、2C、3C、4D、5D

---

## 1. 背景与目标

### 1.1 背景

QuickBoot 知识库已具备 RAG 问答（`RagService` + Spring AI `ChatClient`），但无法连接外部 **Model Context Protocol (MCP)** 工具。团队目前在 Cursor 侧通过 `.cursor/mcp.json` 手工维护 MCP（如 `universal-db-mcp`），缺少统一台账、权限管控与业务侧运行时接入。

### 1.2 目标（本期一次交付）

| 能力 | 说明 |
|------|------|
| MCP 配置 CRUD | 管理后台维护外部 MCP：名称、编码、传输方式、连接参数、环境变量、启用状态 |
| 双传输 | **STDIO**（command + args + env）与 **远程 HTTP**（SSE / Streamable-HTTP URL + 可选 Headers） |
| 连接测试 | 对启用配置执行 `initialize` + `tools/list`，回写最近探测结果 |
| 运行时接入 | 知识库 RAG 问答可调用已绑定且启用的 MCP 工具（与向量检索并存） |
| 导出 | 导出为 Cursor / Claude Desktop 兼容的 `mcp.json` 片段（密钥占位为环境变量引用） |
| 密钥安全 | SM4 加密敏感字段；支持 `ENV_REF` 仅存变量名；列表脱敏、详情按需展示 |

### 1.3 非目标（本期不做）

- MCP Server **托管/部署**（本系统仅作 Client）
- 工作流节点直接调 MCP（可 Phase 2 复用 `McpClientManager`）
- SSE 流式 RAG 输出
- 多租户 / 按部门隔离 MCP 配置（延续知识库 P0 全局可见）
- 自动双向同步 Cursor 本地 `mcp.json`（仅单向导出）

---

## 2. 已定稿产品决策（Q&A）

| 题号 | 选项 | 结论 |
|------|------|------|
| 1 | E | 配置管理 + 运行时调用 + 导出 `mcp.json` |
| 2 | C | STDIO + HTTP/SSE（含 Streamable-HTTP） |
| 3 | C | 菜单挂在「知识管理」下，与 RAG/Agent 强绑定 |
| 4 | D | 一次交付：CRUD + 测试 + 知识库业务接入 + 导出 |
| 5 | D | SM4 加密 + 环境变量引用 + 列表脱敏 |

---

## 3. 方案对比与选型

| 方案 | 概要 | 优点 | 缺点 |
|------|------|------|------|
| **A（推荐）** | DB 配置 + **MCP Java SDK** 程序化创建 `McpSyncClient`，注册为 Spring AI `ToolCallback` | 动态增删改无需重启；与 Spring AI 1.0 Tool 框架兼容 | 需自管 Client 生命周期与连接池 |
| B | DB 变更后写回 `application.yml` 并触发 Context Refresh | 直接用 `spring-ai-starter-mcp-client` 自动配置 | 热更新复杂、多实例不一致、生产风险高 |
| C | 独立 MCP 代理网关进程 | 安全隔离、stdio 与业务 JVM 解耦 | 架构重、本期范围过大 |

**采用方案 A**：依赖 `spring-ai-starter-mcp-client` 传递的 MCP Java SDK（`McpClient.sync` + `StdioClientTransport` / `HttpClient` 系 Transport），由 `McpClientManager` 按库表配置动态建连。

---

## 4. 领域模型与表结构

Flyway **`V62__knowledge_mcp.sql`**（编号以仓库当前最大版本为准）。

### 4.1 MCP 服务主表 — `kb_mcp_server`

| 字段 | 类型 | 说明 |
|------|------|------|
| `mcp_id` | BIGINT PK | 主键 |
| `name` | VARCHAR(100) | 展示名称 |
| `code` | VARCHAR(64) UNIQUE | 稳定标识，导出 `mcpServers` 的 key |
| `description` | VARCHAR(500) | 备注 |
| `transport` | VARCHAR(24) | `STDIO` / `SSE` / `STREAMABLE_HTTP` |
| `command` | VARCHAR(255) NULL | STDIO：可执行命令（Windows 填 `cmd.exe` 时 args 含 `/c`） |
| `args_json` | JSON NULL | STDIO 参数数组，如 `["-y","universal-db-mcp",...]` |
| `url` | VARCHAR(2048) NULL | 远程 MCP 根 URL |
| `headers_json` | JSON NULL | 远程可选 HTTP 头（值可含密钥，见 §4.2） |
| `request_timeout_ms` | INT | 默认 30000 |
| `status` | TINYINT | 0 正常 / 1 停用 |
| `last_test_status` | VARCHAR(16) NULL | `SUCCESS` / `FAILED` / `UNTESTED` |
| `last_test_msg` | VARCHAR(1000) NULL | 最近探测摘要（含 tool 数量） |
| `last_test_time` | DATETIME NULL | 最近探测时间 |
| 审计 + `deleted` | | 与项目规范一致 |

### 4.2 环境变量表 — `kb_mcp_env`

| 字段 | 类型 | 说明 |
|------|------|------|
| `env_id` | BIGINT PK | |
| `mcp_id` | BIGINT | 关联 `kb_mcp_server` |
| `env_key` | VARCHAR(128) | 变量名 |
| `value_type` | VARCHAR(16) | `PLAIN` 明文 / `SECRET` SM4 密文 / `ENV_REF` 仅引用名 |
| `env_value` | VARCHAR(2000) | 按 `value_type` 解释（`ENV_REF` 存 `DB_PASSWORD` 等） |
| `sort_order` | INT | 排序 |

`headers_json` 中敏感值入库前同样经 `McpSecretSupport` 处理：明文 → SM4；引用 → 存 `{envRef:"VAR"}` 结构。

### 4.3 知识库绑定 — `kb_knowledge_base_mcp`

| 字段 | 说明 |
|------|------|
| `id` | 主键 |
| `kb_id` | 知识库 |
| `mcp_id` | MCP 服务（须 `status=0`） |
| `order_num` | 同库多 MCP 时的工具合并顺序 |

唯一约束：`uk_kb_mcp (kb_id, mcp_id)`。

### 4.4 配置项 — `qc.knowledge.mcp`

```yaml
qc:
  knowledge:
    mcp:
      enabled: true
      max-stdio-processes: 10          # 全局 STDIO 子进程上限
      client-cache-ttl-seconds: 300    # 连接缓存 TTL
      test-timeout-ms: 20000
      export:
        include-secrets: false         # 导出默认不含明文，仅 ENV_REF
      stdio:
        # 可选命令白名单；空则仅校验非空（内网管理员场景）
        allowed-commands: npx,node,cmd.exe,docker
```

---

## 5. 运行时架构

```mermaid
flowchart TB
  subgraph AdminUI
    CRUD[MCP 管理页]
    Export[导出 mcp.json]
    Test[连接测试]
    KbBind[知识库表单绑定 MCP]
  end

  subgraph Backend
    API[/knowledge/mcp/**]
    Mgr[McpClientManager]
    SDK[McpSyncClient per server]
    Tools[SyncMcpToolCallbackProvider]
    RAG[RagService + ChatClient]
  end

  subgraph External
    STDIO[npx MCP Server]
    HTTP[Remote MCP SSE/HTTP]
  end

  CRUD --> API
  Export --> API
  Test --> API
  KbBind --> API
  API --> Mgr
  Mgr --> SDK
  SDK --> STDIO
  SDK --> HTTP
  RAG --> Tools
  Tools --> Mgr
```

### 5.1 `McpClientManager`

- **获取客户端**：按 `mcp_id` 从缓存取 `McpSyncClient`；miss 时读库组装 `ServerParameters` / HTTP Transport 并 `initialize()`。
- **失效**：配置 update/remove/status 变更时 `evict(mcp_id)`；可选定时 TTL 刷新。
- **STDIO 安全**：
  - 子进程数不超过 `max-stdio-processes`；
  - 可选 `allowed-commands` 校验 `command` 首段；
  - 请求超时 `request_timeout_ms`；
  - Windows：文档与表单提示 `cmd.exe` + `/c` + `npx` 模式（与 Spring AI 官方说明一致）。
- **远程 URL**：沿用工作流 `HttpRequest` 同类 SSRF 策略（禁内网、限重定向、超时）。

### 5.2 连接测试

`POST /knowledge/mcp/test`（`mcpId`）：

1. 临时建连（或复用缓存）
2. `client.listTools()`
3. 更新 `last_test_*` 字段
4. 返回 `{ success, toolCount, tools: [{name, description}], message }`

失败不修改 `status`，仅写 `last_test_status=FAILED`。

### 5.3 密钥处理 — `McpSecretSupport`

复用 `PasswordCodec` + SM4，模式对齐 `Oauth2SecretSupport`：

| value_type | 存储 | 运行时解析 |
|------------|------|------------|
| PLAIN | 明文 | 原值 |
| SECRET | `{sm4:...}` | `codec.decrypt` |
| ENV_REF | 变量名字符串 | `System.getenv(name)`，缺失则测试/调用失败并提示 |

列表 VO：`SECRET` 与 headers 敏感项显示 `******`；详情接口 `revealSecrets=false` 默认脱敏，`true` 需 `knowledge:mcp:query` 且记操作日志。

### 5.4 RAG 业务接入

扩展 `KnowledgeChatBo`：

```json
{
  "kbId": 1,
  "question": "...",
  "useMcpTools": true
}
```

逻辑（`useMcpTools` 默认 `true`，可关）：

1. 查 `kb_knowledge_base_mcp` + `kb_mcp_server.status=0`
2. 若无绑定或 `qc.knowledge.mcp.enabled=false` → 保持现有纯 RAG
3. 否则 `McpToolCallbackProvider` 聚合绑定 MCP 的 `ToolCallback`，注入 `ChatClient`：

```java
ChatClient.builder(chatModel)
    .defaultSystem(SYSTEM_PROMPT + MCP_TOOL_HINT)
    .defaultAdvisors(qaAdvisor)
    .defaultToolCallbacks(mcpToolCallbacks)
    .build();
```

4. 响应 `KnowledgeChatVo` 增加可选字段 `mcpToolsUsed: string[]`（实际调用的工具名）

**知识库表单**：多选 MCP（仅展示已启用项），保存时维护 `kb_knowledge_base_mcp`。

系统 Prompt 补充：优先使用知识库检索上下文；若 MCP 工具可提供实时数据且与问题相关，可调用工具，但不得捏造引用。

---

## 6. API 设计

前缀 `/knowledge/mcp`；修改/删除仍 `@PostMapping`。

| 路径 | 权限 | 说明 |
|------|------|------|
| `GET /list` | `knowledge:mcp:list` | 分页；条件：name、code、transport、status |
| `GET /getInfo` | `knowledge:mcp:query` | 详情；`revealSecrets` 查询参数 |
| `POST /add` | `knowledge:mcp:add` | 新增（含 env 列表） |
| `POST /update` | `knowledge:mcp:edit` | 修改；SECRET 字段空串表示不修改原值 |
| `POST /remove` | `knowledge:mcp:remove` | 逻辑删；evict 客户端 |
| `POST /test` | `knowledge:mcp:test` | 连接测试 |
| `GET /export` | `knowledge:mcp:export` | 导出 JSON；`ids` 可选逗号分隔，缺省导出全部启用项 |
| `GET /options` | `knowledge:mcp:list` | 下拉选项（知识库绑定用） |

**知识库** `/knowledge/base` 的 `add`/`update`/`getInfo` 增加 `mcpIds: number[]`。

### 6.1 导出格式

`GET /knowledge/mcp/export` 响应示例（Cursor 兼容）：

```json
{
  "mcpServers": {
    "universal-db-mcp": {
      "command": "npx",
      "args": ["--yes", "universal-db-mcp", "--type", "mysql", "..."],
      "env": {
        "MYSQL_PASSWORD": "${MYSQL_PASSWORD}"
      }
    },
    "remote-analytics": {
      "url": "https://mcp.example.com/sse"
    }
  }
}
```

- `SECRET` → 导出为 `${ENV_KEY}` 占位（键名同 `env_key`）
- `ENV_REF` → 原样 `${var}` 或 `var`（统一为 `${var}`）
- `includeSecrets=true` 需额外权限 `knowledge:mcp:export:secrets`（默认不授予 admin 以外角色）

---

## 7. 前端（quick-ui）

### 7.1 菜单

父菜单 `2280`（知识管理）：

| 菜单 | 路由 | 组件 | order |
|------|------|------|-------|
| MCP 管理 | `/knowledge/mcp` | `knowledge/mcp/index` | 6 |

权限按钮：`knowledge:mcp:list|query|add|edit|remove|test|export`（Flyway 插入菜单 2301+）。

### 7.2 MCP 管理页

- 列表：`C7JsonTable`（参照 `views/system/config/index.vue`）
- 列：名称、编码、传输方式、状态、最近测试、更新时间
- 操作：新增/编辑、测试连接、导出所选、删除
- 表单 Tab：
  - **基本**：名称、编码、描述、传输方式、超时、启用
  - **连接**：STDIO → command + 动态 args 行；远程 → URL + Headers 键值表
  - **环境变量**：key、类型（明文/密钥/环境引用）、value；密钥显示眼睛图标需权限
- 测试：弹窗展示 tool 列表与描述

### 7.3 知识库表单

- 字段「关联 MCP」：`el-select` multiple，数据源 `/knowledge/mcp/options`
- 帮助文案：仅启用状态的 MCP 可在 RAG 对话中作为工具使用

### 7.4 RAG 对话（知识库详情 · 对话测试 Tab）

- 开关「启用 MCP 工具」（默认开，写入 `useMcpTools`）
- 回答区展示 `mcpToolsUsed` 标签（若有）

---

## 8. 依赖与模块

| 项 | 说明 |
|----|------|
| Maven | `quickboot-knowledge/pom.xml` 增加 `spring-ai-starter-mcp-client` |
| 包路径 | `io.github.genkidoudou.web.knowledge.mcp.*` |
| 开关 | `qc.knowledge.mcp.enabled=false` 时不注册 `McpClientManager` 与 MCP 相关 Controller；RAG 退化为纯向量 |

---

## 9. 权限与安全

| 项 | 策略 |
|----|------|
| 菜单权限 | `knowledge:mcp:*` |
| STDIO | 子进程限额 + 可选命令白名单；禁止配置读取业务库 SM4 主密钥 |
| 远程 MCP | SSRF 防护（对齐 `WebContentFetcher` / 工作流 HTTP 节点） |
| 审计 | 导出含密钥、详情 `revealSecrets` 记 operlog |
| 鉴权 | Sa-Token；MCP 配置全局可见（与知识库一致） |

---

## 10. 迁移与兼容

1. **V62** 建表 + 菜单权限
2. `application.yml` 增加 `qc.knowledge.mcp` 块
3. 现有 RAG 行为不变：未绑定 MCP 或 `useMcpTools=false` 时与现网一致
4. 无历史数据回填

---

## 11. 测试要点

| ID | 场景 |
|----|------|
| TC_MCP_001 | 登录后可见 MCP 管理菜单 |
| TC_MCP_010 | 新增 STDIO MCP（npx）→ 测试成功 → `last_test_status=SUCCESS` |
| TC_MCP_011 | 新增远程 SSE MCP → 测试列出 tools |
| TC_MCP_012 | SECRET 入库加密；列表脱敏；详情默认不明文 |
| TC_MCP_013 | ENV_REF 运行时从环境变量解析 |
| TC_MCP_020 | 导出 JSON 结构可被 Cursor 粘贴使用（密钥为占位符） |
| TC_MCP_030 | 知识库绑定 MCP → RAG 问答 `mcpToolsUsed` 非空（mock 工具可调用） |
| TC_MCP_031 | 停用 MCP 后 RAG 不再加载其工具 |
| TC_MCP_040 | 内网 URL 远程 MCP 测试失败且 error 明确 |
| TC_MCP_041 | STDIO 超并发拒绝并提示 |

---

## 12. 实施任务概览（供 writing-plans）

1. Flyway V62 + 实体/DTO/枚举 + `McpSecretSupport`
2. `McpClientManager` + Transport 工厂 + 单测（mock process / mock HTTP）
3. MCP CRUD API + 连接测试 + 导出
4. 知识库 `mcpIds` 绑定 + `RagService` 接入 ToolCallbacks
5. 前端 MCP 管理页 + 知识库表单 + 对话测试开关
6. 菜单权限 + `application.yml` + 联调

---

## 13. 风险与缓解

| 风险 | 缓解 |
|------|------|
| STDIO 子进程泄漏 | `destroyMethod` / try-finally `close`；缓存 TTL；停机钩子 |
| LLM 滥用 MCP 工具 | 系统 Prompt 约束；仅绑定库可用；超时 |
| Spring AI 1.0.0 MCP API 与文档差异 | 以 SDK 程序化建连为准；集成测试覆盖 |
| Windows/Linux stdio 差异 | 表单说明 + 示例模板（cmd.exe 包装） |
| 一次交付面大 | 按 §12 顺序；先后端 CRUD+测试，再 RAG 接入，最后导出与 UI 抛光 |

---

**请评审本文档**。确认无重大异议后，将基于 §12 编写实现计划（`writing-plans` / OpenSpec tasks）。
