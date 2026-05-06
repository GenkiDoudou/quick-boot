---
name: table-split
description: >-
  按场景维护 doc/ddl 下按表拆分的 DDL 与 table-comment-map.md：无参则从 universal-db-mcp 配置库全量导出；有表名则仅更新这些表；仅有引用文件则解析拆分落盘；引用文件+表名则从文件抽取对应表并以 MCP 拉取配置库中这些表的权威 DDL 覆盖写入。
  执行涉及数据库前校验 npx 与仓库根 `.cursor/mcp.json` 中的 universal-db-mcp；全程禁止 pg_dump/mysqldump 等 CLI。
  Use when the user mentions @table-split、table-split、按表拆分 DDL、doc/ddl、从文件拆分建表 SQL。
---

# table-split（按表 DDL 与映射维护）

## 依赖与通用约束

- **universal-db-mcp** 用法与 `--type` 见 `.cursor/skills/universal-db-mcp/SKILL.md`。
- 凡 **从配置数据库读取 DDL** 的步骤，**只允许** MCP 工具（如 `get_schema`、`get_table_info`、`execute_query`）；禁止 `pg_dump`、`mysqldump`、`sqlcmd` 等 CLI。
- 连接数据库时保持 **只读**；勿开启写权限。
- 调用 MCP 前 **先读当前会话中的工具 schema**。

## 调用分流（必须先判定）

根据用户本次 invocation **是否带有表名参数**、**是否引用/附加 SQL 文件**，进入唯一模式：

| 模式 | 条件 | 行为摘要 |
|------|------|----------|
| **A** | 仅 `@table-split` / 等同表述，**无**后续表名参数，**无**引用文件 | 对 `.cursor/mcp.json` 中 **已配置的** universal-db-mcp 目标库：**全库**用户表 DDL → `doc/ddl/<db_slug>/`，并重写 `table-comment-map.md`。 |
| **B** | **有**表名参数，**无**引用文件 | 仅对参数所列表：经 MCP 拉取 DDL，**新增或覆盖**对应 `.sql`，并 **更新** `table-comment-map.md` 中这些表的行（其余表行保留不动，除非用户要求全量重算映射）。 |
| **C** | **有**引用文件，**无**表名参数 | **不连库**解析该文件：按 `CREATE TABLE`（及方言等价语句）**拆分**，写入 `doc/ddl/<db_slug>/` 下约定路径；**尽力**从 DDL 内注释/`COMMENT ON` 等更新映射；`db_slug` 优先用户指定，否则从文件名推断，否则 `imported`。 |
| **D** | **有**引用文件 **且** **有**表名参数 | （1）从文件中 **仅解析** 参数所列表名的 DDL 片段（用于对齐片段边界、比对或补充文件内注释）；（2）**必须**再通过 MCP 从配置库拉取 **同名表** 的权威 DDL，**以 MCP 结果写入** `doc/ddl/` 对应 `.sql`（覆盖）；（3）这些表的 **`table-comment-map.md` 行以 MCP 元数据为准**；若文件片段内有表注释而库元数据无，可在该行脚注「文件：`xxx.sql`」可选补充（不臆造库注释）。文件中 **不存在** 的表名：告警并 **仍通过 MCP** 仅更新库侧 DDL。 |

**表名参数解析**：支持空格、英文逗号、中文逗号、顿号分隔；表名区分大小写策略与目标库一致；PostgreSQL 允许 `schema.table`，写入路径规则见下文。

---

## 执行前校验

### 模式 A、B、D（需要访问配置库）

未完成则 **停止**，说明修复方式：

1. 仓库根执行 `node -v`（建议 18+）；`npx --yes universal-db-mcp --help`（或 `--version`）退出码 0。
2. 读取 **仓库根** `.cursor/mcp.json`：`mcpServers` 中至少一项的 `args` 含 **`universal-db-mcp`**；否则请用户按上游文档配置并重启 Cursor。
3. 当前会话可调用 MCP 工具（`execute_query` / `get_schema` 等）。

### 模式 C（仅文件）

1. 确认引用文件在工作区内 **可读**（路径有效、编码优先 UTF-8）。
2. **不要求** 本会话已连接数据库；**不要求** 步骤 3 的 MCP 工具可用（可选仍提示保留 `mcp.json` 便于后续同步）。

