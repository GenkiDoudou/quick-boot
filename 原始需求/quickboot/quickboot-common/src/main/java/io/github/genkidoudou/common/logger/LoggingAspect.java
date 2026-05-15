package io.github.genkidoudou.common.logger;

import io.github.genkidoudou.common.logger.annotation.IgnoreLogger;
import io.github.genkidoudou.common.trace.TraceUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.annotation.Order;

import java.lang.reflect.Method;

/**
 * 日志拦截切面
 * 拦截所有 Controller 方法，记录请求日志
 *
 * @author genkidoudou
 * @since 2026/03/05
 */
@Slf4j
@Aspect
@Order(-1)
@RequiredArgsConstructor
public class LoggingAspect {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * 拦截所有 Controller 方法
     *
     * @param proceedingJoinPoint 切点
     * @return 方法返回值
     * @throws Throwable 异常
     * @since 2026/03/05
     */
    @Around("@within(org.springframework.web.bind.annotation.RestController) || "
            + "@within(org.springframework.stereotype.Controller) || "
            + "@annotation(org.springframework.web.bind.annotation.RequestMapping) || "
            + "@annotation(org.springframework.web.bind.annotation.PostMapping) || "
            + "@annotation(org.springframework.web.bind.annotation.GetMapping) || "
            + "@annotation(org.springframework.web.bind.annotation.DeleteMapping) || "
            + "@annotation(org.springframework.web.bind.annotation.PatchMapping) || "
            + "@annotation(org.springframework.web.bind.annotation.PutMapping)")
    public Object around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object[] args = proceedingJoinPoint.getArgs();
        Throwable exception = null;
        Object result = null;

        try {
            result = proceedingJoinPoint.proceed();
            return result;
        } catch (Throwable ex) {
            exception = ex;
            throw ex;
        } finally {
            long endTime = System.currentTimeMillis();
            MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint.getSignature();
            Method method = methodSignature.getMethod();
            IgnoreLogger ignoreLogger = method.getAnnotation(IgnoreLogger.class);

            // 检查是否忽略所有日志
            if (ignoreLogger == null || ignoreLogger.type() != IgnoreLogger.Type.ALL) {
                // 处理参数
                if (isIgnore(ignoreLogger, IgnoreLogger.Type.PARAMS)) {
                    args = null;
                }

                // 构建日志事件DTO
                LoggerEventDto loggerEventDto = new LoggerEventDto()
                        .setStartTime(startTime)
                        .setEndTime(endTime)
                        .setThrowable(exception)
                        .setSignature(proceedingJoinPoint.getSignature())
                        .setArgs(args)
                        .setTraceId(TraceUtil.getTraceId());

                // 处理返回结果
                if (isIgnore(ignoreLogger, IgnoreLogger.Type.RESULT)) {
                    loggerEventDto.setResult(null);
                } else {
                    loggerEventDto.setResult(result);
                }

                // 发布日志事件
                eventPublisher.publishEvent(new LoggerEvent(loggerEventDto));
            }
        }
    }

    /**
     * 判断是否忽略
     *
     * @param ignoreLogger 忽略注解
     * @param type         忽略类型
     * @return 是否忽略
     * @since 2026/03/05
     */
    private boolean isIgnore(IgnoreLogger ignoreLogger, IgnoreLogger.Type type) {
        return ignoreLogger != null && (ignoreLogger.type().equals(type)
                || ignoreLogger.type().equals(IgnoreLogger.Type.ALL));
    }
}
