# 登录日志与登录失败锁定 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 交付登录日志 CRUD/导出/解锁及登录失败锁定能力；锁定状态仅通过 `CacheManager` 维护；阈值与开关在 `sys_config` 内置参数中可配且不可删除。

**Architecture:** Flyway 建 `sys_logininfor` 表并种子内置参数；`LoginLockService`（命名以仓库为准）封装 `CacheManager.getCache("qc-login-fail#...")` 的 get/put/evict；`AuthController`/`AuthLoginService` 在登录前后调用锁定与写日志；`monitor/logininfor` 包提供分页与导出；`quick-ui` 增加 API、路由与列表页。

**Tech Stack:** Spring Boot 3、MyBatis-Plus、Sa-Token、`SysConfigService`、Spring `CacheManager`（`DynamicTtlRedisCacheManager` 或 Caffeine，由现有自动配置决定）、Flyway、Vue 3 + 现有表格/权限体系。

**设计依据：** `docs/superpowers/specs/2026-05-16-logininfor-design.md`

---

## 文件结构（预计创建/修改）

| 区域 | 路径（示例，实现时可微调包名） |
|------|--------------------------------|
| 迁移 | `quickboot-web/src/main/resources/db/migration/V17__sys_logininfor.sql`（与现有 `V15`/`V16` 错开，避免版本冲突） |
| 实体/Mapper | `.../monitor/logininfor/domain`、`mapper`、`SysLogininforMapper.xml`（若项目用 XML） |
| 服务 | `.../monitor/logininfor/service`、`LoginLockService`（或 `.../auth/LoginLockService` 与登录就近） |
| 控制器 | `.../monitor/logininfor/LogininforController` |
| DTO | `SysLogininforQueryBo`、`SysLogininforVo` 等 |
| 登录 | 修改 `AuthController.java`、`AuthLoginService.java` |
| 前端 API | `quick-ui/src/api/monitor/logininfor.ts` |
| 前端页面 | `quick-ui/src/views/monitor/logininfor/index.vue`（路径与路由约定对齐） |
| 路由/菜单 | 前端路由表 + Flyway `sys_menu` / `sys_role_menu` |

---

### Task 1: Flyway — `sys_logininfor` 表

**Files:**
- Create: `quickboot-web/src/main/resources/db/migration/V{next}__sys_logininfor.sql`

- [ ] 编写 DDL：`info_id` PK、`user_id`、`user_name`、`ipaddr`、`login_location`、`browser`、`os`、`status`、`msg`、`login_time`（索引）、审计字段若项目表惯例需要则对齐 `sys_user`。
- [ ] 本地执行 `mvn -pl quickboot-web test` 或启动验证迁移无报错。

---

### Task 2: Flyway — 内置参数（登录锁定）

**Files:**
- Modify: 同上迁移文件或单独 `V{next}__sys_config_login_lock.sql`

- [ ] `INSERT` 三条（或合并为一条迁移）：`qc.login.fail-lock-enabled`、`qc.login.max-retry`、`qc.login.lock-minutes`；`config_type='1'`；`config_value` 为约定默认值；`config_id` 不与现有种子冲突。
- [ ] 确认 `uk_sys_config_key` 下键唯一。

---

### Task 3: 领域模型与 Mapper

**Files:**
- Create: Entity、Mapper 接口、`SysLogininforMapper.xml`（若全注解则省略 XML）

- [ ] Entity 与表字段一致，MyBatis-Plus `@TableName` / `@TableField`。
- [ ] `selectPage` 条件：ip、userName、status、`login_time` between；`orderBy` 动态。

---

### Task 4: `LoginLockService`（仅 CacheManager）

**Files:**
- Create: `.../auth/LoginLockService.java`（或 monitor 下，以「仅登录使用」为准放 auth）

- [ ] 注入 `CacheManager`，常量缓存名如 `qc-login-fail#604800`（与设计文档一致）。
- [ ] 方法：`isLocked(String userName)`、`recordFailure(String userName)`、`clearForUserName(String userName)`、`onLoginSuccess(String userName)`。
- [ ] 从 `SysConfigService` 读取三个 config key；`enabled=false` 时 `recordFailure` no-op，`isLocked` false。
- [ ] 单元测试：使用 `DynamicTtlCaffeineCacheManager` 或 `@SpringBootTest` + 测试配置 mock `SysConfigService` 固定返回值。

---

### Task 5: 接入登录流程

**Files:**
- Modify: `AuthController.java`、`AuthLoginService.java`

- [ ] `AuthController#login`：trim 用户名；在 authenticate 前调用 `isLocked`；捕获凭据失败时 `recordFailure` 并写失败日志；成功路径 `onLoginSuccess` + 写成功日志。
- [ ] 抽取「写登录日志」到独立小服务，避免 Controller 膨胀；UA/IP/地理解析复用项目已有工具或 Hutool 轻量实现。
- [ ] 写日志失败 try/catch，打 error，不阻断登录。

---

### Task 6: 登录日志 Controller + Service

**Files:**
- Create: Controller、Service、QueryBo、Vo、Export DTO（若与 Vo 复用则合并）

- [ ] `GET /monitor/logininfor/list` → `R<PageInfo<Vo>>`。
- [ ] `POST /monitor/logininfor/remove` 批量 ID；`POST /monitor/logininfor/clean`；`POST /monitor/logininfor/export` Excel（对齐 `ExcelUtils` 用法）。
- [ ] `GET /monitor/logininfor/unlock/{userName}` → 调用 `clearForUserName` + 明确成功消息。
- [ ] `@SaCheckPermission` 与 OpenAPI 注解齐全。

---

### Task 7: 字典与状态

**Files:**
- Modify: `V3__sys_dict.sql` 或新迁移追加字典类型与数据（若尚无「登录状态」类字典）

- [ ] 列表 `status` 与字典 `sys_dict` 对齐；与写库时赋值约定一致。

---

### Task 8: 菜单与权限种子

**Files:**
- Create: Flyway `sys_menu`、`sys_role_menu` 插入（登录日志；父级挂在系统管理或「监控」目录，与产品一致）

- [ ] `perms` 与需求四点一致；`menu_id` 不与 V9/V14 等冲突。

---

### Task 9: `quick-ui` — API 与页面

**Files:**
- Create: `src/api/monitor/logininfor.ts`、`src/views/monitor/logininfor/index.vue`
- Modify: 路由模块（与现有 `system/config` 注册方式一致）

- [ ] 列表、查询、多选删、清空、导出、解锁按钮及确认框。
- [ ] 权限指令与后端一致。
- [ ] `pnpm build:prod` 通过。

---

### Task 10: 集成验证

- [ ] 手工：错误密码 N 次锁定；解锁后可登录；参数页改 `max-retry` 生效；内置参数删除被后端拒绝。
- [ ] 可选：`@SpringBootTest` 测 `remove`/`unlock` 接口返回结构。

---

## 注意事项

- 业务代码中**不得**出现 `RedisTemplate`/`StringRedisTemplate` 操作锁定数据。
- 删除/清空接口使用 **POST**（见设计文档）。
- 内置参数删除已由 `SysConfigServiceImpl.removeBatch` 拦截；前端对 `configType=== '1'` 删除按钮禁用可与参数页现有一致。
