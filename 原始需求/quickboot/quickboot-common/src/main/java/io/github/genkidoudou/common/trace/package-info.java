/**
 * 链路追踪模块
 * 
 * 提供分布式链路追踪能力，支持在日志中自动携带 TraceId 和 SpanId
 * 
 * 主要功能：
 * 1. 自动生成和传递 TraceId、SpanId
 * 2. 在日志中自动注入链路追踪信息
 * 3. 支持跨服务的链路追踪传递
 * 4. 支持导出到 OTLP（OpenTelemetry Protocol）
 * 5. 提供工具类获取当前链路信息
 * 
 * 技术实现：
 * <ul>
 *   <li>基于 Spring Boot 3.x 内置的 Micrometer Tracing 自动配置</li>
 *   <li>使用 OpenTelemetry 作为追踪桥接</li>
 *   <li>通过 MDC（Mapped Diagnostic Context）将链路信息注入日志</li>
 *   <li>支持 OTLP 协议导出到 Jaeger、Zipkin 等追踪系统</li>
 * </ul>
 * 
 * Maven 依赖：
 * <pre>
 * &lt;!-- Micrometer Tracing + OpenTelemetry --&gt;
 * &lt;dependency&gt;
 *     &lt;groupId&gt;io.micrometer&lt;/groupId&gt;
 *     &lt;artifactId&gt;micrometer-tracing-bridge-otel&lt;/artifactId&gt;
 * &lt;/dependency&gt;
 * 
 * &lt;!-- OTLP exporter（可选，用于导出到追踪系统）--&gt;
 * &lt;dependency&gt;
 *     &lt;groupId&gt;io.opentelemetry&lt;/groupId&gt;
 *     &lt;artifactId&gt;opentelemetry-exporter-otlp&lt;/artifactId&gt;
 * &lt;/dependency&gt;
 * 
 * &lt;!-- MDC 桥接（关键，用于日志集成）--&gt;
 * &lt;dependency&gt;
 *     &lt;groupId&gt;io.micrometer&lt;/groupId&gt;
 *     &lt;artifactId&gt;micrometer-observation&lt;/artifactId&gt;
 * &lt;/dependency&gt;
 * </pre>
 * 
 * 配置示例（application.yml）：
 * <pre>
 * management:
 *   # Actuator 端点配置
 *   endpoint:
 *     health:
 *       show-details: when_authorized
 *   # OTLP 导出配置（可选）
 *   otlp:
 *     tracing:
 *       endpoint: "http://localhost:4318/v1/traces"
 *   # 链路追踪配置
 *   tracing:
 *     sampling:
 *       probability: 1.0   # 100% 采样，生产环境建议调低（如 0.1）
 * 
 * # 日志配置
 * logging:
 *   pattern:
 *     level: "%5p [${spring.application.name:},%X{traceId:-},%X{spanId:-}]"
 * </pre>
 * 
 * 日志配置示例（logback-spring.xml）：
 * <pre>
 * &lt;configuration&gt;
 *     &lt;appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender"&gt;
 *         &lt;encoder&gt;
 *             &lt;pattern&gt;%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] [%X{traceId:-},%X{spanId:-}] %-5level %logger{36} - %msg%n&lt;/pattern&gt;
 *         &lt;/encoder&gt;
 *     &lt;/appender&gt;
 * &lt;/configuration&gt;
 * </pre>
 * 
 * 使用示例：
 * <pre>
 * // 1. 自动注入（推荐）- 在日志中自动包含 traceId 和 spanId
 * &#64;RestController
 * public class UserController {
 *     private static final Logger log = LoggerFactory.getLogger(UserController.class);
 *     
 *     &#64;GetMapping("/user/{id}")
 *     public User getUser(&#64;PathVariable Long id) {
 *         // 日志会自动包含 [traceId,spanId]
 *         log.info("Getting user: {}", id);
 *         return userService.getUser(id);
 *     }
 * }
 * 
 * // 2. 手动获取链路信息
 * String traceId = TraceUtil.getTraceId();
 * String spanId = TraceUtil.getSpanId();
 * 
 * // 3. 创建新的 Span（用于细粒度追踪）
 * &#64;Service
 * public class UserService {
 *     &#64;Autowired
 *     private Tracer tracer;
 *     
 *     public User getUser(Long id) {
 *         Span span = tracer.nextSpan().name("getUserFromDb").start();
 *         try (Tracer.SpanInScope ws = tracer.withSpan(span)) {
 *             // 业务逻辑
 *             return userRepository.findById(id);
 *         } finally {
 *             span.end();
 *         }
 *     }
 * }
 * 
 * // 4. 添加自定义标签
 * TraceUtil.addTag("userId", "12345");
 * TraceUtil.addTag("operation", "query");
 * 
 * // 5. 记录错误
 * try {
 *     // 业务逻辑
 * } catch (Exception e) {
 *     TraceUtil.error(e);
 *     throw e;
 * }
 * </pre>
 * 
 * 日志输出示例：
 * <pre>
 * 2026-03-02 10:30:45.123 [http-nio-8080-exec-1] [a1b2c3d4e5f6g7h8,1234567890abcdef] INFO  c.e.UserController - Getting user: 123
 * 2026-03-02 10:30:45.156 [http-nio-8080-exec-1] [a1b2c3d4e5f6g7h8,fedcba0987654321] INFO  c.e.UserService - Querying database for user: 123
 * </pre>
 * 
 * 与追踪系统集成：
 * <ul>
 *   <li><b>Jaeger</b>: 启动 Jaeger，配置 endpoint 为 http://localhost:4318/v1/traces</li>
 *   <li><b>Zipkin</b>: 使用 zipkin-exporter 依赖，配置 endpoint 为 http://localhost:9411/api/v2/spans</li>
 *   <li><b>自定义</b>: 实现 SpanExporter 接口，导出到自定义系统</li>
 * </ul>
 * 
 * 注意事项：
 * <ul>
 *   <li>Spring Boot 3.x 已内置自动配置，无需手动配置 Bean</li>
 *   <li>TraceId 在整个请求链路中保持不变</li>
 *   <li>SpanId 在每个服务调用中会变化</li>
 *   <li>异步调用需要手动传递 Span 上下文</li>
 *   <li>生产环境建议调整采样率（如 0.1）以降低性能开销</li>
 *   <li>如果不需要导出到追踪系统，可以不配置 otlp.tracing.endpoint</li>
 * </ul>
 * 
 * 性能建议：
 * <ul>
 *   <li>开发环境：sampling.probability = 1.0（100% 采样）</li>
 *   <li>测试环境：sampling.probability = 0.5（50% 采样）</li>
 *   <li>生产环境：sampling.probability = 0.1（10% 采样）</li>
 * </ul>
 *
 * @author QuickBoot
 * @since 2026/03/02
 */
package io.github.genkidoudou.common.trace;
