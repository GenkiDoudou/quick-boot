## Why

quickboot 后端（~429 main Java）与 quick-ui（43 个 api 模块、16 套同质 C7JsonTable CRUD 页）存在大量重复样板：Entity/Vo 双轨、GET `/list` 与 POST `/page` 混用、横切链路（操作日志/慢 SQL）跨 4 个模块、14 个无后端的前端 API 文件。维护成本高、新人认知负担重，且与 `code_formater.md` 中 Modulith / 统一契约目标未完全对齐。现已有 `docs/docs/guide/fullstack-simplify-plan.md` v1.1 规划，需转为可执行的 OpenSpec change 分阶段落地。

## What Changes

- 新增 **`CrudServiceImpl`**（`quickboot-common`）与集成测试基架，统一 13+ 套 Service 分页/拷贝风格。
- **Tier-1 CRUD Vo-only API**：Controller/`ISysXxxService` 不暴露 Entity；试点 Config/Dict/FileClassify 等。
- **统一 CRUD API 契约**：标准 `POST .../page`；遗留 GET `/list` 保留 4 周 deprecated 别名；Quartz 前缀保持 `monitor/job`。
- 前端 **`createCrudApi` 工厂** + **schema 驱动 C7JsonTable 页**（Tier A 10 页首批）。
- **菜单路由正式化**：`MenuRouteController` + `GET /api/menu/routes`；**BREAKING** 弃用 `ScaffoldCompatController` / `/getRouters`（兼容转发 1 版本）。
- **横切收拢**：OperLog 落库合并为 `OperLogRecorder`；SlowSQL 内聚 monitor；monitor 仅依赖 `system.api`/`quartz.api`；`ExceptionReporter` SPI。
- **激进清理**：删除 knowledge/ai/workflow 无后端 API（14 文件）；收缩 `ruoyi.js`；dev E2E → Vitest；评估删除 C7 薄封装。
- 扩展 **tool 全栈 codegen**：后端 + 前端 api + schema 页 + Flyway 菜单。
- **Modulith 落地**：各 module `@ApplicationModule` + `ModularityTests`。
- **不变**：表结构约定、`ISysXxxService` 命名、`quickboot-core` 与 `quickboot-common` **不合并**。

## Capabilities

### New Capabilities

- `crud-service-template`: `CrudServiceImpl`/`CrudQuerySupport` 与 `QuickbootIntegrationTestBase` 集成测试基架。
- `unified-crud-api`: 标准 CRUD HTTP 契约、GET list deprecated 迁移、前端 `createCrudApi` 工厂。
- `tier1-vo-only-api`: Tier-1 系统域 Vo-only API（Entity 仅在 Service/Mapper 内部）。
- `menu-route-api`: 正式菜单路由 API，替代 ScaffoldCompat。
- `crosscut-consolidation`: OperLog/SlowSQL 链路收拢、monitor Maven 解耦、GlobalExceptionHandler SPI。
- `frontend-crud-schema`: schema 驱动 C7JsonTable 页、时间展示统一、dynamicRoutes 迁入 sys_menu。
- `frontend-dead-code-cleanup`: 无后端 API 删除、ruoyi 遗留收缩、dev E2E 迁 Vitest、C7 薄封装评估。
- `fullstack-codegen`: tool 模块扩展，生成 Vo-only 后端 + createCrudApi + schema 页。

### Modified Capabilities

- （`openspec/specs/` 尚无主 specs；本次均为新增 delta specs，归档后成为主 specs。）

## Impact

- **后端**：`quickboot-common`（Crud 模板）、`quickboot-module-system`（Tier-1 迁移、MenuRoute）、`quickboot-module-monitor`（解耦、SlowSQL）、`quickboot-module-quartz`（POST page）、`quickboot-module-tool`（codegen）、`quickboot-app`（测试、ExceptionReporter）。
- **前端**：`quick-ui/src/api/_factory/`、`views/**/index.vue`（Tier A/B）、`api/menu.js`、`router/`、`utils/`。
- **文档**：`docs/docs/guide/fullstack-simplify-plan.md` 为规划来源；本 change 为实施契约。
- **BREAKING**：部分 GET `/list` 端点 deprecated；`/getRouters` 更名（有兼容期）。
- **非目标**：表结构 ALTER、core/common 模块合并、quartz 路径改为 `sys/job`。