---

## 产出目录（必须遵守）

```
doc/ddl/<db_slug>/
  table-comment-map.md
  tables/<table>.sql                    # MySQL 协议等（无 schema 层级时）
  schemas/<schema>/<table>.sql          # PostgreSQL 协议等多 schema
```

- `<db_slug>`：模式 A/B/D 默认取 MCP 连接中的 database；模式 C/D 用户未给时用文件名主干或 `imported`。不安全字符替换规则同前。
- 目录不存在则创建。

### 方言与单文件内容

- **MySQL 协议**：`SHOW CREATE TABLE` 全文写入 `tables/<table>.sql`。
- **PostgreSQL 协议**：仅 MCP 拼装或 `get_table_info` DDL；路径 `schemas/<schema>/<table>.sql`；文件首行注释含 `Generated via universal-db-mcp only`。
- **其它 type**：`execute_query` 可用的字典/`SHOW CREATE`/元数据接口尽力而为。

### 文件名净化

路径非法字符替换为 `_`；PG 用目录承载 schema。

---

## `table-comment-map.md` 格式（固定）

路径：`doc/ddl/<db_slug>/table-comment-map.md`。

```markdown
# 表名与注释映射

**数据库标识**：`<db_slug>`
**生成说明**：…

| 表名（含 schema 时写全称） | 表注释 |
|---------------------------|--------|
| `orders` | 订单主表 |
```

- 无注释用 `—`；排序：先 schema 后 table。
- **模式 B/D**：只更新参数表的行时，其它行保留。
- **模式 C**：以解析到的注释为准；解析不到则为 `—`。

---

## 模式 A / B / D：数据库侧执行顺序

0. 完成「执行前校验」。
1. `get_schema`（或等价）确认表清单；**B/D** 过滤为参数表（参数+D 时尚需在文件中存在的表额外标记）。
2. **A**：全库注释映射 SQL 一次 → 写满 `table-comment-map.md`。**B/D**：仅参数表的映射查询 → 合并写入映射文件。
3. **按表** MCP 拉 DDL → 写入 `.sql`（**D** 以 MCP 覆盖为准）。
4. 汇总输出：更新文件列表与模式代号。

## 模式 C：文件解析规则

1. 读取全文，按语句边界拆分（处理括号与字符串内的分号，避免误切）。
2. 识别每张表的 **首个** `CREATE TABLE`（或 `CREATE UNLOGGED TABLE` 等）直至匹配的语句结束；每张表一个输出文件。
3. 若同一文件内同名表重复定义：保留 **最后一次** 或告警让用户指定（默认最后一次）。
4. 写入 `doc/ddl/<db_slug>/` 对应路径；可选生成/合并 `table-comment-map.md`（从 `COMMENT ON TABLE`、MySQL `COMMENT=` 等抽取）。

## SQL 辅助（MySQL / PostgreSQL 映射与 DDL）

MySQL 映射（全库或 `WHERE TABLE_NAME IN (...)` 限定参数表）：

```sql
SELECT TABLE_NAME AS table_name, IFNULL(NULLIF(TABLE_COMMENT, ''), '—') AS table_comment
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_TYPE = 'BASE TABLE'
ORDER BY TABLE_NAME;
```

PostgreSQL 映射：

```sql
SELECT n.nspname || '.' || c.relname AS table_name,
       COALESCE(NULLIF(d.description, ''), '—') AS table_comment
FROM pg_class c
JOIN pg_namespace n ON n.oid = c.relnamespace
LEFT JOIN pg_description d ON d.objoid = c.oid AND d.objsubid = 0
WHERE c.relkind = 'r' AND n.nspname NOT IN ('pg_catalog', 'information_schema')
ORDER BY 1;
```

单表 MySQL DDL：`SHOW CREATE TABLE \`<table>\`;`

---

## 安全与仓库

- 不在对话或提交的 `mcp.json` 中写入真实密码。
- DDL 含结构信息；公开仓库可自行 `.gitignore` `doc/ddl/`。

## 与其它 Skill 的关系

- **universal-db-mcp**：连接、工具名、`--type` 列表以此为准。
- 旧 skill 名 **export-db-ddl-doc** 已并入 **table-split**；全库导出对应 **模式 A**。
