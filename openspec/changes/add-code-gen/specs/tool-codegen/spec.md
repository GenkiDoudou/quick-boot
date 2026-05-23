## ADDED Requirements

### Requirement: 代码生成元数据表

系统 MUST 提供 `gen_table` 与 `gen_table_column` 表，字段语义对齐若依参考（含表名、模板类型、包路径、模块名、业务名、树表配置列、列级 Java/HTML/查询配置等），主键类型与现有系统表 BIGINT 策略一致；`gen_table.table_name` MUST 唯一。

#### Scenario: 迁移后可写入元数据

- **WHEN** Flyway 迁移已执行
- **THEN** 应用可向 `gen_table` 与 `gen_table_column` 插入一行且可通过 `table_id` 关联

---

### Requirement: 库表内省基于 MyBatis-Plus Generator

系统 MUST 使用 **`com.baomidou:mybatis-plus-generator` 3.5.15**（与项目 `mybatis-plus.version` 一致）从当前应用 `DataSource` 读取表与列信息（`ConfigBuilder` / `TableInfo` / `TableField`），用于库表列表、导入与同步；系统 MUST NOT 依赖已废弃的 `原始需求/old` 中 `CodeGenerator` 类完成内省。

#### Scenario: 库表列表排除已导入表

- **WHEN** 管理员调用 `GET /tool/gen/db/list`
- **THEN** 返回结果 MUST 不包含 `gen_table` 中已存在的 `table_name`

#### Scenario: 导入使用 MP 表结构

- **WHEN** 管理员对未导入的物理表执行 `POST /tool/gen/importTable`
- **THEN** 系统 MUST 通过 MP Generator 读取该表列信息并写入 `gen_table_column`，且 `java_type`/`column_type` 与库表一致

---

### Requirement: 导入、建表与同步

系统 MUST 支持：从库导入多表（未选表名时 MUST 返回业务错误并阻断）；执行用户提交的建表 SQL（**仅** `CREATE TABLE`，经 Druid 解析，拒绝其他语句类型）；按物理表名同步列配置（同步时 MUST 以库表为准替换该表全部 `gen_table_column` 行）。建表成功后 MUST NOT 自动写入 `gen_table`（须用户再导入）。

#### Scenario: 导入未选表

- **WHEN** `importTable` 请求未包含任何表名
- **THEN** 系统 MUST 返回业务失败且前端可展示提示

#### Scenario: 同步覆盖列

- **WHEN** 管理员对某已导入表执行 `GET /tool/gen/synchDb/{tableName}`
- **THEN** 该表 `gen_table_column` 行集 MUST 与当前库表结构一致（通过 MP Generator 重新读取）

---

### Requirement: 配置编辑与删除元数据

系统 MUST 提供 `GET /tool/gen/{tableId}` 返回表头与列列表；`POST /tool/gen/update` 保存基本信息、字段配置与生成信息（Jakarta Validation）；`POST /tool/gen/remove/{tableId}` 仅删除元数据，不删除物理表。更新与删除 MUST NOT 使用对外 `PUT`/`DELETE` 动词。

#### Scenario: 保存使用 POST

- **WHEN** 客户端保存生成配置
- **THEN** HTTP 方法 MUST 为 `POST` 且路径为 `/tool/gen/update`

---

### Requirement: 预览与 Zip 代码生成

系统 MUST 通过 **FreeMarker** 模板（非 MP `AutoGenerator` 默认模板）根据 `gen_table` 元数据渲染代码；`GET /tool/gen/preview/{tableId}` 返回模板文件名与内容列表；`GET /tool/gen/batchGenCode` 返回 `application/zip`（默认文件名 `ruoyi.zip`），Zip MUST 在内存构建且不得写入服务端业务目录。当 `tpl_category=tree` 且请求生成代码时，系统 MUST 返回明确业务错误（树表模板未开放）。

#### Scenario: Zip 可解压且含后端与前端

- **WHEN** 管理员对有效 CRUD 配置表调用 `batchGenCode`
- **THEN** 响应 MUST 为 Zip 且解压后包含至少一个 Java Controller 类与一个前端 `api` 或 `vue` 文件

#### Scenario: 预览可复制

- **WHEN** 管理员打开预览
- **THEN** 每个模板项 MUST 含非空 `content` 供前端展示与复制

---

### Requirement: 管理端 REST 与权限

系统 MUST 暴露 `/tool/gen/*` 端点，权限使用 `tool:gen:list|import|create|edit|remove|preview|code`（与需求文档一致）。除 Zip 下载外，响应 MUST 使用统一 `R<T>` 包装。Controller MUST 具备 OpenAPI `@Tag`/`@Operation` 与 `@SaCheckPermission`。业务失败 MUST 使用项目 `WarningException`，禁止 `IllegalArgumentException` 作为业务信号。

#### Scenario: 列表权限

- **WHEN** 无 `tool:gen:list` 的用户访问 `GET /tool/gen/list`
- **THEN** 系统 MUST 拒绝访问

---

### Requirement: 管理端前端

`quick-ui` MUST 提供代码生成列表页（查询：表名、表描述、创建时间区间；操作：生成、创建表、导入、编辑、删除、预览、同步）、三 Tab 编辑页、导入/建表/预览弹窗；API 模块 MUST 统一调用 `/tool/gen` 前缀。页面 MUST 遵循 `DESIGN.md` 与 C7 组件模式（参考操作日志/通知公告列表）。

#### Scenario: 导入未选前端阻断

- **WHEN** 用户在导入弹窗未勾选表即确认
- **THEN** 前端 MUST 提示且不得发起 `importTable` 请求

---

### Requirement: 菜单与内置参数

Flyway MUST 插入「系统工具」目录及「代码生成」菜单与按钮权限，并为超级管理员授权。系统 SHOULD 支持 `quickboot.gen` 配置项（作者、默认包名、模块名、zip 文件名、建表语句上限）。

#### Scenario: 管理员可见菜单

- **WHEN** 超级管理员登录且迁移已执行
- **THEN** 侧栏 MUST 显示代码生成入口且具备 `tool:gen:list` 权限

---

### Requirement: 管理端不产生操作日志噪声

`GenController` 映射方法 MUST 使用 `@IgnoreLogger(ALL)` 或等价机制，避免列表/预览/下载产生大量操作日志。

#### Scenario: 列表查询不写操作日志

- **WHEN** 管理员仅调用 `GET /tool/gen/list`
- **THEN** 不得仅因该调用新增 `sys_oper_log` 记录
