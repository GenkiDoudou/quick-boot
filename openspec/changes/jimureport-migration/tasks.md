## 1. Maven / Modulith 脚手架

- [x] 1.1 新建 `quickboot/quickboot-module-report`（POM：依赖 `quickboot-core` + jimureport/jimubi/echarts Boot4 starter；不依赖 `module-system`）
- [x] 1.2 父 POM：`jimureport.version` / `jimubi.version` / echarts 版本、Jeecg 仓库、`dependencyManagement`；注册 `<module>quickboot-module-report</module>`；必要时迁入 `scripts/install-jimureport-maven-deps.ps1`
- [x] 1.3 `quickboot-app` 增加对 `quickboot-module-report` 的依赖；确认扫描覆盖 `io.github.genkidoudou.report`
- [x] 1.4 声明 `report` / `api` 的 `package-info`（`@ApplicationModule`、`@NamedInterface("api")`）；`ApplicationModuleSourceFactory` 追加 `io.github.genkidoudou.report`

## 2. 积木适配与目录 API

- [x] 2.1 自 bak 迁入 `api.JimuAuthBridge` 与 `internal`：`config` / `security` / `token`；AutoConfiguration.imports；**注册** `JimuPrimaryDataSourceSynchronizer` 为 Bean
- [x] 2.2 迁入 catalog（原 web.jimu）：`GET /report/jimu/catalog/reports`、`…/bi-pages`；`R<T>` + 登录鉴权
- [x] 2.3 在 `quickboot-app` 实现 `JimuAuthBridge`（对接 system 用户/角色/权限/字典）；`qc.jimu.enabled` 条件装配
- [x] 2.4 `SaTokenWebConfig`：`qc.jimu.enabled=true` 时排除 `/jmreport/**`、`/drag/**`、`/jimubi/**`、`/jimureport/**`（及 properties 列表）

## 3. Flyway 与配置

- [x] 3.1 `V14__jimureport.sql`：自 bak V3 迁入表结构 + 菜单 3000–3002 + 角色授权 + OAuth `/report/**`
- [x] 3.2 `V15__jimureport_demo.sql`：自 bak V4 迁入官方演示数据
- [x] 3.3 `V16__jimureport_2_5_upgrade.sql`：自 bak V5 迁入 2.5 增量
- [x] 3.4 yml：`qc.jimu.*`（dev `base-url` 对准现网端口）；Flyway `placeholder-replacement: false`；Druid `select-where-alway-true-check: false`；client-sign / firewall / XSS 等放行积木路径；**禁止** AI 密钥

## 4. 菜单外链与前端

- [x] 4.1 `SysPermissionServiceImpl`：对齐 bak `resolveFrameLink`（`is_frame=1` → `meta.link = base-url + query`）
- [x] 4.2 菜单 `add-or-update`：恢复打开方式「报表 / BI」+ 目录选择写 query；确认 `api/report/jimu.js` 可用
- [x] 4.3 InnerLink / IframeToggle / AppMain / TagsView：iframe URL 追加 `token=`；`InnerLink` 路由映射正常

## 5. 验证

- [x] 5.1 `mvn -pl quickboot-module-report,quickboot-app -am test`（或等价）编译 + Modulith `verify()` 通过
- [ ] 5.2 冒烟：侧栏打开报表工作台 / BI 工作台；目录 API 有数据；菜单绑定预览；确认仓库无 AI 密钥
