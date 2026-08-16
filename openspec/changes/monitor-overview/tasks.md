## 1. system.api 聚合 Facade

- [x] 1.1 扩展用户只读能力：活跃用户计数（`del_flag='0'`），供总览「总用户」
- [x] 1.2 新增 `LoginInfoMonitorQuery`（或等价）：时间窗内登录用户去重、成功/失败次数、按小时/按日分桶趋势
- [x] 1.3 扩展 `OperLogMonitorQuery`：时间窗内请求总数、错误数（`status <> 0`）、按小时/按日分桶趋势
- [x] 1.4 实现上述 Facade 的 internal 适配（Mapper/Service），不向外暴露实体

## 2. quartz.api 任务日志聚合

- [x] 2.1 在 `module-quartz` 的 `api` 新增 `JobLogMonitorQuery`：时间窗成功/失败计数、失败率、最近失败 TopN
- [x] 2.2 实现 Facade；确认 Modulith `quartz :: api` 可被 monitor 依赖

## 3. monitor overview 后端

- [x] 3.1 更新 `monitor` `package-info`：`allowedDependencies` 增加 `quartz :: api`
- [x] 3.2 新增 `internal.overview`：时间窗解析（today/yesterday/week/last7d/month → `[start,end)` + 分桶粒度）
- [x] 3.3 本模块聚合：`sys_client_track`（page_visits/sessions/active_users/热门 Top5）、`sys_slow_sql`（count/avg/max/来源分布或 Top5）
- [x] 3.4 实现 `OverviewService`：组合 system/quartz Facade + 本模块聚合，组装 summary / trends DTO
- [x] 3.5 实现 `GET /monitor/overview/summary` 与 `GET /monitor/overview/trends`，权限字 `monitor:overview:query`

## 4. Flyway 菜单与权限

- [x] 4.1 核对现网 `sys_menu` 占用 ID，编写下一可用 `V*`：插入「态势总览」菜单、`monitor:overview:query` 按钮、管理员角色授权
- [ ] 4.2 本地迁移验证菜单与权限字生效

## 5. quick-ui 态势总览页

- [x] 5.1 新增 `api/monitor/overview.js`（summary / trends）
- [x] 5.2 新增 `views/monitor/overview` 页面：时间窗切换、五组 KPI、趋势图（ECharts）、下钻链接到既有监控子页
- [x] 5.3 路由/动态菜单 component 路径与权限指令对齐后端菜单

## 6. 校验与验收

- [x] 6.1 运行 Modulith `verify()`（或项目既有校验命令）确认跨模块边界
- [x] 6.2 对照 `docs/demo/system-monitor-dashboard-metrics.sql` 抽查至少两个时间窗（今日 + 近7天）KPI/分桶口径
- [ ] 6.3 冒烟：无权限用户拒绝；有权限用户打开总览可见非 mock 数据并可下钻
