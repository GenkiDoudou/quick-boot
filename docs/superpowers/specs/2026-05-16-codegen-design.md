# 代码生成设计文档

## 1. 背景与目标

在 `quickboot` + `quick-ui` 中实现《代码生成需求文档》所述能力：基于数据库表管理生成元数据，支持导入、创建表、编辑配置、预览、同步库表结构、Zip 下载生成代码。

实现策略：**库表内省**使用 **MyBatis-Plus Generator 3.5.15**（`ConfigBuilder` / `TableInfo`）；**代码产出**由 **FreeMarker** 自定义模板渲染并 Zip 下载；对外 API 与生成物对齐当前仓库分层（`domain` / `dto` / `vo`、`R<T>`、POST 删改、Sa-Token、`C7` + `DESIGN.md`）。OpenSpec 真源：`openspec/changes/add-code-gen/`。

路径与权限以需求文档为真源：`/tool/gen/*`、`tool:gen:list|import|create|edit|remove|preview|code`。

## 2. 已确认决策摘要

| 项 | 决策 |
|----|------|
| API 前缀与权限 | `/tool/gen`，`tool:gen:*`（见 §6.2） |
| 实现策略 | 迁移旧 generator 核心 + 适配 quickboot2；**不**整体引入 RuoYi Velocity 模板 |
| 生成物风格 | 对齐 `SysNotice` / `SysOperLog` 等现有模块 + `quick-ui` C7 组件 |
| HTTP 动词 | **对外路径**与需求一致；**修改/删除**用 **POST**（需求 `PUT`/`DELETE` 不对外暴露，以本设计为准） |
| 生成方式 | 首期仅 **Zip 下载**（默认文件名 `ruoyi.zip`）；自定义路径写盘二期 |
| 表结构与能力 | 若依语义 `gen_table` / `gen_table_column`；**单表 CRUD 模板**；树表/主子表字段可配置，**模板二期** |
| 建表 SQL | 首期支持；仅允许 `CREATE TABLE`；Druid 解析 + `tool:gen:create` |
| 模板引擎 | **FreeMarker**（`classpath:vm/quickboot/**/*.ftl`） |
| 模块落点 | 首期集中在 `quickboot-web` 包 `io.github.genkidoudou.web.tool.gen` |

## 3. 方案选型

| 方案 | 说明 | 结论 |
|------|------|------|
| **A（采用）** | 迁移旧 `CodeGenerator` + 元数据 CRUD + 重写 FreeMarker 模板 | 与决策一致；导入/同步/预览逻辑已验证 |
| B | 迁移 Service，元数据改用 MP Generator / JDBC 内省 | 默认值规则需重造，风险高 |
| C | 引入 RuoYi Velocity 模板 + 适配层 | 与当前仓库风格冲突大 |

## 4. 范围与非范围

### 4.1 范围

- Flyway：`gen_table`、`gen_table_column`、菜单与按钮权限种子。
- 后端：`GenController` 及 Service / Mapper / Domain / Bo / Vo；`DbIntrospector`、`GenTemplateRenderer`、`GenZipExporter`。
- FreeMarker 模板：单表 CRUD 后端全套 + `api.js` + C7 列表页 + **三 Tab 编辑配置页**（基本信息 / 字段信息 / 生成信息）。
- `quick-ui`：`api/tool/gen.js`、`views/tool/gen/index.vue`、`views/tool/gen/edit.vue`（及导入/创建表/预览弹窗）。
- 配置：`application.yml` 中 `quickboot.gen.*`；可选与 `sys_config` 键（如作者、默认包名）联动。

### 4.2 非范围（本期不做）

- `GET /tool/gen/genCode/{tableName}` 自定义路径写盘（可返回明确「未开放」或 501）。
- `tpl_category=tree` / 主子表 **代码模板**（配置可保存；生成时提示「树表模板尚未开放」）。
- 生成结果自动 `INSERT sys_menu`（可提供 `menu.sql.ftl` 片段供手工执行）。
- 将 generator 下沉到 `quickboot-common`（除非后续多模块复用再抽）。

