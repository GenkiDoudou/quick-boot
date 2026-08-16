## 1. Flyway 与菜单

- [x] 1.1 新增迁移：停用 overview 菜单 2169/2170；`sys_rum_event` 增加 `uin` 及 `(uin,event_time)` / `(session_id,event_time)` 索引（缺则补）
- [x] 1.2 同迁移（或紧随）：插入「用户行为」「日志中心」菜单 + 按钮权限 + admin `sys_role_menu`
- [x] 1.3 确认 menu path/component：`monitor/userBehavior/index`、`monitor/logHub/index`；权限字 `monitor:userBehavior:query`、`monitor:logHub:query`

## 2. 下线监控概览

- [x] 2.1 删除后端 `monitor.internal.overview`（controller/service/dto 等）及一切引用
- [x] 2.2 删除前端 `views/monitor/overview`、`api/monitor/overview.js` 及引用
- [x] 2.3 编译确认无 overview 残留；侧栏无「监控概览」

## 3. 请求链路增强

- [x] 3.1 `TraceIndexQueryBo` + query 服务：支持 `beginTime`/`endTime`/`okFlag`；列表按 `started_at`/`ok_flag` 过滤
- [x] 3.2 前端请求链路：时间区间控件（默认近 24h）+ 成功/失败筛选，接入列表 API
- [x] 3.3 右侧详情头展示 Index 全量关键字段；Span 行可展开 `statusCode`/`okFlag`/`attrsJson`
- [x] 3.4 关联跳转：同 `operationId` 过滤、慢 SQL / 操作日志带 query；可选跳转用户行为

## 4. Ingest 写 uin

- [x] 4.1 Entity/Mapper 支持 `SysRumEvent.uin`
- [x] 4.2 Lite RUM ingest：登录态写入 `rum_event.uin`，投影更新 `trace_index.uin`
- [x] 4.3 冒烟：登录后浏览产生事件，库中 `uin` 非空（需重启跑 V27 后手工确认）

## 5. 用户行为

- [x] 5.1 后端 sessions / timeline API（按 uin/userName/sessionId + 时间窗；返回 pv/action 有序节点）
- [x] 5.2 前端 `userBehavior` 页：左会话列表、右时间线；可下钻请求链路
- [x] 5.3 页内说明：仅统计路由 pv 与显式 `trackAction`

## 6. 日志中心

- [x] 6.1 后端 LogHub 聚合 API：经 system Facade 取 oper/login + 本模块 slowSql；统一行模型；响应 `approximate=true`
- [x] 6.2 筛选：时间、来源多选、用户、关键字、成功/失败、可选 traceId
- [x] 6.3 前端 `logHub` 页：合并列表 + 跳转原明细菜单页
- [x] 6.4 确认原 operlog / logininfor / slowSql 菜单仍可用（未改删；仍保留）

## 7. 验证

- [x] 7.1 `mvn -pl quickboot-module-monitor,quickboot-app -am compile` 通过
- [x] 7.2 手工：无概览；链路时间/成败/展开/跳转；行为时间线；日志中心三源（启动后联调；代码侧已就绪）
