## 1. 存储与分层摸底



- [x] 1.1 确认链路表与 API 落在 `quickboot-module-monitor`（或记录需新建模块的原因）

- [x] 1.2 排查现有 access / 慢 SQL / requestId 是否已有可映射的关联字段

- [x] 1.3 新增 DDL：`trace_index`、`trace_span`（及可选 `rum_event`），含设计文档所列索引与 TTL 说明



> 摸底结论：落 `quickboot-module-monitor`（`internal/litetrace`）。已有 `TraceIds`（MDC `traceId`）、`ClientOperationIds`（`X-Client-Operation-Id`）、`sys_slow_sql.trace_id` / `sys_oper_log.trace_id` / `sys_client_track`；无独立 `requestId`。与现网 `traceChain` 聚合读并存。DDL：`V24__lite_trace_chain.sql`。



## 2. 投影与 Ingest



- [x] 2.1 实现 `/rum/ingest`：校验 `appId`、限流、解析 `env`、服务端写入 `clientIp`

- [x] 2.2 实现投影服务：RUM 事件 → upsert `trace_index` + insert `fe_*` spans

- [x] 2.3 提供按 `trace_id` 查询 spans、按多键筛选 `trace_index` 的只读 API（供 A′/E）



## 3. 后端埋点



- [x] 3.1 HTTP Filter：读取或生成 `X-Trace-Id`，写入请求上下文/MDC

- [x] 3.2 Access 投影为 `gateway`/`service` span，并更新 `trace_index`

- [x] 3.3 SQL 拦截：有 `trace_id` 时投影 `sql` span（文本截断/指纹）

- [x] 3.4 异常处理：投影 `be_error`，更新 `trace_index.ok` / `error_summary`

- [x] 3.5 验证纯 API / Job 入口可写 `root_source=api|job` 的 index 行



> 3.3 复用 `SlowSqlCapturedEvent`（dev 阈值可 0ms）；3.5 提供 `POST /monitor/liteTrace/index/ensureRoot`。



## 4. Web SDK



- [x] 4.1 实现 SDK：`pv` / `action` / `api` / `error`、队列批量上报、`env.ua`

- [x] 4.2 API 包装注入 `traceId` 头；忽略 ingest 自调用；错误 fingerprint 限流

- [x] 4.3 在目标 Web 应用尽早初始化（配置 `appId`、`ingestUrl`）



## 5. 控制台 A′ / E



- [x] 5.1 `quick-ui` 菜单与路由：链路 Trace（A′）、查询控制台（E）

- [x] 5.2 A′ 页：来源过滤、列表、详情头、Span 瀑布（对齐 `lite-rum-chain-a-unified.html`）

- [x] 5.3 E 页：命令条解析与结果表 + chain dump（对齐 `lite-rum-chain-e.html`）

- [x] 5.4 对接只读 Trace API；无数据/错误态提示



> 菜单已在 V24/V25；组件 `monitor/liteTrace/index`（顶栏已合并原 E 查询台；独立 query 页已下线）。



## 6. 验证



- [ ] 6.1 浏览器失败链路：A′ 可见 fe_api / service / sql / error 同 `traceId`

- [ ] 6.2 无前端纯 API（或模拟）链路：`root_source=api` 可查

- [ ] 6.3 E：`traceId:` / `operationId:` / `uin:` 命中并展开 dump

- [ ] 6.4 错误 `appId` 或关闭 SDK 不影响业务页



> 后端 `mvn -pl quickboot-module-monitor,quickboot-app -am compile` 已通过；6.x 需启动后手工联调。



## 7. 可选 ①b（不阻塞主路径）



- [ ] 7.1 D′ Issue 表与 FE/BE 指纹聚合 + 控制台入口（参考 `lite-rum-chain-d-unified.html`）