## 5. 架构与模块边界

```
quick-ui (views/tool/gen, api/tool/gen.js)
        │  R<T> / PageInfo；Zip 为 blob 下载
        ▼
quickboot-web … tool.gen
  ├── controller   GenController  @RequestMapping("/tool/gen")
  ├── service      GenTableService / GenTableColumnService
  ├── mapper       GenTableMapper, GenTableColumnMapper
  ├── domain       GenTable, GenTableColumn
  └── support
        ├── DbIntrospector       ← 自 old CodeGenerator 精简迁入
        ├── GenTemplateRenderer  ← FreeMarker
        └── GenZipExporter       ← 内存 ZipOutputStream，不落盘
```

**依赖**：Druid SQL 解析（建表）、FreeMarker、JDK Zip。

**配置项（`quickboot.gen`）**：

| 配置键 | 说明 | 默认建议 |
|--------|------|----------|
| `author` | 生成注释作者 | 可覆盖为 `sys_config` 中 `gen.author` |
| `package-name` | Java 根包 | `io.github.genkidoudou.web` |
| `module-name` | 默认模块名 | `system` |
| `zip-file-name` | 下载文件名 | `ruoyi.zip` |
| `create-table-max-statements` | 单次建表语句上限 | `10` |

## 6. 数据模型

### 6.1 `gen_table`

主键策略与 `V1__baseline` 等现有表一致（实现阶段对照 Flyway 定稿，建议 `BIGINT` + 雪花）。

| 列 / 含义 | 说明 |
|-----------|------|
| `table_id` | 主键 |
| `table_name` | 表名称；**唯一** |
| `table_comment` | 表描述 |
| `class_name` | 实体类名 |
| `tpl_category` | 模板类型，默认 `crud`；`tree` 仅保存配置 |
| `tpl_web_type` | 前端模板类型，固定 `element-plus` |
| `package_name` | 生成包路径 |
| `module_name` | 模块名 |
| `business_name` | 业务名（路由/权限片段） |
| `function_name` | 功能名 |
| `function_author` | 作者 |
| `gen_type` | `0` Zip / `1` 自定义路径（首期仅 `0` 生效） |
| `gen_path` | 自定义路径（二期） |
| `parent_menu_id` | 上级菜单 ID |
| `tree_code` / `tree_parent_code` / `tree_name` | 树表字段（配置用，模板二期） |
| `sub_table_name` / `sub_table_fk_name` | 主子表（二期） |
| `options` | JSON 扩展，可空 |
| `remark` | 备注 |
| `create_by` / `create_time` / `update_by` / `update_time` | 审计字段 |

### 6.2 `gen_table_column`

对齐若依 + 旧栈：`column_id`、`table_id`、`column_name`、`column_comment`、`column_type`、`java_type`、`java_field`、`is_pk`、`is_increment`、`is_required`、`is_insert`、`is_edit`、`is_list`、`is_query`、`query_type`、`html_type`、`dict_type`、`sort`、审计字段。

**`is_*` 语义**：与若依一致，`1` 表示「是」、`0` 表示「否」（导入时修正旧栈个别反转逻辑）。

### 6.3 Flyway 与菜单

迁移文件编号以实现时递增为准（如 `V18__gen_table.sql`）：

- 建表 + 索引（`table_name` 唯一）。
- 菜单：**目录**「系统工具」（`M`，如 `menu_id=2100`）→ **菜单**「代码生成」（`C`，`path=tool/gen`，`component=tool/gen/index`，`perms=tool:gen:list`）。
- **按钮**：`tool:gen:import`、`tool:gen:create`、`tool:gen:edit`、`tool:gen:remove`、`tool:gen:preview`、`tool:gen:code`。
- 为超级管理员角色授权（与 `V16`/`V17` 监控菜单做法一致）。

## 7. 核心业务流

### 7.1 导入表

