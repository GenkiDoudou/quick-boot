## 1. CRUD 模板与测试基架（crud-service-template）

- [x] 1.1 在 `quickboot-common` 新增 `CrudServiceImpl<M,T,V>` 与 `CrudQuerySupport`（page/get/save/update/remove/export 模板）
- [x] 1.2 可选：新增 `CrudControllerSupport` default 方法（R 包装）
- [x] 1.3 `quickboot-app` 新增 `QuickbootIntegrationTestBase`
- [x] 1.4 新增 `SysConfigCrudIT`（POST page + add 冒烟）
- [x] 1.5 `mvn -pl quickboot-app -am test -Dtest=SysConfigCrudIT` 通过

## 2. Tier-1 Vo-only 试点（tier1-vo-only-api + crud-service-template）

- [x] 2.1 `SysConfigServiceImpl` 迁 `CrudServiceImpl`；`ISysConfigService`/Controller 仅 Vo
- [x] 2.2 同样迁移：`SysDictTypeServiceImpl`、`SysDictDataServiceImpl`
- [x] 2.3 同样迁移：`SysFileClassifyServiceImpl`（配合 POST page）
- [x] 2.4 只读域薄化：`SysLogininforServiceImpl`、`SysOperLogServiceImpl`、`SysDeployRecordServiceImpl`
- [x] 2.5 Tier-1 Controller 行数审查（目标 ≤80 行）并补 OpenAPI 注解
- [x] 2.6 OpenAPI 检查 Tier-1 无 Entity schema 暴露

## 3. 统一 API 契约（unified-crud-api）

- [x] 3.1 后端：为 P1 控制器新增 POST `/page`（Dept/File/FileClassify/Online/Job/JobLog/SlowSql）
- [x] 3.2 后端：遗留 GET `/list` 加 `@Deprecated` + `Deprecation: true` 转发
- [x] 3.3 前端：新增 `src/api/_factory/createCrudApi.js`
- [x] 3.4 迁移 API：`config.js`、`dict/type.js`、`dict/data.js` 使用 factory
- [x] 3.5 迁移 API：`job.js`、`jobLog.js`、`online.js`、`slowSql.js`、`fileClassify.js`、`file.js`、`dept.js`
- [x] 3.6 P2：`menu.js`、`gen.js` POST page（树形 list 别名保留文档说明）

## 4. 菜单路由正式化（menu-route-api）

- [x] 4.1 新增 `MenuRouteController`：`GET /api/menu/routes`
- [x] 4.2 `/getRouters` deprecated 转发；删除 `ScaffoldCompatController`
- [x] 4.3 前端 `api/menu.js` 新增 `getMenuRoutes()`；`permission.js`/store 改用新 API
- [x] 4.4 冒烟：登录后动态菜单与路由组件加载正常

## 5. 横切收拢（crosscut-consolidation）

- [x] 5.1 system：合并 OperLog Assembler/Meta/Persist → `OperLogRecorder` + Listener
- [x] 5.2 monitor：LogHub 仅通过 `OperLogMonitorQuery` 等 api Facade 查询
- [x] 5.3 SlowSQL：persist 逻辑确认仅在 monitor；app 仅 Filter 注册
- [x] 5.4 common：新增 `ExceptionReporter`；monitor 实现；app GEH 改 SPI 注入
- [x] 5.5 删除 `common.file.FileClassifyVo`；引用改 system Vo
- [x] 5.6 monitor pom/source：去除所有 `system.internal` / `quartz.internal` import
- [x] 5.7 各 module `package-info.java` 加 `@ApplicationModule`；启用/扩展 `ModularityTests`

## 6. 前端 schema 与路由（frontend-crud-schema）

- [x] 6.1 新增 `useCrudPage` composable 或 `CrudPage` 薄壳组件
- [x] 6.2 Tier A 迁移 10 页：config、dict/type、dict/data、fileClassify、online、logininfor、operlog、deployRecord、slowSql、job-log
- [x] 6.3 新增 `utils/formatTime.js`（dayjs）；时间列统一 formatter
- [x] 6.4 Flyway ADD：三条 hidden 菜单（dict-data、user-auth、gen edit）
- [x] 6.5 删除 `router/index.js` dynamicRoutes + `filterDynamicRoutes`

## 7. 前端激进清理（frontend-dead-code-cleanup）

- [x] 7.1 删除 `api/knowledge/*`、`api/ai/*`、`api/workflow/*`（14 文件）
- [x] 7.2 迁移 `handleTree`/`getNormalPath`/`parseTime` 至独立 utils；收缩 `ruoyi.js`
- [x] 7.3 移除 `main.js` 全局 ruoyi 挂载（parseTime 等）
- [x] 7.4 移除 `lodash` 依赖（确认零引用）
- [x] 7.5 新增 C7JsonTable 等 Vitest 用例后删除 `views/dev/C7*E2E.vue`
- [x] 7.6 评估 C7Button/C7Title：deprecated 或删除

## 8. Tier-2 骨架与 codegen（tier1-vo-only-api + fullstack-codegen）

- [x] 8.1 User/Role/OauthClient/File/Job Controller 薄化（保留业务 slot 逻辑）
- [x] 8.2 tool 模块：Freemarker 模板输出 `CrudServiceImpl` + Vo-only Controller
- [x] 8.3 tool 模块：生成 `createCrudApi` js + schema `index.vue`
- [x] 8.4 tool 模块：生成 Flyway 菜单/权限 INSERT 模板
- [x] 8.5 更新 `.cursor/skills/quickboot-system-codegen/SKILL.md` 与方案文档交叉引用

## 9. 验证与文档

- [x] 9.1 `mvn clean install -DskipTests` 通过
- [x] 9.2 `quick-ui` `pnpm build` 通过
- [x] 9.3 冒烟：登录 → 菜单 → Config CRUD → 导出 Excel → 操作/登录日志
- [x] 9.4 集成测试数量 ≥ 阶段目标（累计 toward 40）
- [x] 9.5 更新 `docs/docs/guide/fullstack-simplify-plan.md` 状态为「实施中/已完成」并链到本 change
