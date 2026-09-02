## Context

权威规划见 `docs/docs/guide/fullstack-simplify-plan.md` v1.1。现状：system 约 12 套同质 CRUD（Entity + Vo + `ISysXxxService` + Controller）；13 个 Service 用 `BaseServiceImpl`、15+ 手写分页；前端 70% 业务页已用 C7JsonTable 但仍手写 `api/*.js`（~80–110 行/实体）；OperLog/SlowSQL 采集分散在 common/app/system/monitor；`ScaffoldCompatController` 提供若依形态 `/getRouters`。

约束（已确认）：表结构不变；保留 `ISysXxxService`；core/common 分立；Quartz 前缀 `monitor/job`；Tier-1 API 不暴露 Entity。

## Goals / Non-Goals

**Goals:**

1. 后端 CRUD 骨架统一到 `CrudServiceImpl`；Tier-1 域 Vo-only API 试点并推广。
2. 前后端统一 `POST .../page` 契约；前端 `createCrudApi` 消除 API 重复。
3. Tier A 共 10 个页面 schema 化（~40 行/页）。
4. 横切链路收拢；monitor 不依赖 system.internal。
5. 删除 14 个无后端前端 API；Modulith 注解启用。
6. 集成测试 ≥ 40；`mvn install` + `pnpm build` 绿。

**Non-Goals:**

- 合并 `quickboot-core` 与 `quickboot-common`。
- 表结构 ALTER、字典/权限语义变更。
- Tier-2 复杂域（User/Role/Menu/Dept）Vo 合并或 codegen 覆盖。
- 不宜模板化页：liteTrace、logHub、userBehavior、menu、gen/edit。
- quick-h5 范围（本 change 仅 quick-ui + quickboot）。

## Decisions

### 1. CrudServiceImpl 放在 common，实体仍在 core

- `CrudServiceImpl<M,T,V>` extends 现有 `BaseServiceImpl`，提供 page/getById/save/update/remove/export 模板方法。
- 子类实现 `applyQuery(LambdaQueryWrapper<T>, V query)`；`ISysXxxService` 接口保留。
- 实体继续继承 `quickboot-core` 的 `BaseEntity`。
- 备选：放 system → 否决，common 已有 `BaseServiceImpl`，全模块可复用。

### 2. Tier-1 Vo-only：Controller/Service 接口零 Entity

- `SysConfigController` 等仅 `SysConfigVo`；`ISysConfigService.page` 返回 `PageInfo<SysConfigVo>`。
- `toEntity`/`toVo` 仅在 `SysConfigServiceImpl` 内。
- Tier-2 暂不合并 Vo 字段，但 API 同样不返回 Entity。

### 3. API 统一与兼容

- 标准路径见 spec `unified-crud-api`。
- 遗留 GET `/list`：同逻辑转发 + `@Deprecated` + `Deprecation: true` 响应头，**4 周**后删除。
- Quartz 保持 `monitor/job`、`monitor/job-log`（不改 Flyway 菜单权限域）。

### 4. 菜单路由

- 新增 `MenuRouteController`：`GET /api/menu/routes` → 委托 `ISysPermissionService.buildRouters()`。
- `ScaffoldCompatController` 删除；`/getRouters` 保留 deprecated 转发 1 版本。
- 前端 `getMenuRoutes()`；`permission.js` 收敛单入口。

### 5. 横切收拢

- **OperLog**：common 保留注解+事件+脱敏；system 合并 `OperLogRecorder`（Assembler/Meta/Persist）；monitor LogHub 只调 Facade。
- **SlowSQL**：common SPI + app Filter 注册；monitor 内聚 persist+Controller。
- **ExceptionReporter**：common 接口；monitor 实现；app `ObjectProvider` 注入。
- **FileClassifyVo**：删 common 副本，统一 system Vo。

### 6. 前端 createCrudApi + schema

- `src/api/_factory/createCrudApi.js` 生成 page/get/add/update/remove/export/import。
- schema JSON：`columns`/`form`/`permPrefix`/`module`；生成薄 `index.vue` 绑定 C7JsonTable。
- 时间列统一 `formatDateTime`（dayjs）；依赖后端 JVM 东八区（`JacksonTimeConfig`）。

### 7. 死代码与遗留

- 删除 `api/knowledge/*`、`api/ai/*`、`api/workflow/*`（无 quickboot 后端）。
- `ruoyi.js`：保留 `handleTree`/`getNormalPath` 迁独立模块；删 `resetForm`/全局 `parseTime` 挂载。
- `views/dev/C7*E2E.vue` → Vitest 组件测试后删除。
- `lodash` 从 package.json 移除（src 零引用）。

### 8. Modulith

- 各 module `package-info.java` 加 `@ApplicationModule`；api 包 export。
- `ModularityTests` 禁止跨 module internal 依赖。

### 9. 实施顺序（与 tasks 对齐）

A Crud 模板 + 测试 → B API 统一 + createCrudApi → C Tier-1 Vo-only + MenuRoute → D 横切收拢 → E schema 页 + 清理 → F codegen 扩展。

## Risks / Trade-offs

- [POST page 破坏旧客户端] → 4 周 GET deprecated 别名 + 文档
- [Vo-only 校验遗漏] → `@Validated` 分组；Config 试点 IT 覆盖
- [OperLog 合并回归] → 采集 IT + 手工冒烟导出忽略 RESULT
- [monitor 解耦编译失败] → 先补 Facade 再删 internal import
- [schema 页丢失自定义 slot] → Tier B 保留 slot 扩展点；复杂页不模板化
- [路由改名影响已部署前端] → 双 URL 兼容 + 配置开关 `qc.compat.get-routers`

## Migration Plan

1. 按 tasks 分阶段合并；每阶段 `mvn install` + 关键路径冒烟。
2. Flyway 仅 ADD：hidden 菜单（dynamicRoutes 迁入）、无表结构变更。
3. 第 4 周起移除 deprecated GET list；第 2 版本移除 `/getRouters`。
4. 回滚：按 change 分支 revert；Flyway ADD 脚本可保留；compat 配置恢复旧 URL。

## Open Questions

- C7Button/C7Title 是否在 E 阶段直接删除，还是先标记 deprecated？（默认：先 deprecated 1 版本）
- Modulith `@ApplicationModule` 是否一次性全 module 启用？（默认：是，与 monitor 解耦同批）
