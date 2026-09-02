package io.github.genkidoudou.common.idempotency;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记需在短窗口内拒绝重复提交的 Controller / Service 方法。
 * <p>
 * <b>推荐（管理端）</b>：{@link #key()} SpEL 从参数 / 请求体生成防重键，前端零改动。示例：
 * <pre>{@code
 * @Idempotent(ttlSeconds = 10, key = "#userId + ':' + #body.orderId")
 * @PostMapping("pay")
 * public R<Void> pay(@RequestBody OrderVo body) { ... }
 * }</pre>
 * SpEL 上下文：{@code #参数名}（方法形参）、{@code #body}（{@code @RequestBody}）、
 * {@code #userId}（当前登录 ID）、{@code #request}（{@link jakarta.servlet.http.HttpServletRequest}）。
 * <p>
 * <b>可选（开放 API）</b>：未配置 {@link #key()} 时，可改由 Header {@link IdempotencyKeys#HEADER_NAME} 携带幂等键。
 * <p>
 * 业务方法抛异常时会释放占用；成功则保留占用直至 TTL 过期。
 *
 * @see IdempotencyAutoConfiguration
 * @see IdempotencyStore
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {

  /**
   * SpEL 表达式，动态生成客户端幂等键片段；非空时优先于 Header。
   * <p>
   * 示例：{@code "#orderId"}、{@code "#body.orderId"}、{@code "#userId + ':' + #body.orderId"}。
   */
  String key() default "";

  /**
   * 幂等键占用 TTL（秒）；{@code <=0} 时使用 {@link IdempotencyProperties#getTtlSeconds()}。
   */
  int ttlSeconds() default -1;

  /**
   * 仅 Header 模式：是否必须携带幂等键；{@code true} 且未配置 {@link #key()} 且无 Header 时拒绝请求。
   */
  boolean required() default false;

  /**
   * 服务端键隔离维度：默认按登录用户隔离。
   */
  IdempotencyScope scope() default IdempotencyScope.USER;

  /**
   * 是否将 HTTP 方法 + URI 纳入服务端键，避免同一 SpEL 结果误用于不同接口。
   */
  boolean includeUri() default true;

  /**
   * 重复提交时的提示文案；空则使用默认「重复请求，请勿重复提交」。
   */
  String message() default "";
}
