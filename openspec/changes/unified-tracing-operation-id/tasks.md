## 1. 数据库迁移

- [x] 1.1 新增 Flyway `V41__oper_log_client_operation_id.sql`：`sys_oper_log` 增加 `client_operation_id VARCHAR(64) NULL` 及索引；列注释说明来自 `X-Client-Operation-Id`
- [x] 1.2 新增 Flyway `V42__client_track_operation_id.sql`：`sys_client_track` 增加 `operation_id VARCHAR(64) NOT NULL DEFAULT ''` 及索引；更新 `trace_id` 列注释为过渡/可选 serverTraceId

## 2. 后端 clientOperationId 接入

- [x] 2.1 在 `quickboot-common` 新增 MDC 常量（如 `ClientOperationIds.MDC_KEY = "clientOperationId"`）与 `ClientOperationFilter`：读取 `X-Client-Operation-Id`（trim、≤64、非法丢弃），写入/清除 MDC
- [x] 2.2 注册 Filter Bean，顺序在 tracing 之后；CORS Allow-Headers 增加 `X-Client-Operation-Id`（及预留 `traceparent`、`x-trace-id`）
- [x] 2.3 单元测试：合法 Header 进 MDC；超长丢弃；请求结束 MDC 清除

## 3. 操作日志 client_operation_id 落库

- [x] 3.1 `OperLogCapturePayload` / 事件 DTO 增加 `clientOperationId`；宽切面 `finally` 从 MDC 读取写入载荷
- [x] 3.2 `SysOperLog` 实体、Mapper、VO/BO 增加 `clientOperationId` 字段映射
- [x] 3.3 `OperLogPersistListener` / Assembler 落库 `client_operation_id`
- [x] 3.4 `GET /monitor/operlog/list` 查询 BO 与 Service 支持 `clientOperationId` 筛选

## 4. 前端监控后端适配

- [x] 4.1 `ClientTrackReportBo`、`SysClientTrack`、VO/QueryBo 增加 `operationId`；Service 写入 `operation_id`
- [x] 4.2 `events_json` 解析/展示支持 per-event `operationId`、`serverTraceId`（API 事件）

## 5. 前端 operationContext 与 axios

- [x] 5.1 新增 `quick-ui/src/monitor/operationContext.js`（`beginOperation` / `getOperationId` / `endOperation` / `runInOperation`）
- [x] 5.2 `utils/request.js` 拦截器：有活跃 operationId 时注入 `X-Client-Operation-Id`；无则不注入
- [x] 5.3 移除 `createUserMonitor.js` 中 `front_trace_id` / `sessionStorage` 写入逻辑

## 6. 前端 monitor 字段迁移

- [x] 6.1 `createUserMonitor.js`：`data-track` click 时 `beginOperation`；click/route 事件携带 `operationId`
- [x] 6.2 `bindRequestMonitor.js`：api_slow/api_error 携带 `operationId`（若有）与 `serverTraceId`（= `response.data.traceId`）
- [x] 6.3 `report.js`：批次 payload 增加 `operationId`；上报字段与后端 DTO 对齐

## 7. 管理端页面

- [x] 7.1 `views/monitor/clientTrack/index.vue`：列表/详情展示 `operationId`；API 事件行展示 `serverTraceId`（可复制）
- [x] 7.2 操作日志页：搜索条件与列表列增加 `client_operation_id`；对照 `DESIGN.md` 与 `views/system/config/index.vue` 列表风格

## 8. 验证

- [ ] 8.1 手工验收（spec §11.1）：一次 click 触发 ≥2 API → 同一 operationId、不同 serverTraceId/trace_id、oper_log ≥2 行 client_operation_id 相同
- [ ] 8.2 操作日志页按 `client_operation_id` 可一次查出多行；按任一 `trace_id` 唯一定位单行
- [x] 8.3 `pnpm build:prod`（quick-ui）与 `mvn -pl quickboot-web -am test`（或模块级等价）通过

## 9. 可选（策略 B，默认跳过）

- [ ] 9.1 新增 `requestTrace.js`：`nextRequestTraceHeaders()` 每请求生成新 traceparent/x-trace-id；axios 集成（与 operationId 并存）
