## Context

`quickboot2` 主工程尚无代码生成能力；需求见 `原始需求/系统管理/代码生成-需求文档.md`。工程内设计真源为 `docs/superpowers/specs/2026-05-16-codegen-design.md`（用户已批准）。父 POM 已统一 **`mybatis-plus.version=3.5.15`**，与本次要求的 **`mybatis-plus-generator:3.5.15`** 版本对齐。

**与 superpowers 设计文档的差异（本变更采纳用户指令）**：

| 项 | superpowers 设计 | 本 OpenSpec 设计 |
|----|------------------|------------------|
| 库表内省 | 迁移旧 `CodeGenerator` | **`mybatis-plus-generator` 3.5.15**（`ConfigBuilder` / `TableInfo` / `TableField`） |
| 代码产出 | 仅 FreeMarker 自定义模板 | **不变**：预览与 Zip 仍由 **FreeMarker**（`vm/quickboot/*.ftl`）渲染；MP Generator **不**用于直接 `AutoGenerator` 写盘，避免生成物风格偏离 `quickboot2` |

业务流程（导入默认规则、同步删列重建、建表 Druid、Zip 内存下载）沿用 superpowers 设计与旧栈意图。

## Goals / Non-Goals

**Goals:**

- `gen_table` / `gen_table_column` 元数据 + Flyway + `tool:gen:*` 菜单。
- `/tool/gen/*` 全量 API；POST 表达 update/remove。
- **MP Generator** 驱动：`db/list`、import、synchDb 的表/列元数据读取与 Java 类型映射。
- **FreeMarker** 驱动：preview、`batchGenCode` Zip；生成物对齐 `SysNotice`、`C7`、`DESIGN.md`。
- `quick-ui` 列表、三 Tab 编辑、导入/建表/预览。

**Non-Goals:**

- 旧 `com.su60.quickboot.data.generator.CodeGenerator` 迁入。
- MP `AutoGenerator` 默认模板直接落盘或打进 Zip（与项目分层冲突）。
- `tpl_category=tree` / 主子表模板、`genCode` 自定义路径写盘（配置可存，生成时拒绝或 501）。

## Decisions

| 决策 | 选项 | 理由 |
|------|------|------|
| 库表内省 | **MP Generator 3.5.15** | 用户指定；与现有 MP 版本一致；`TableInfo`/`TableField` 维护成本低 |
| 内省实现 | **`GenDbIntrospector`** 封装 `ConfigBuilder` + 当前 `DataSource` | 统一 MySQL 策略；过滤已导入表；不启动完整 `AutoGenerator` |
| 元数据默认值 | **移植**旧 `converTableInfo` / `converTableField` 规则到 `GenColumnDefaults` | 保留主键/remark/del_flag/字典注释等若依习惯；`is_*` 统一 **1=是、0=否** |
| 业务代码模板 | **FreeMarker** `classpath:vm/quickboot/` | 可控生成 `R`/`POST`/C7；MP 仅提供结构信息 |
| 依赖声明 | `quickboot-web/pom.xml` 增加 `mybatis-plus-generator` **3.5.15**；父 BOM 已有 `mybatis-plus.version` | 与 `mybatis-plus-spring-boot3-starter` 同版本；实现阶段评估是否 `optional`（仅 web 使用） |
| 建表 SQL | **Druid** 解析，仅 `CREATE TABLE` | MP Generator 不负责 DDL 执行 |
| Zip | 内存 `ZipOutputStream`，默认 `ruoyi.zip` | 安全、不落盘 |
| 模块包 | `io.github.genkidoudou.web.tool.gen` | 与 superpowers 一致 |
| 自记操作日志 | `GenController` **`@IgnoreLogger(ALL)`** | 与 operlog 一致 |

### MP Generator 使用要点（实现参考）

```text
DataSource → DataSourceConfig + StrategyConfig（include 单表/多表）
          → ConfigBuilder.build().getTableInfoList()
          → 映射为 GenTable / GenTableColumn 实体
```

- `db/list`：无 `include` 或全库扫描后内存过滤表名/注释，排除 `gen_table` 已占用 `table_name`。
- `importTable` / `synchDb`：对目标表 `addInclude(tableName)` 后取 `TableInfo`。
- **不**调用 `FastAutoGenerator` / `AutoGenerator.execute()` 生成 Java 文件。

### API 路径（对外 = 需求路径；动词调整见 spec）

| 方法 | 路径 |
|------|------|
| GET | `/tool/gen/list`、`/tool/gen/db/list`、`/tool/gen/{tableId}`、`/tool/gen/preview/{tableId}`、`/tool/gen/synchDb/{tableName}`、`/tool/gen/batchGenCode` |
| POST | `/tool/gen/update`、`/tool/gen/importTable`、`/tool/gen/createTable`、`/tool/gen/remove/{tableId}` |

## Risks / Trade-offs

| 风险 | 缓解 |
|------|------|
| MP Generator 全表扫描性能 | `db/list` 分页在内存完成；大库可考虑 INFORMATION_SCHEMA 直查（二期） |
| MP 与 FreeMarker 字段名不一致 | 单一 `GenContext` 由元数据 + MP `TableField` 组装，模板只读 Context |
| `createTable` SQL 注入 | 白名单语句 + 条数上限 + 仅 DDL |
| Flyway `menu_id` 冲突 | 实现前取当前最大 `V*` 与菜单 id 顺延 |
| Generator 依赖传递 | 仅 `quickboot-web` 引入，避免 common 膨胀 |

## Migration Plan

1. 部署前 Flyway：建 `gen_table`、`gen_table_column`、菜单与 `sys_role_menu`。
2. 配置 `quickboot.gen.*`（及可选 `sys_config` 作者/包名）。
3. 回滚：新表可保留；禁用菜单即可停用功能（生产以运维策略为准）。

## Open Questions

- （无）`mybatis-plus-generator` 是否需显式引入 `freemarker` 模板引擎依赖——实现时以 MP 3.5.15 传递依赖为准，业务 FTL 使用项目已有 FreeMarker（若缺则单独声明）。
