package io.github.genkidoudou.system.internal.support;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.common.monitor.operlog.OperLogBusinessType;
import io.github.genkidoudou.common.monitor.operlog.OperLogCapturePayload;
import io.github.genkidoudou.common.monitor.operlog.OperLogCapturedEvent;
import io.github.genkidoudou.common.monitor.operlog.OperLogMeta;
import io.github.genkidoudou.common.monitor.operlog.OperLogPublishingAspect;
import io.github.genkidoudou.common.monitor.operlog.OperLogSensitiveMasker;
import io.github.genkidoudou.system.internal.entity.SysDept;
import io.github.genkidoudou.system.internal.entity.SysOperLog;
import io.github.genkidoudou.system.internal.entity.SysUser;
import io.github.genkidoudou.system.internal.mapper.SysDeptMapper;
import io.github.genkidoudou.system.internal.mapper.SysOperLogMapper;
import io.github.genkidoudou.system.internal.mapper.SysUserMapper;
import io.github.genkidoudou.system.internal.vo.LoginRequestVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 操作日志落库入口：合并元数据解析、实体组装与持久化，供同步/异步监听器调用。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OperLogRecorder {

  private static final int MAX_ERROR_MSG = 3800;

  private final SysOperLogMapper operLogMapper;
  private final ObjectMapper objectMapper;
  private final SysUserMapper userMapper;
  private final SysDeptMapper deptMapper;

  /**
   * 将采集事件写入 {@code sys_oper_log}；失败仅记日志，不向上抛出。
   *
   * @param event 切面发布的采集事件
   */
  public void record(OperLogCapturedEvent event) {
    if (event == null || event.getPayload() == null) {
      return;
    }
    try {
      SysOperLog row = assemble(event.getPayload());
      operLogMapper.insert(row);
    } catch (Exception ex) {
      log.error("persist oper log failed", ex);
    }
  }

  /**
   * 将采集载荷组装为待插入实体（含脱敏与状态映射）。
   */
  private SysOperLog assemble(OperLogCapturePayload payload) {
    MethodSignature ms = (MethodSignature) payload.getSignature();
    Method method = ms.getMethod();
    Class<?> declaring = ms.getDeclaringType();
    ResolvedOperLogMeta meta = resolveMeta(
      method, declaring, payload.getRequestMethod(), payload.getRequestUri());

    String operParam = "";
    if (payload.getArgs() != null) {
      operParam = OperLogSensitiveMasker.mask(
        OperLogPublishingAspect.serializeParams(ms, payload.getArgs(), objectMapper));
    }
    String jsonResult;
    String errorMsg;
    int status;
    if (payload.getThrowable() != null) {
      status = 1;
      Throwable root = ExceptionUtil.getRootCause(payload.getThrowable());
      String msg = root != null && root.getMessage() != null
        ? root.getMessage()
        : ExceptionUtil.getRootCauseMessage(payload.getThrowable());
      errorMsg = StrUtil.sub(msg, 0, MAX_ERROR_MSG);
      jsonResult = "";
    } else {
      status = 0;
      errorMsg = "";
      jsonResult = OperLogSensitiveMasker.mask(
        OperLogPublishingAspect.serializeResult(payload.getResult(), objectMapper));
    }

    SysOperLog row = new SysOperLog();
    row.setTitle(meta.title());
    row.setBusinessType(meta.businessType());
    row.setMethod(declaring.getSimpleName() + "." + method.getName());
    row.setRequestMethod(StrUtil.blankToDefault(payload.getRequestMethod(), ""));
    row.setOperUrl(StrUtil.blankToDefault(payload.getRequestUri(), ""));
    row.setOperIp(StrUtil.blankToDefault(payload.getRequestIp(), ""));
    row.setOperLocation("");
    row.setOperParam(operParam);
    row.setJsonResult(jsonResult);
    row.setStatus(status);
    row.setErrorMsg(errorMsg);
    row.setOperTime(LocalDateTime.now());
    row.setCostTime(Math.max(0, payload.getEndTimeMs() - payload.getStartTimeMs()));
    row.setTraceId(payload.getTraceId());
    row.setClientOperationId(payload.getClientOperationId());
    row.setClientId(payload.getClientId());
    row.setUserAgent(StrUtil.blankToDefault(payload.getUserAgent(), ""));

    Long loginUserId = payload.getLoginUserId();
    if (loginUserId != null && loginUserId > 0) {
      row.setOperatorType(meta.operatorType() > 0 ? meta.operatorType() : 1);
      SysUser u = userMapper.selectById(loginUserId);
      if (u != null) {
        row.setOperName(StrUtil.blankToDefault(u.getUserName(), ""));
        row.setDeptName(resolveDeptName(u.getDeptId()));
      } else {
        row.setOperName("");
        row.setDeptName("");
      }
    } else if (isLoginRequest(payload.getRequestUri())) {
      row.setOperatorType(meta.operatorType());
      row.setOperName(resolveLoginAttemptUserName(payload));
      row.setDeptName("");
    } else {
      row.setOperatorType(0);
      row.setOperName("");
      row.setDeptName("");
    }
    return row;
  }

  private static ResolvedOperLogMeta resolveMeta(Method method, Class<?> declaringType,
                                                 String requestMethod, String requestUri) {
    OperLogMeta meta = method.getAnnotation(OperLogMeta.class);
    if (meta != null) {
      String title = StrUtil.blankToDefault(meta.title(), defaultTitle(method, declaringType));
      int businessType = meta.businessType() != OperLogBusinessType.OTHER
        ? meta.businessType()
        : inferBusinessType(method, requestMethod, requestUri);
      return new ResolvedOperLogMeta(title, businessType, meta.operatorType());
    }
    Tag tag = declaringType.getAnnotation(Tag.class);
    Operation op = method.getAnnotation(Operation.class);
    StringBuilder sb = new StringBuilder();
    if (tag != null && StrUtil.isNotBlank(tag.name())) {
      sb.append(tag.name().trim());
    }
    if (op != null && StrUtil.isNotBlank(op.summary())) {
      if (!sb.isEmpty()) {
        sb.append('-');
      }
      sb.append(op.summary().trim());
    }
    String title = sb.isEmpty() ? defaultTitle(method, declaringType) : sb.toString();
    int businessType = inferBusinessType(method, requestMethod, requestUri);
    return new ResolvedOperLogMeta(title, businessType, 1);
  }

  private static int inferBusinessType(Method method, String httpMethod, String requestUri) {
    if (!"POST".equalsIgnoreCase(httpMethod)) {
      return OperLogBusinessType.OTHER;
    }
    String uri = requestUri == null ? "" : requestUri.toLowerCase();
    String name = method.getName().toLowerCase();
    if (matchesMutation(name, uri, "export")) {
      return OperLogBusinessType.EXPORT;
    }
    if (matchesMutation(name, uri, "import")) {
      return OperLogBusinessType.IMPORT;
    }
    if (matchesMutation(name, uri, "remove", "delete", "clean")) {
      return OperLogBusinessType.DELETE;
    }
    if (matchesMutation(name, uri, "create", "add")) {
      return OperLogBusinessType.INSERT;
    }
    if (matchesMutation(name, uri, "update", "edit", "change", "reset", "grant", "auth", "save", "run", "sync")) {
      return OperLogBusinessType.UPDATE;
    }
    return OperLogBusinessType.OTHER;
  }

  private static boolean matchesMutation(String methodName, String uri, String... keywords) {
    for (String keyword : keywords) {
      if (methodName.contains(keyword) || uri.contains("/" + keyword)) {
        return true;
      }
    }
    return false;
  }

  private static String defaultTitle(Method method, Class<?> declaringType) {
    return declaringType.getSimpleName() + "." + method.getName();
  }

  private static boolean isLoginRequest(String requestUri) {
    if (StrUtil.isBlank(requestUri)) {
      return false;
    }
    String uri = requestUri.trim();
    return uri.endsWith("/login") && !uri.endsWith("/login/captcha-config");
  }

  private String resolveLoginAttemptUserName(OperLogCapturePayload payload) {
    MethodSignature ms = (MethodSignature) payload.getSignature();
    String[] names = ms.getParameterNames();
    Object[] args = payload.getArgs();
    if (names == null || args == null) {
      return "";
    }
    for (int i = 0; i < names.length && i < args.length; i++) {
      if ("username".equals(names[i]) && args[i] instanceof String username) {
        return StrUtil.blankToDefault(username, "");
      }
      if (args[i] instanceof LoginRequestVo req) {
        return StrUtil.blankToDefault(req.getUsername(), "");
      }
    }
    return "";
  }

  private String resolveDeptName(Long deptId) {
    if (deptId == null) {
      return "";
    }
    SysDept d = deptMapper.selectById(deptId);
    return d == null || d.getDeptName() == null ? "" : d.getDeptName();
  }

  /**
   * 元数据解析结果：标题、业务类型、操作者类别。
   */
  private record ResolvedOperLogMeta(String title, int businessType, int operatorType) {
  }
}
