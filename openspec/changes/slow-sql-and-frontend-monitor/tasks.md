## 1. Maven / Modulith：module-monitor

- [x] 1.1 新建 `quickboot/quickboot-module-monitor`（POM 依赖 `quickboot-core`；按需依赖 `quickboot-module-system` 仅用于 `api`）
- [x] 1.2 父 POM 注册模块；`quickboot-app` 增加依赖；`package-info`（`@ApplicationModule` 含 `system::api`、`@NamedInterface("api")`）；`ApplicationModuleSourceFactory` 追加 `io.github.genkidoudou.monitor`
- [x] 1.3 确认组件扫描 / MapperScan 覆盖 `io.github.genkidoudou.monitor`

## 2. 数据与权限（Flyway）

- [x] 2.1 新增下一可用 `V*`：`sys_slow_sql`、`sys_client_track`（自 bak 终态 DDL 适配）
- [x] 2.2 同迁移：监控下菜单（慢 SQL、前端监控、事件链路、行为轨迹、全链路）+ 按钮权限与管理员授权；menu_id 避开已占用段

## 3. 慢 SQL 采集与管理

- [x] 3.1 迁入 common：`SlowSqlProperties`、采集事件/Support、MyBatis mapper_id 拦截器与 AutoConfiguration；配置前缀 `qc.monitor.slow-sql.*`
- [x] 3.2 app：迁入/装配 Druid `SlowSqlFilter`（及可执行 SQL 解析支撑）；`ignore-sql-contains` 覆盖自写递归
- [x] 3.3 monitor.internal.slowsql：entity/mapper/service/controller/persist listener/dto；路径 `/monitor/slowSql`；同步 Excel 导出；权限字对齐 bak
- [x] 3.4 前端：`api/monitor/slowSql.js` + `views/monitor/slowSql`；分页与权限对齐现网监控页

## 4. 前端监控（clientTrack + 埋点）

- [x] 4.1 monitor.internal.clienttrack：entity/mapper/service/controller/support/dto；`/report` 需登录不校验菜单权限；list/timeline/timeline/page/remove/clean
- [x] 4.2 迁入 `quick-ui/src/monitor/**` 及上报依赖工具；入口注册插件；request 拦截器挂钩 `X-Client-Operation-Id` 等；默认 `VITE_APP_MONITOR_ENABLED=true`；exclude 监控页
- [x] 4.3 前端管理页：`clientTrack`（index / events / timeline）+ `api/monitor/clientTrack.js`；XSS ignore `/monitor/clientTrack/report`（若现网 XSS 过滤器需要）

## 5. system.api + operlog 头 + 全链路

- [x] 5.1 `system.api` 增加 `OperLogMonitorQuery`（按需 `MenuPathQuery`）及 internal 实现
- [x] 5.2 修复 `OperLogPublishingAspect`：从 `X-Client-Operation-Id` / `X-Client-Id` 写入，禁止有效头时空串写死
- [x] 5.3 monitor.internal.tracechain：`GET /monitor/traceChain/graph`；经 Facade 读 operlog，本模块读 track/slowSql
- [x] 5.4 前端：`api/monitor/traceChain.js` + `views/monitor/traceChain`

## 6. 验证

- [x] 6.1 `mvn -pl quickboot-module-monitor,quickboot-module-system,quickboot-app -am test`（或等价）编译 + Modulith `verify()` 通过
- [ ] 6.2 冒烟慢 SQL：超阈值落库；列表/详情/导出/删除/清空；关采集后无新数据；权限拒绝
- [ ] 6.3 冒烟前端监控：登录后上报；批次/事件/轨迹可查；关埋点后无新上报；未登录 `/report` 拒绝
- [ ] 6.4 冒烟全链路：同 operationId 下 graph 聚合 track + operlog + slowSql；无匹配数据有明确提示
