## Context

权威产品设计见 `docs/superpowers/specs/2026-08-08-jimureport-migration-design.md`。本文件为 OpenSpec 实现向设计。

现状：现网已完成 Modulith（`common` / `core` / `module-system` / `module-quartz` / `app`）。bak 在 `quickboot-report` + web `jimu` catalog / AuthBridge + Flyway V3–V5 已跑通「报表工作台」「BI工作台」iframe；主仓仅残留 `quick-ui/src/api/report/jimu.js` 与菜单文案裁剪痕迹。

约束：新域模板一对一；包根 `io.github.genkidoudou.report`；report **不依赖** system.internal；AuthBridge 实现在 app；含官方演示数据；不迁 Jimu AI / 密钥。

## Goals / Non-Goals

**Goals:**

1. 新建 `quickboot-module-report` 并注册到父反应器、`app`、Modulith 基包。
2. 迁入积木完整适配（Token/分享 Filter、主库同步、Drag Redis 修补、目录 API）与两个工作台菜单入口。
3. Flyway V14–V16（表+菜单+OAuth、演示、2.5 增量）与 `qc.jimu` / 放行 yml。
4. system `resolveFrameLink` + Sa-Token 排除；前端菜单积木选项与 iframe `token`。
5. `ModularityTests` 通过；手工可打开两个工作台。

**Non-Goals:**

- Jimu AI；自研非 iframe 报表页；慢 SQL JIMU 标签；与 online/codegen 同变更。

## Decisions

### 1. 独立 `quickboot-module-report`（非塞进 system）

- Maven ↔ Modulith `report` 一对一。
- 备选：挂 app / 并入 system → 否决（边界与已批决策不符）。

### 2. 包与跨模块桥

```text
io.github.genkidoudou.report/
  api/JimuAuthBridge
  internal/{config,security,token,catalog}
```

- Catalog Controller 在 report.internal，路径 `/report/jimu/catalog/*`。
- `JimuAuthBridge` 实现仅在 **app**，注入 system 服务；report 源码不引用 `system.internal`。
- `JimuPrimaryDataSourceSynchronizer` **必须**注册为 Bean（补 bak 缺口）。

### 3. 第三方依赖

- `jimureport-spring-boot4-starter` 2.5.0、`jimubi-spring-boot4-starter` 2.5.0、`jimureport-echarts-starter` 2.3.0。
- 父 POM：Jeecg 仓库 + 版本属性；必要时迁入 `scripts/install-jimureport-maven-deps.ps1`。

### 4. iframe 与鉴权

- 菜单种子：3000 目录；3001 → query `/jmreport/list`；3002 → `/drag/list`；`is_frame=1`。
- `SysPermissionServiceImpl`：`is_frame=1` 时 `meta.link = qc.jimu.base-url + query`（绝对 URL 直通）。
- 积木路径排除 Sa-Token 拦截；`JimuReportTokenServiceImpl` + Header Bridge 鉴权；iframe URL 追加 `token=`。

### 5. Flyway 与配置

- V14 / V15 / V16 分别对应 bak V3 / V4 / V5；`placeholder-replacement: false`；Druid `select-where-alway-true-check: false`。
- 不写入任何 AI api-key。
- H2：尽力而为；验收优先 MySQL/MariaDB（与 bak 一致）。

### 6. 前端

- 恢复菜单打开方式 report/bi + 目录选择；复用现有 InnerLink / TagsView 登记逻辑；IframeToggle 带 token。
- 无独立报表 Vue 业务页。

## Risks / Trade-offs

- [H2 不兼容官方 MySQL DDL] → 文档标明；验收用 MySQL。
- [Jeecg 依赖解析失败] → 仓库 + install 脚本。
- [AuthBridge 破坏 Modulith] → 实现只在 app。
- [V15 约 9MB] → 单独文件；接受体积。
- [现网外链只认 http path] → 显式改 resolveFrameLink。

## Migration Plan

1. POM / 仓库 / module-report 骨架 + app 依赖 + 基包。
2. 迁入 report 代码 + catalog；app AuthBridge；同步器 Bean。
3. Flyway V14–V16 + yml 放行与开关。
4. system resolveFrameLink；Sa-Token 排除。
5. 前端菜单与 iframe token。
6. 编译、`verify()`、手工打开两个工作台与目录 API。

回滚：删模块与依赖、Flyway 文件（已执行环境需清积木表/菜单）、前端改动；以 Git 为准。

## Open Questions

- 本地 `qc.jimu.base-url` 端口以现网 `server.port` 为准（设计示例 9993），实现时写入 yml 注释。
- Catalog 接口权限字符：实现时对齐 bak / 现网菜单管理权限，不扩大匿名面。
- `qc.jimu.enabled=false` 时 Bean 条件装配细节：实现时用 `@ConditionalOnProperty`，保证应用可启动。