1. `GET /tool/gen/db/list` 返回库表候选（支持表名、表描述筛选），**排除已导入**的 `table_name`。
2. `POST /tool/gen/importTable` 接收表名列表；**空列表** → `WarningException` + 前端提示。
3. `DbIntrospector` 读取表/列 → `converTableInfo` / `converTableField`（沿用旧栈默认规则：主键、`remark`、`create_by`、`del_flag`、`create_time`、字典注释 `[dict_type]` 等）→ 写入 `gen_table` + `gen_table_column`。

### 7.2 创建表

1. `POST /tool/gen/createTable`，Body：`{ "sql": "..." }`。
2. Druid 解析，**仅执行** `MySqlCreateTableStatement`；拒绝 `DROP`/`ALTER`/`INSERT` 等。
3. 执行成功后**不自动 import**；用户从「导入表」选择新表。

### 7.3 编辑保存

1. `GET /tool/gen/{tableId}` 返回表头 + `columns`。
2. `POST /tool/gen/update`：校验基本信息与生成信息表单（Jakarta Validation）→ 更新表头 + 批量更新列配置。

### 7.4 同步数据库

1. `GET /tool/gen/synchDb/{tableName}`：按库表重读结构 → 更新 `gen_table` 表头 → **删除并重建** 该表 `gen_table_column` 行（与旧栈一致）。

### 7.5 预览与生成

1. 组装 `GenContext`（表、列、包路径、权限前缀 `{module}:{business}` 等）。
2. **预览**：`GET /tool/gen/preview/{tableId}` → `List<GenPreviewVo>`，项为 `templateName` + `content`；前端 Tab 展示 + **复制**。
3. **生成**：`GET /tool/gen/batchGenCode?tables=table1,table2` → `application/zip`，默认 `ruoyi.zip`；流式内存打包，**不写服务端业务目录**。
4. 若 `tpl_category=tree`：生成接口返回明确业务错误「树表模板尚未开放」。

### 7.6 删除

`POST /tool/gen/remove/{tableId}`：仅删除元数据，**不删除**物理库表。

