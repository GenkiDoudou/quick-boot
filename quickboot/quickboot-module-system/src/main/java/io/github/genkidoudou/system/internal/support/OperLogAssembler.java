package io.github.genkidoudou.system.internal.support;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.common.monitor.operlog.OperLogCapturePayload;
import io.github.genkidoudou.common.monitor.operlog.OperLogPublishingAspect;
import io.github.genkidoudou.common.monitor.operlog.OperLogSensitiveMasker;
import io.github.genkidoudou.system.internal.entity.SysDept;
import io.github.genkidoudou.system.internal.entity.SysOperLog;
import io.github.genkidoudou.system.internal.entity.SysUser;
import io.github.genkidoudou.system.internal.mapper.SysDeptMapper;
import io.github.genkidoudou.system.internal.mapper.SysUserMapper;
import io.github.genkidoudou.system.internal.vo.LoginRequestVo;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 将采集载荷组装为 {@link SysOperLog} 实体（含脱敏与状态映射）。
 */
@Component
public class OperLogAssembler {

  private static final int MAX_ERROR_MSG = 3800;

  private final ObjectMapper objectMapper;
  private final SysUserMapper userMapper;
  private final SysDeptMapper deptMapper;

  /**
   * @param objectMapper Jackson
   * @param userMapper   用户
   * @param deptMapper   部门
   */
  public OperLogAssembler(ObjectMapper objectMapper, SysUserMapper userMapper, SysDeptMapper deptMapper) {
    this.objectMapper = objectMapper;
    this.userMapper = userMapper;
    this.deptMapper = deptMapper;
  }

  /**
   * @param payload 切面线程发布的载荷
   * @return 待插入实体
   */
  public SysOperLog assemble(OperLogCapturePayload payload) {
    MethodSignature ms = (MethodSignature) payload.getSignature();
    Method method = ms.getMethod();
    Class<?> declaring = ms.getDeclaringType();
    OperLogMetaResolver.ResolvedOperLogMeta meta = OperLogMetaResolver.resolve(
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

  private static boolean isLoginRequest(String requestUri) {
    if (StrUtil.isBlank(requestUri)) {
      return false;
    }
    String uri = requestUri.trim();
    return uri.endsWith("/login") && !uri.endsWith("/login/captcha-config");
  }

  /**
   * 登录失败或未建立会话时，从方法参数中提取尝试登录的用户名。
   */
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
}
