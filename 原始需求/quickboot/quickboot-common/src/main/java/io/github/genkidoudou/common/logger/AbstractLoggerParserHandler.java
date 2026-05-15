package io.github.genkidoudou.common.logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.genkidoudou.common.core.GlobalMsgCode;
import io.github.genkidoudou.common.exception.BaseException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 日志解析处理器
 *
 * @author genkidoudou
 * @since 2026/03/05
 */
@Slf4j
public abstract class AbstractLoggerParserHandler {

    /**
     * 方法描述分割线
     *
     * @since 2026/03/05
     */
    protected static final String DESCRIPTION_DIVIDING_LINE = "-";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 解析日志事件为日志信息
     *
     * @param loggerEventDto 日志事件DTO
     * @return 日志信息
     * @since 2026/03/05
     */
    public LoggerInfo parseToLoggerInfo(LoggerEventDto loggerEventDto) {
        if (loggerEventDto == null) {
            return null;
        }

        Signature signature = loggerEventDto.getSignature();
        Method method = null;
        String params = null;

        if (signature != null) {
            MethodSignature methodSignature = (MethodSignature) signature;
            method = methodSignature.getMethod();
            Map<String, Object> requestParams = getRequestParams(methodSignature, loggerEventDto.getArgs());
            params = toJsonString(requestParams);
        } else {
            params = toJsonString(loggerEventDto.getArgs());
        }

        // 计算耗时
        Long timeConsuming = null;
        if (loggerEventDto.getStartTime() != null && loggerEventDto.getEndTime() != null) {
            timeConsuming = loggerEventDto.getEndTime() - loggerEventDto.getStartTime();
        }

        // 获取请求信息
        HttpServletRequest request = getRequest();

        // 处理返回结果
        String result = null;
        if (loggerEventDto.getResult() != null) {
            Object resultObj = loggerEventDto.getResult();
            if (resultObj instanceof String) {
                result = (String) resultObj;
            } else {
                result = toJsonString(resultObj);
            }
        }

        // 获取客户端IP
        String clientIp = null;
        if (request != null) {
            clientIp = getClientIP(request);
        }

        String traceId = MDC.get("traceId");
        // 构建日志信息
        LoggerInfo loggerInfo = new LoggerInfo()
                .setMethodName(method != null ? method.getDeclaringClass().getName() + "." + method.getName() : null)
                .setDescription(getDescription(method))
                .setResult(result)
                .setStartTime(loggerEventDto.getStartTime())
                .setEndTime(loggerEventDto.getEndTime())
                .setTimeConsuming(timeConsuming)
                .setTraceId(loggerEventDto.getTraceId() != null ? loggerEventDto.getTraceId() : traceId)
                .setErrorMsg(loggerEventDto.getThrowable() != null ? loggerEventDto.getThrowable().getLocalizedMessage() : null)
                .setCode(getErrorCode(loggerEventDto.getThrowable()))
                .setSourceIp(clientIp)
                .setUri(request != null ? request.getRequestURI() : null)
                .setMethod(request != null ? request.getMethod() : null)
                .setRequestParams(params);

        return loggerInfo;
    }

    /**
     * 获取方法描述
     * 优先从 OpenAPI 3.0 注解获取，兼容 Swagger 2.0
     *
     * @param method 方法
     * @return 方法描述
     * @since 2026/03/05
     */
    protected String getDescription(Method method) {
        if (method == null) {
            return null;
        }

        StringBuilder description = new StringBuilder();

        // 尝试从 OpenAPI 3.0 注解获取
        if (method.getDeclaringClass().isAnnotationPresent(Tag.class)) {
            Tag tag = method.getDeclaringClass().getAnnotation(Tag.class);
            String tagName = tag.name();
            if (StringUtils.hasText(tagName)) {
                description.append(tagName).append(DESCRIPTION_DIVIDING_LINE);
            }
        }

        if (method.isAnnotationPresent(Operation.class)) {
            Operation operation = method.getAnnotation(Operation.class);
            String summary = operation.summary();
            if (StringUtils.hasText(summary)) {
                description.append(summary);
            }
        }

        // 移除末尾的分割线
        if (description.toString().endsWith(DESCRIPTION_DIVIDING_LINE)) {
            description.deleteCharAt(description.length() - 1);
        }

        return description.length() > 0 ? description.toString() : null;
    }

    /**
     * 获取请求参数
     *
     * @param methodSignature 方法签名
     * @param args            参数值
     * @return 请求参数Map
     * @since 2026/03/05
     */
    protected Map<String, Object> getRequestParams(MethodSignature methodSignature, Object[] args) {
        if (args == null || args.length == 0) {
            return new HashMap<>();
        }

        Map<String, Object> requestParams = new HashMap<>(16);
        String[] paramNames = methodSignature.getParameterNames();

        if (paramNames == null || paramNames.length == 0) {
            return new HashMap<>();
        }

        for (int i = 0; i < paramNames.length; i++) {
            Object value = args[i];
            if (value == null) {
                continue;
            }

            // 过滤特殊类型
            if (value instanceof MultipartFile) {
                MultipartFile file = (MultipartFile) value;
                value = file.getOriginalFilename();
            } else if (value instanceof ServletResponse || value instanceof ServletRequest) {
                continue;
            } else if (value instanceof Model || value.getClass().getName().startsWith("org.springframework")) {
                continue;
            }

            requestParams.put(paramNames[i], value);
        }

        return requestParams;
    }

    /**
     * 获取错误码
     *
     * @param throwable 异常
     * @return 错误码
     * @since 2026/03/05
     */
    protected Integer getErrorCode(Throwable throwable) {
        if (throwable == null) {
            return GlobalMsgCode.SUCCESS;
        }

        if (throwable instanceof BaseException) {
            BaseException baseException = (BaseException) throwable;
            Integer code = baseException.getCode();
            return code != null ? code : GlobalMsgCode.INTERNAL_SERVER_ERROR;
        }

        return GlobalMsgCode.INTERNAL_SERVER_ERROR;
    }

    /**
     * 获取当前请求
     *
     * @return HttpServletRequest
     * @since 2026/03/05
     */
    protected HttpServletRequest getRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取客户端IP
     *
     * @param request 请求
     * @return 客户端IP
     * @since 2026/03/05
     */
    protected String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * 对象转JSON字符串
     *
     * @param obj 对象
     * @return JSON字符串
     * @since 2026/03/05
     */
    protected String toJsonString(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("对象转JSON失败", e);
            return obj.toString();
        }
    }
}
