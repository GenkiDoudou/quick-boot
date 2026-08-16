package io.github.genkidoudou.common.monitor.operlog;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.common.api.ClientIds;
import io.github.genkidoudou.common.api.ClientOperationIds;
import io.github.genkidoudou.common.api.TraceIds;
import io.github.genkidoudou.common.oauth.OauthClientVo;
import io.github.genkidoudou.common.security.utils.LoginUserUtils;
import io.github.genkidoudou.common.security.vo.LoginUser;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.ui.Model;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 宽切面：环绕 {@link org.springframework.web.bind.annotation.RestController} 的 public 方法，
 * 在 {@code finally} 中发布 {@link OperLogCapturedEvent}。
 */
@Slf4j
@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class OperLogPublishingAspect {

  private static final int MAX_PARAM_JSON = 3500;
  private static final int MAX_RESULT_JSON = 3500;

  private final ApplicationEventPublisher eventPublisher;
  private final ObjectMapper objectMapper;
  private final OperLogProperties properties;

  private final AntPathMatcher pathMatcher = new AntPathMatcher();

  /** 匹配所有标注 {@code @RestController} 的类。 */
  @Pointcut("@within(org.springframework.web.bind.annotation.RestController)")
  public void restControllers() {
  }

  /**
   * 环绕 RestController 的 public 方法：计时并在 {@code finally} 中发布 {@link OperLogCapturedEvent}。
   *
   * @param joinPoint 连接点
   * @return 原方法返回值
   * @throws Throwable 原方法抛出的异常
   */
  @Around("restControllers() && execution(public * *(..))")
  public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
    if (!properties.isCaptureEnabled()) {
      return joinPoint.proceed();
    }
    if (shouldIgnoreCurrentRequestUri()) {
      return joinPoint.proceed();
    }
    long start = System.currentTimeMillis();
    Throwable failure = null;
    Object result = null;
    try {
      result = joinPoint.proceed();
      return result;
    } catch (Throwable ex) {
      failure = ex;
      throw ex;
    } finally {
      long end = System.currentTimeMillis();
      publishIfNeeded(joinPoint, start, end, result, failure);
    }
  }

  private boolean shouldIgnoreCurrentRequestUri() {
    ServletRequestAttributes attrs = currentRequestAttributes();
    if (attrs == null) {
      return false;
    }
    String uri = attrs.getRequest().getRequestURI();
    if (StrUtil.isBlank(uri)) {
      return false;
    }
    List<String> patterns = properties.getIgnoreUrlPatterns();
    if (patterns == null) {
      return false;
    }
    for (String pattern : patterns) {
      if (StrUtil.isBlank(pattern)) {
        continue;
      }
      if (pathMatcher.match(pattern.trim(), uri)) {
        return true;
      }
    }
    return false;
  }

  private void publishIfNeeded(ProceedingJoinPoint joinPoint, long start, long end, Object result, Throwable failure) {
    MethodSignature ms = (MethodSignature) joinPoint.getSignature();
    Method method = ms.getMethod();
    IgnoreLogger ignoreMethod = method.getAnnotation(IgnoreLogger.class);
    Class<?> targetClass = AopUtils.getTargetClass(joinPoint.getTarget());
    IgnoreLogger ignoreClass = targetClass.getAnnotation(IgnoreLogger.class);
    if (isAllIgnored(ignoreMethod) || isAllIgnored(ignoreClass)) {
      return;
    }
    boolean skipParams = isParamsIgnored(ignoreMethod) || isParamsIgnored(ignoreClass);
    boolean skipResult = isResultIgnored(ignoreMethod) || isResultIgnored(ignoreClass);

    Object[] argsForLog = joinPoint.getArgs();
    if (skipParams) {
      argsForLog = null;
    }
    Object resultForLog = result;
    if (skipResult) {
      resultForLog = null;
    }

    OperLogCapturePayload payload = OperLogCapturePayload.builder()
        .startTimeMs(start)
        .endTimeMs(end)
        .traceId(TraceIds.current())
        .clientOperationId(currentClientOperationId())
        .clientId(currentClientId())
        .signature(ms)
        .args(argsForLog)
        .result(resultForLog)
        .throwable(failure)
        .requestMethod(currentRequestMethod())
        .requestUri(currentRequestUri())
        .requestIp(currentRequestIp())
        .userAgent(currentUserAgent())
        .loginUserId(currentLoginUserId())
        .build();
    try {
      eventPublisher.publishEvent(new OperLogCapturedEvent(payload));
    } catch (Exception e) {
      log.warn("publish OperLogCapturedEvent failed: {}", e.getMessage());
    }
  }

  private static boolean isAllIgnored(IgnoreLogger ann) {
    return ann != null && ann.type() == IgnoreLogger.Type.ALL;
  }

  private static boolean isParamsIgnored(IgnoreLogger ann) {
    return ann != null && ann.type() == IgnoreLogger.Type.PARAMS;
  }

  private static boolean isResultIgnored(IgnoreLogger ann) {
    return ann != null && ann.type() == IgnoreLogger.Type.RESULT;
  }

  private ServletRequestAttributes currentRequestAttributes() {
    try {
      var attrs = RequestContextHolder.getRequestAttributes();
      return attrs instanceof ServletRequestAttributes s ? s : null;
    } catch (IllegalStateException e) {
      return null;
    }
  }

  /**
   * 将方法参数序列化为 JSON 字符串（供落库）；与旧栈行为类似，过滤 Web 基础设施类型。
   *
   * @param ms     方法签名
   * @param args   方法参数
   * @param mapper Jackson ObjectMapper
   * @return 截断后的 JSON 字符串
   */
  public static String serializeParams(MethodSignature ms, Object[] args, ObjectMapper mapper) {
    if (args == null || args.length == 0) {
      return "";
    }
    String[] names = ms.getParameterNames();
    if (names == null || names.length == 0) {
      return safeJson(mapper, args);
    }
    Map<String, Object> map = new LinkedHashMap<>(names.length);
    for (int i = 0; i < names.length && i < args.length; i++) {
      Object v = args[i];
      if (v == null) {
        continue;
      }
      if (v instanceof MultipartFile f) {
        map.put(names[i], f.getOriginalFilename());
      } else if (v instanceof ServletRequest || v instanceof ServletResponse) {
        continue;
      } else if (v instanceof Model || v.getClass().getName().startsWith("org.springframework.")) {
        continue;
      } else {
        map.put(names[i], v);
      }
    }
    String json = safeJson(mapper, map);
    return StrUtil.sub(json, 0, MAX_PARAM_JSON);
  }

  /**
   * 将返回值序列化为 JSON 字符串（供落库）。
   *
   * @param result 方法返回值
   * @param mapper Jackson ObjectMapper
   * @return 截断后的 JSON 字符串
   */
  public static String serializeResult(Object result, ObjectMapper mapper) {
    if (result == null) {
      return "";
    }
    if (result instanceof String s) {
      return StrUtil.sub(s, 0, MAX_RESULT_JSON);
    }
    String json = safeJson(mapper, result);
    return StrUtil.sub(json, 0, MAX_RESULT_JSON);
  }

  private static String safeJson(ObjectMapper mapper, Object value) {
    try {
      return mapper.writeValueAsString(value);
    } catch (JacksonException e) {
      return String.valueOf(value);
    }
  }

  /**
   * 从当前请求解析客户端 IP（不做反向代理链解析，保持简单）。
   *
   * @return 客户端 IP，无请求上下文时返回空串
   */
  public static String currentRequestIp() {
    ServletRequestAttributes attrs = currentRequestAttributesStatic();
    if (attrs == null) {
      return "";
    }
    HttpServletRequest req = attrs.getRequest();
    return req.getRemoteAddr() == null ? "" : req.getRemoteAddr();
  }

  /**
   * 当前请求 URI，无请求上下文时返回空串。
   *
   * @return 请求 URI
   */
  public static String currentRequestUri() {
    ServletRequestAttributes attrs = currentRequestAttributesStatic();
    if (attrs == null) {
      return "";
    }
    String uri = attrs.getRequest().getRequestURI();
    return uri == null ? "" : uri;
  }

  /**
   * 当前 HTTP 方法。
   *
   * @return HTTP 方法名
   */
  public static String currentRequestMethod() {
    ServletRequestAttributes attrs = currentRequestAttributesStatic();
    if (attrs == null) {
      return "";
    }
    String m = attrs.getRequest().getMethod();
    return m == null ? "" : m;
  }

  /**
   * 解析前端一次用户操作 ID：MDC（{@link ClientOperationIds}）→ 头 {@link ClientOperationIds#HEADER_NAME}。
   *
   * @return 合法 operationId；不可得时返回空串（不得在有效头存在时写死空串）
   */
  public static String currentClientOperationId() {
    String fromMdc = ClientOperationIds.current();
    if (fromMdc != null) {
      return fromMdc;
    }
    ServletRequestAttributes attrs = currentRequestAttributesStatic();
    if (attrs == null) {
      return "";
    }
    String normalized = ClientOperationIds.normalizeHeader(
        attrs.getRequest().getHeader(ClientOperationIds.HEADER_NAME));
    return normalized == null ? "" : normalized;
  }

  /**
   * 解析客户端 ID：请求属性 OauthClientVo → LoginUser → MDC（{@link ClientIds}）→ 头 {@link ClientIds#HEADER_NAME}。
   *
   * @return 客户端 ID；均不可得时返回空串
   */
  public static String currentClientId() {
    ServletRequestAttributes attrs = currentRequestAttributesStatic();
    if (attrs != null) {
      Object oauth = attrs.getRequest().getAttribute(OauthClientVo.ATTR_KEY);
      if (oauth instanceof OauthClientVo vo && StrUtil.isNotBlank(vo.getClientId())) {
        return vo.getClientId().trim();
      }
    }
    try {
      LoginUser loginUser = LoginUserUtils.getLoginUser();
      if (loginUser != null && StrUtil.isNotBlank(loginUser.getClientId())) {
        return loginUser.getClientId().trim();
      }
    } catch (Exception ignored) {
      // ignore
    }
    String fromMdc = ClientIds.current();
    if (fromMdc != null) {
      return fromMdc;
    }
    if (attrs == null) {
      return "";
    }
    String normalized = ClientIds.normalizeHeader(
        attrs.getRequest().getHeader(ClientIds.HEADER_NAME));
    return normalized == null ? "" : normalized;
  }

  /**
   * 当前请求 User-Agent。
   *
   * @return User-Agent；无请求时返回空串
   */
  public static String currentUserAgent() {
    ServletRequestAttributes attrs = currentRequestAttributesStatic();
    if (attrs == null) {
      return "";
    }
    String ua = attrs.getRequest().getHeader("User-Agent");
    return ua == null ? "" : StrUtil.sub(ua.trim(), 0, 512);
  }

  private static ServletRequestAttributes currentRequestAttributesStatic() {
    try {
      var attrs = RequestContextHolder.getRequestAttributes();
      return attrs instanceof ServletRequestAttributes s ? s : null;
    } catch (IllegalStateException e) {
      return null;
    }
  }

  /**
   * 在 Web 请求线程读取当前登录用户 ID，供异步落库使用。
   *
   * @return 登录用户 ID；未登录为 {@code null}
   */
  private static Long currentLoginUserId() {
    try {
      if (StpUtil.isLogin()) {
        return StpUtil.getLoginIdAsLong();
      }
    } catch (Exception ignored) {
      // 非 Web 上下文或未登录
    }
    return null;
  }
}
