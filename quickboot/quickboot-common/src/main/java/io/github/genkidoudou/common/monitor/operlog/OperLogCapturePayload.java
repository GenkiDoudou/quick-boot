package io.github.genkidoudou.common.monitor.operlog;

import lombok.Builder;
import lombok.Value;
import org.aspectj.lang.Signature;

import java.io.Serializable;

/**
 * 操作日志采集事件载荷；在切面线程组装，含 {@link #traceId} 以便与异步化演进解耦。
 */
@Value
@Builder
public class OperLogCapturePayload implements Serializable {

  long startTimeMs;

  long endTimeMs;

  /** 与 {@link io.github.genkidoudou.common.api.TraceIds} / MDC 同源，可空。 */
  String traceId;

  /** 客户端操作 ID；本项目暂无统一上下文，可为空串。 */
  String clientOperationId;

  /** 客户端 ID；优先请求属性 / LoginUser，其次 {@code X-Client-Id}。 */
  String clientId;

  Signature signature;

  Object[] args;

  Object result;

  Throwable throwable;

  /** 请求线程快照：HTTP 方法，异步落库时不再读 RequestContext。 */
  String requestMethod;

  /** 请求线程快照：URI。 */
  String requestUri;

  /** 请求线程快照：客户端 IP。 */
  String requestIp;

  /** 请求线程快照：User-Agent。 */
  String userAgent;

  /** 请求线程快照：登录用户 ID；未登录为 null。 */
  Long loginUserId;
}
