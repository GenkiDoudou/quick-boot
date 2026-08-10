package io.github.genkidoudou.common.monitor.operlog;

import cn.hutool.core.util.StrUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.event.EventListener;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.Method;

/**
 * 参照旧栈 {@code WebPrintLoggerEventListener}：监听 {@link OperLogCapturedEvent}，将请求摘要打印到控制台。
 */
@Slf4j
@RequiredArgsConstructor
public class OperLogConsolePrintListener {

  private static final int MAX_RESULT_PRINT_LEN = 2000;

  private final ObjectMapper objectMapper;

  /**
   * 打印操作日志摘要到控制台。
   *
   * @param event 采集完成事件
   */
  @EventListener
  public void onOperLogCaptured(OperLogCapturedEvent event) {
    if (event == null || event.getPayload() == null) {
      return;
    }
    OperLogCapturePayload payload = event.getPayload();
    String line = format(payload);
    if (line == null) {
      return;
    }
    if (payload.getThrowable() != null) {
      log.error(line);
    } else {
      log.info(line);
    }
  }

  private String format(OperLogCapturePayload payload) {
    MethodSignature ms = payload.getSignature() instanceof MethodSignature signature ? signature : null;
    Method method = ms != null ? ms.getMethod() : null;
    Class<?> declaring = method != null ? method.getDeclaringClass() : null;

    String methodName = method != null && declaring != null
        ? declaring.getName() + "." + method.getName()
        : null;

    long cost = payload.getEndTimeMs() - payload.getStartTimeMs();
    String params = ms != null
        ? OperLogPublishingAspect.serializeParams(ms, payload.getArgs(), objectMapper)
        : "";
    params = OperLogSensitiveMasker.mask(params);
    String result = OperLogPublishingAspect.serializeResult(payload.getResult(), objectMapper);
    result = OperLogSensitiveMasker.mask(result);
    if (StrUtil.isNotBlank(result) && result.length() > MAX_RESULT_PRINT_LEN) {
      result = result.substring(0, MAX_RESULT_PRINT_LEN) + "...";
    }

    StringBuilder sb = new StringBuilder();
    sb.append("\n[-----------------------------------").append('\n');
    if (methodName != null) {
      sb.append("methodName: ").append(methodName).append('\n');
    }
    sb.append("sourceIp: ").append(resolveClientIp()).append('\n');
    String description = resolveDescription(method, declaring);
    if (StrUtil.isNotBlank(description)) {
      sb.append("description: ").append(description).append('\n');
    }
    sb.append("uri: ")
        .append(OperLogPublishingAspect.currentRequestMethod())
        .append(' ')
        .append(OperLogPublishingAspect.currentRequestUri())
        .append('\n');
    if (StrUtil.isNotBlank(params)) {
      sb.append("requestParams: ").append(params).append('\n');
    }
    sb.append("timeConsuming: ").append(cost).append("ms\n");
    if (StrUtil.isNotBlank(payload.getTraceId())) {
      sb.append("traceId: ").append(payload.getTraceId()).append('\n');
    }
    if (StrUtil.isNotBlank(result)) {
      sb.append("result: ").append(result).append('\n');
    }
    if (payload.getThrowable() != null) {
      sb.append("errorMsg: ").append(payload.getThrowable().getLocalizedMessage()).append('\n');
    }
    sb.append("------------------------------]");
    return sb.toString();
  }

  private static String resolveDescription(Method method, Class<?> declaring) {
    if (method == null || declaring == null) {
      return null;
    }
    StringBuilder description = new StringBuilder();
    Tag tag = declaring.getAnnotation(Tag.class);
    if (tag != null && StringUtils.hasText(tag.name())) {
      description.append(tag.name().trim()).append('-');
    }
    Operation operation = method.getAnnotation(Operation.class);
    if (operation != null && StringUtils.hasText(operation.summary())) {
      description.append(operation.summary().trim());
    }
    if (!description.isEmpty() && description.charAt(description.length() - 1) == '-') {
      description.deleteCharAt(description.length() - 1);
    }
    return description.isEmpty() ? null : description.toString();
  }

  /**
   * 解析客户端 IP（优先代理头，与旧栈一致）。
   *
   * @return 客户端 IP
   */
  private static String resolveClientIp() {
    ServletRequestAttributes attrs = currentRequestAttributes();
    if (attrs == null) {
      return "";
    }
    HttpServletRequest request = attrs.getRequest();
    String ip = request.getHeader("X-Forwarded-For");
    if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("Proxy-Client-IP");
    }
    if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("WL-Proxy-Client-IP");
    }
    if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("HTTP_CLIENT_IP");
    }
    if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("HTTP_X_FORWARDED_FOR");
    }
    if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getRemoteAddr();
    }
    if (ip != null && ip.contains(",")) {
      ip = ip.split(",")[0].trim();
    }
    return ip == null ? "" : ip;
  }

  private static ServletRequestAttributes currentRequestAttributes() {
    try {
      var attrs = RequestContextHolder.getRequestAttributes();
      return attrs instanceof ServletRequestAttributes s ? s : null;
    } catch (IllegalStateException e) {
      return null;
    }
  }
}
