## Context

权威产品设计见 `docs/superpowers/specs/2026-08-08-online-user-and-codegen-migration-design.md`。本文件为 OpenSpec 实现向设计。

现状：现网已有 Modulith（`common` / `core` / `module-system` / `module-quartz` / `app`）与 operlog / logininfor / job 等监控能力；在线用户与代码生成仍在 `bak`（`system/online`、`tool/gen` + 对应 UI）。登录已基于 Sa-Token；C7 分页与 job-log 扁平 query 映射模式可复用。

约束：在线落 `module-system`；生成新建 `module-tool`；行为对齐 bak 已实现全量；配置前缀 `qc.gen.*`；沿用 FreeMarker，不引入 Velocity。

## Goals / Non-Goals

**Goals:**

1. 在线用户：列表（IP/用户名筛选、内存分页）+ 强退；登录成功写入 Token-Session 展示字段；前端页 + 菜单权限。
2. 新建 `quickboot-module-tool` 并注册到父反应器与 `app`；迁入 bak gen 全量 API、表结构、FreeMarker 模板（生成物路径对齐现网）。
3. 前端 `monitor/online`、`tool/gen`；Modulith `verify()` 通过。

**Non-Goals:**

- 慢 SQL、客户端轨迹、报表等其它 bak 监控/工具。
- RuoYi Velocity；自动 `INSERT sys_menu`（可保留 menu.sql 模板片段）。
- 树表/主子表若 bak 未开放，保持「配置可存、生成提示未开放」。
- 为在线用户建业务表。

## Decisions

### 1. 同一变更交付两块能力

- 产品已定「一起交付」；tasks 分「在线 → tool 脚手架 → gen 后端 → gen 前端 → 验收」，可并行但同一 change。
- 备选：拆两个 change → 否决（设计已合并）。

### 2. 在线落 module-system；会话无表

- 路径：`internal/.../online/`（controller / service / dto / support）。
- API：`GET /monitor/online/list`、`POST /monitor/online/forceLogout`；权限 `monitor:online:list|forceLogout`；建议 `@IgnoreLogger(Type.ALL)`。
- 登录成功、`LoginHelper.loginByDevice` 之后调用 `OnlineSessionRecorder` 写会话字段（用户名、部门、IP、登录地、浏览器、OS、登录时间）。
- 列表：`StpUtil.searchTokenValue` + bak 式 `resolveTokenValue`；内存筛选分页；注意 C7 `{current,size,param}` → 扁平 GET（对齐 job-log）。
- 备选：独立 monitor 模块 → 否决（体量小、与登录同属 system）。

### 3. 新建 quickboot-module-tool

```text
io.github.genkidoudou.tool/
  package-info @ApplicationModule
  api/          @NamedInterface（本期可空）
  internal/     gen controller/service/mapper/entity/dto/config/support + ftl
```

- 依赖：`quickboot-core`；默认不依赖 `module-system`。
- `ApplicationModuleSourceFactory` 追加 `io.github.genkidoudou.tool`。
- 参照 `new-domain-module-template.md` 与 quartz 先例。

### 4. 代码生成行为与配置

- 前缀 `/tool/gen`；权限 `tool:gen:list|import|create|edit|remove|preview|code`。
- 能力对齐 bak `GenController`：配置分页、库表候选、defaults、详情、保存、导入、建表 SQL、预览、删除、同步、Zip、写盘。
- Flyway：`gen_table` / `gen_table_column` + 菜单种子；配置键 `qc.gen.*`（author、package-name、module-name、zip-file-name、create-table-max-statements、写盘根路径等）。
- 建表：仅允许 `CREATE TABLE`；语句数上限可配；写盘须路径校验防穿越。
- 模板：自 bak `vm/quickboot/**/*.ftl` 迁入；输出包路径/分层对照 `SysUser` / C7 页样板改写。

### 5. 前端

- 在线：`api/monitor/online.js` + `views/monitor/online`。
- 生成：迁 bak `views/tool/gen`（index / edit + 导入 / 建表 / 预览）并对齐现网 `request`、权限指令、分页。

## Risks / Trade-offs

- [一次迁两块体量大] → tasks 分段；冒烟分能力验收。
- [ftl 与现网分层不一致] → 改模板输出路径/类名，对照现网样板。
- [C7 分页与 GET query 不一致] → 复用 job-log 扁平映射。
- [Sa-Token searchTokenValue 前缀差异] → 复用 bak `resolveTokenValue` 并冒烟。
- [写盘目录穿越] → 根路径配置 + 规范化校验拒绝越界。

## Migration Plan

1. Flyway（online 菜单 + gen 表/菜单；序号按现网最新 `V*` 顺延）。
2. 在线后端 + 登录挂钩 + 前端。
3. `module-tool` 脚手架 + Modulith 基包。
4. 迁 gen 后端/模板/配置；编译 + `verify()`。
5. gen 前端联调。
6. 按设计验收清单勾选。

回滚：删 online/tool 代码与依赖、Flyway 文件（已执行环境清菜单/表）、前端文件；以 Git 为准。

## Open Questions

- Flyway 最终序号、菜单精确 ID：实现时核对现网后写入 tasks 说明（online / tool 建议挂监控或系统工具父菜单，避免与已用 ID 冲突）。
- 写盘根路径默认值与是否仅允许相对工程路径：实现时按 bak 语义 + 安全校验落配置项。
