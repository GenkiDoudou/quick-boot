## 1. 文档与需求对齐

- [x] 1.1 更新 `原始需求/后端/链路追踪模块.md`：将读取 traceId 的规范表述统一为 `TraceIds.current()`（类 `io.github.genkidoudou.common.api.TraceIds`），删除或替换对 `TraceUtil` / `common/trace` 包路径的硬性要求；补充说明 span 由 Spring Boot + Micrometer Tracing 创建或延续，上游（浏览器/网关）可通过 W3C `traceparent` 等标准头传递上下文。
- [x] 1.2 对照 `openspec/changes/align-tracing-conventions/specs/common-tracing/spec.md` 通读更新后的原始需求，确认验收标准、配置项示例与边界描述一致。

## 2. 代码注释（非行为变更）

- [x] 2.1 在 `TraceIds` 的 JavaDoc 中简要说明：依赖 Micrometer Tracing 对 MDC 的填充；本类不负责创建 span；异步线程需另行保证 MDC/上下文传播（点明即可）。
- [x] 2.2 在 `R` 的类级 JavaDoc 中明确：`traceId` 在 `build` 时取自 `TraceIds.current()`，与日志 `%X{traceId}` 同源；未采样或无上下文时可能为 `null`。

## 3. 验证

- [ ] 3.1 在 `quickboot` 目录执行 `mvn -pl quickboot-common test`，确认测试通过且无编译问题。
