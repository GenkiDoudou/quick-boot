## Why

主工程缺少与《代码生成需求文档》一致的能力：无法从数据库表导入元数据、配置字段与生成选项、预览并 Zip 下载符合 `quickboot2` 规范的后端与前端脚手架代码。在系统管理工具链中补齐代码生成，可显著降低 CRUD 模块的手工搭建成本，并与已落地的监控、通知等模块保持同一技术栈与风格。

## What Changes

- 新增表 `gen_table`、`gen_table_column`（若依语义）及 Flyway 迁移；新增「系统工具 / 代码生成」菜单与 `tool:gen:*` 按钮权限。
- 新增 REST：`/tool/gen/*`（列表、库表候选、导入、建表、详情、保存、预览、同步、删除元数据、批量 Zip 下载）；修改/删除使用 **POST**（与 `AGENTS.md` 一致，需求字面 PUT/DELETE 不对外暴露）。
- 新增 `quickboot-web` 模块 `tool.gen`：元数据 CRUD、**MyBatis-Plus Generator 3.5.15** 驱动的库表内省（`db/list`、导入、同步）、**FreeMarker** 自定义模板渲染（预览与 Zip，对齐 `SysNotice` / C7 风格）。
- 新增 `quick-ui`：`api/tool/gen.js`、`views/tool/gen`（列表、三 Tab 编辑、导入/建表/预览弹窗）。
- **非 BREAKING**：均为新增表与新增接口；不改变现有业务模块契约。

## Capabilities

### New Capabilities

- `tool-codegen`：覆盖代码生成元数据管理、库表内省与导入/同步、建表 SQL、配置编辑、预览、Zip 下载、前端管理页与权限的可验收需求。

### Modified Capabilities

- （无）本期不修改 `openspec/specs/` 下既有 capability 的 REQUIREMENTS 正文。

## Impact

- **后端**：`quickboot-web`（Flyway、`io.github.genkidoudou.web.tool.gen`）、新增 Maven 依赖 **`com.baomidou:mybatis-plus-generator:3.5.15`**（建议 `scope` 以实现阶段评估为准，至少用于内省与类型映射；与项目 MyBatis-Plus 版本兼容）。
- **前端**：`quick-ui` 下 `api/tool/gen`、`views/tool/gen`。
- **其他依赖**：Druid SQL 解析（`createTable`）、FreeMarker（业务代码模板）、JDK Zip。
- **真源文档**：`docs/superpowers/specs/2026-05-16-codegen-design.md`、`原始需求/系统管理/代码生成-需求文档.md`。
- **参考实现**：`原始需求/old/quick-boot`（业务流程与默认字段规则）、`原始需求/old/quick-ui/src/views/tool/gen`（交互参考）；**库表读取以 MP Generator 为准**，不再迁移旧 `CodeGenerator` 类。
