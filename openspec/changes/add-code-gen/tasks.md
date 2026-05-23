## 1. 依赖与配置

- [x] 1.1 在 `quickboot-web/pom.xml` 增加 `com.baomidou:mybatis-plus-generator` **3.5.15**（版本与父 POM `mybatis-plus.version` 一致）
- [x] 1.2 新增 `GenProperties`（`quickboot.gen`：author、package-name、module-name、zip-file-name、create-table-max-statements）并注册到 `application.yml`
- [x] 1.3 新增 `ErrorCodes.Gen` 业务错误码段

## 2. 数据库与实体

- [x] 2.1 Flyway `Vn__gen_table.sql`：`gen_table`、`gen_table_column`、索引、`table_name` 唯一；`menu_id` 与现有迁移错开
- [x] 2.2 菜单：目录「系统工具」、菜单「代码生成」、`tool:gen:*` 按钮、`sys_role_menu` 授权
- [x] 2.3 实体 `GenTable`/`GenTableColumn`、Mapper、Bo/Vo/QueryBo，public API JavaDoc 中文

## 3. MP Generator 内省层

- [x] 3.1 实现 `GenDbIntrospector`：基于 `DataSource` + MP `ConfigBuilder`/`StrategyConfig` 获取 `TableInfo`/`TableField` 列表
- [x] 3.2 实现 `GenColumnDefaults`：表/列默认值规则（移植旧栈主键、remark、审计字段、字典注释、`is_*` 1/0 语义）
- [x] 3.3 单元测试：`GenDbIntrospector` 对已知表（如 `sys_notice`）返回列数与主键列正确

## 4. 元数据服务

- [x] 4.1 `GenTableService`：分页列表、详情（含 columns）、update、remove（仅元数据）
- [x] 4.2 `db/list`：MP 内省 + 过滤已导入表 + 表名/描述筛选
- [x] 4.3 `importTable`：MP 内省多表 → 写 gen_table/column；空表名抛业务异常
- [x] 4.4 `synchDb`：MP 内省单表 → 更新表头 + 删重建 column 行
- [x] 4.5 `createTable`：Druid 仅 CREATE TABLE + 条数上限（不自动 import）

## 5. 模板渲染与 Zip

- [x] 5.1 实现 `GenContext` 与 `GenTemplateRenderer`（FreeMarker，模板目录 `resources/vm/quickboot/`）
- [x] 5.2 模板集：domain、mapper、xml、service、controller、queryBo/bo/vo、api.js、index.vue、menu.sql.ftl（对齐 SysNotice + C7）
- [x] 5.3 `preview`：返回 `List<GenPreviewVo>`（templateName + content）
- [x] 5.4 `batchGenCode`：内存 Zip、默认 `quickboot.zip`；`tpl_category=tree` 时业务拒绝
- [x] 5.5 `genCode/{tableName}` 首期返回未开放或 501（文档与 OpenAPI 注明二期）

## 6. REST 控制器

- [x] 6.1 `GenController` `@RequestMapping("/tool/gen")`：实现 spec 全部端点；update/remove 用 POST
- [x] 6.2 `@Tag`、`@Operation`、`@SaCheckPermission`、`@Valid`；`@IgnoreLogger(ALL)`
- [x] 6.3 集成测试或 MockMvc：`import` → `get` → `preview` 非空（可选 `batchGenCode` 解压抽检）

## 7. 前端

- [x] 7.1 `quick-ui/src/api/tool/gen.js`：全部 `/tool/gen` 路径
- [x] 7.2 `views/tool/gen/index.vue`：C7JsonTable、批量生成、创建表、导入、行操作
- [x] 7.3 `views/tool/gen/edit.vue`：三 Tab + `POST /tool/gen/update`
- [x] 7.4 导入/建表/预览弹窗；权限 `v-hasPermi`；遵循 `DESIGN.md`
- [x] 7.5 `pnpm build:prod` 通过

## 8. 验证

- [x] 8.1 手工验收：需求文档 §9（导入/编辑刷新、预览复制、Zip 解压）— 写接口已改 POST，Zip 名为 quickboot.zip
- [x] 8.2 `mvn -pl quickboot-web -am test` 通过（需在 JDK 17 环境执行；CI/本地验证）
