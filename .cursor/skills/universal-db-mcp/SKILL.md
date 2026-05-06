---
name: universal-db-mcp
description: >-
  通过已配置的 Universal DB MCP（Anarkh-Lee/universal-db-mcp）访问多数据源：查 Schema、跑只读 SQL、辅助导出表 DDL。
  Use when the user mentions universal-db-mcp、通用数据库 MCP、多数据源 MCP，或要求在 Cursor 内连 MySQL/PostgreSQL/国产库（达梦、金仓、GaussDB、OceanBase、TiDB 等）做结构查询与 DDL 相关任务。
disable-model-invocation: true
---

# Universal DB MCP（Cursor 使用）

## 前提

- 本机已安装 **Node.js 18+**，`npx` 可用。
- Cursor 已加载 MCP Server：`universal-db-mcp`（项目 `.cursor/mcp.json` 或用户 `%USERPROFILE%\.cursor\mcp.json`）。
- 上游仓库与文档：<https://github.com/Anarkh-Lee/universal-db-mcp>；Cursor 集成说明：<https://github.com/Anarkh-Lee/universal-db-mcp/blob/main/docs/integrations/CURSOR.zh-CN.md>。
- **禁止**将含真实密码的 `mcp.json` 提交进 Git；生产连接优先 **只读账号**，并保持默认不开写权限。

## 何时优先走 MCP（而非凭空猜表结构）

- 用户给出或可推断连接目标，需要 **真实 Schema / 样例数据 / 元数据查询**。
- 任务涉及 **多库、国产协议兼容库**（`--type` 见下文），且希望与上游工具链一致。

## 调用前必读（工具契约）

在 MCP 工具面板或会话中，**先查阅当前可用的工具 schema**（名称与参数以 Cursor 实际加载为准）。Universal DB MCP 常见能力包括：

| 能力方向 | 典型工具（名称以 IDE 展示为准） |
|---------|----------------------------------|
| 结构总览 | `get_schema` |
| 单表详情 | `get_table_info` |
| 只读 SQL | `execute_query` |
| 缓存 | `clear_cache` |
| 连接切换 | `connect_database` / `disconnect_database` / `get_connection_status` |

**必须先读 schema 再传参**，不要假定参数名与上游 README 完全一致（版本迭代可能造成差异）。

## `--type` 速查（与上游 README 对齐）

`mysql`、`postgres`、`sqlite`、`sqlserver`、`oracle`、`mongodb`、`redis`、`dm`、`kingbase`、`gaussdb`、`oceanbase`、`tidb`、`clickhouse`、`polardb`、`vastbase`、`highgo`、`goldendb`。SQLite 用 `--file` 指向库文件路径。

## 安全与权限

- 默认应为 **只读**；不要随意添加 `--allow-write` / `--permission-mode full`。若用户明确要求 DDL/DML，先确认环境与账号。
- 结果集中可能含敏感字段；遵守上游 **脱敏** 行为说明，避免把大批量生产数据贴进对话。

## 「整库表 DDL」类任务怎么做

该 MCP **侧重 Schema 与查询**，不一定提供单一的「导出全部 CREATE TABLE」按钮式工具。推荐顺序：

1. `get_schema`（必要时 `get_table_info`）确认库表清单与类型。
2. 按方言用 **`execute_query` 只读** 拉取 DDL：
   - **MySQL / TiDB / OceanBase（MySQL 模式）等**：对各用户表执行 `` SHOW CREATE TABLE `name` ``（或由 Agent 循环调用 MCP）。
   - **PostgreSQL / Kingbase / Gauss / Vastbase / HighGo（PG 协议）**：一般场景可用 `execute_query` + 目录视图拼装；若不要求走仓库 Skill、且用户允许在本机执行命令，`pg_dump -s` 常更完整。**凡属于本仓库 `doc/ddl` 落盘、且用户引用 `@table-split` 时，一律仅 MCP，不适用 pg_dump**（见 `.cursor/skills/table-split/SKILL.md`）。
3. 输出过长时：写入工作区 `.sql` 文件再摘要，避免单次对话塞满。

## 多数据源

- **静态**：在 `mcpServers` 中为每个连接起一个独立名称（例如 `mysql-prod`、`postgres-analytics`），各启一个 `universal-db-mcp` 进程。
- **动态**：若当前 MCP 版本暴露 `connect_database`，按工具 schema 切换目标（仍以只读默认为准）。

## 与 table-split 的关系

- **按表拆分 DDL、写入 `doc/ddl/`、表注释映射**：使用 **`@table-split`**（`.cursor/skills/table-split/SKILL.md`）。该流程含执行前校验、`mcp.json` 检测及 **仅 MCP** 约束。
- 本文与 table-split 冲突时：**table-split 任务以 table-split 为准**。

## 故障排查（极简）

- 工具未出现：改配置后 **重启 Cursor**；确认 JSON 无语法错误；`npx` 在 PATH 中。
- 连接失败：核对主机、端口、防火墙、`127.0.0.1` vs `localhost`（Docker 场景）。
- 达梦等：**按需安装上游文档指定的驱动**（如 `dmdb`）。