## 8. 后端接口契约

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/tool/gen/list` | `tool:gen:list` | 分页；条件：表名、表描述、创建时间区间 |
| GET | `/tool/gen/db/list` | `tool:gen:list` | 库表候选列表 |
| GET | `/tool/gen/{tableId}` | `tool:gen:list` | 详情含 `columns` |
| POST | `/tool/gen/update` | `tool:gen:edit` | 保存配置（对应需求 PUT） |
| POST | `/tool/gen/importTable` | `tool:gen:import` | 导入；`tables` 逗号分隔或 JSON 数组 |
| POST | `/tool/gen/createTable` | `tool:gen:create` | 执行建表 SQL |
| GET | `/tool/gen/preview/{tableId}` | `tool:gen:preview` | 预览 |
| POST | `/tool/gen/remove/{tableId}` | `tool:gen:remove` | 删除元数据（对应需求 DELETE） |
| GET | `/tool/gen/synchDb/{tableName}` | `tool:gen:edit` | 同步库表结构 |
| GET | `/tool/gen/batchGenCode` | `tool:gen:code` | Zip 下载；query `tables` |
| GET | `/tool/gen/genCode/{tableName}` | `tool:gen:code` | **二期**；首期未实现 |

**通用约定**：

- 除 Zip 外统一 `R<T>` / `PageInfo`。
- `@Tag("代码生成")`、`@Operation`、`@Parameter`、`@SaCheckPermission`。
- 业务失败使用 `WarningException` + `ErrorCodes.Gen.*`；禁止 `IllegalArgumentException` 作为业务信号。
- `/tool/gen/**` 建议 `@IgnoreLogger(ALL)`，避免操作日志噪声。

## 9. 代码模板（首期）

模板目录：`quickboot-web/src/main/resources/vm/quickboot/`。

| 模板文件 | 产出路径（Zip 内） |
|----------|-------------------|
| `domain.java.ftl` | `.../domain/{Class}.java` |
| `mapper.java.ftl` / `mapper.xml.ftl` | Mapper 接口 + XML |
| `service.java.ftl` / `serviceImpl.java.ftl` | Service 层 |
| `controller.java.ftl` | Controller（`R`、`POST` 删改、`@SaCheckPermission`） |
| `queryBo.java.ftl` / `bo.java.ftl` / `vo.java.ftl` | DTO |
| `api.js.ftl` | `quick-ui/src/api/{module}/{business}.js` |
| `index.vue.ftl` | C7JsonTable 列表页 |
| `menu.sql.ftl` | 菜单 SQL 片段（注释说明需手工执行） |

**不生成**：旧栈 `entity`/`do`、Ruoyi `@PutMapping`/`@DeleteMapping`、非 C7 的独立 `add-or-update.vue`（编辑配置由代码生成**管理页** `edit.vue` 完成，与被生成业务模块的表单页区分）。

**生成 Controller 参考**：`SysNoticeController`（分页 GET、详情 GET、增改 POST、删除 POST）。

**生成前端列表参考**：`views/monitor/operlog/index.vue`、`views/system/notice`（C7JsonTable、权限指令、`DESIGN.md`）。

## 10. 前端（`quick-ui`）

### 10.1 API

- `src/api/tool/gen.js`：全部指向 `/tool/gen`（不得混用 `/generator/gentable`）。

### 10.2 页面

| 页面/组件 | 职责 |
|-----------|------|
| `views/tool/gen/index.vue` | 列表：查询（表名、表描述、创建时间）；工具栏：批量生成、创建表、导入；行操作：预览、编辑、同步、删除 |
| `views/tool/gen/edit.vue` | 三 Tab：基本信息 / 字段表格（列配置）/ 生成信息；提交 `POST /tool/gen/update` |
| 导入弹窗 | `db/list` 多选；未选 `ElMessage.warning` |
| 创建表弹窗 | SQL 文本域 → `createTable` |
| 预览弹窗 | 按模板名 Tab + 复制 |

### 10.3 权限与路由

- `v-hasPermi` 与 `tool:gen:*` 一致。
- 编辑页路由隐藏（`activeMenu` 指向列表），与若依习惯一致。

实现前须阅读 `DESIGN.md`、`sdd/前端代码规范.md`、`sdd/后端代码规范.md`（见 `AGENTS.md`）。

## 11. 安全与错误码

**安全**

- `createTable`：白名单语句类型；语句数上限（配置项）。
- Zip：仅内存流；不写入 `gen_path` 等业务目录。
- `genCode` 写盘：首期不实现。

**错误码（`ErrorCodes.Gen` 示例，实现阶段定稿）**

| 场景 | 说明 |
|------|------|
| 表已导入 | 重复 `importTable` |
| 表不存在 | `synchDb` / `preview` / `get` |
| SQL 非法 | 非 CREATE TABLE 或解析失败 |
| 树模板未开放 | `tpl_category=tree` 且调用生成 |
| 导入未选表 | 与需求「未选择时阻断」一致 |

## 12. 测试与验收

对齐需求文档 §9 及本设计扩展项：

- 导入 / 创建表 / 编辑保存后返回列表并正确刷新。
- 预览内容与模板文件一一对应；**复制**可用。
- `batchGenCode` 下载成功，Zip 可解压，含预期后端与前端文件。
- 同步后列配置与库表一致。
- 菜单与 `tool:gen:*` 权限对 admin 可见可操作。
- 集成测试建议：`import` → `get` → `update` → `preview` 非空 → `batchGenCode` 解压校验关键路径。

## 13. 后续步骤

在单独会话中按 **writing-plans** 产出 `docs/superpowers/plans/2026-05-16-codegen.md` 的实现任务清单后再编码。

---

**文档版本**：2026-05-16  
**关联需求**：`原始需求/系统管理/代码生成-需求文档.md`  
**关联设计**：`docs/superpowers/specs/2026-05-16-operlog-design.md`（C7 列表、POST 删改、Flyway 菜单模式）  
**参考实现**：`原始需求/old/quick-boot`（`generator` 包、`CodeGenerator`、`template2`）、`原始需求/old/quick-ui/src/views/tool/gen`
