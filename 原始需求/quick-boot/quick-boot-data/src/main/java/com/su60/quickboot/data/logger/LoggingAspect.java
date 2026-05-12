package com.su60.quickboot.data.logger;

/**
 * 日志拦截
 *
 * @author luyanan
 * @since 2024/04/27
 **/

import com.su60.quickboot.data.logger.annotation.IgnoreLogger;
import com.su60.quickboot.data.spring.SpringContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;

import java.lang.reflect.Method;

/**
 * 日志拦截器
 *
 * @author luyanan
 * @since 2023/05/21
 **/
@Slf4j
@Aspect
@Order(-1)
public class LoggingAspect {


	/**
	 * 对注解进行拦截
	 *
	 * @param proceedingJoinPoint proceedingJoinPoint
	 * @return java.lang.Object
	 * @since 2023/05/21
	 */
	@Around("@annotation(org.springframework.web.bind.annotation.RestController)"
			+ "||@annotation(org.springframework.stereotype.Controller)"
			+ "||@annotation(org.springframework.web.bind.annotation.RequestMapping)"
			+ "||@annotation(org.springframework.web.bind.annotation.PostMapping)"
			+ "||@annotation(org.springframework.web.bind.annotation.GetMapping)"
			+ "||@annotation(org.springframework.web.bind.annotation.DeleteMapping)"
			+ "||@annotation(org.springframework.web.bind.annotation.PatchMapping)"
			+ "||@annotation(org.springframework.web.bind.annotation.PutMapping)")
	public Object around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

		long startTime = System.currentTimeMillis();
		Object[] args = proceedingJoinPoint.getArgs();
		Throwable e = null;

		Object result = null;

		try {
			result = proceedingJoinPoint.proceed();
		} catch (Throwable ex) {
			e = ex;
			throw ex;
		} finally {
			long endTime = System.currentTimeMillis();
			MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint.getSignature();
			Method method =
					methodSignature.getMethod();
			IgnoreLogger ignoreLogger = method.getAnnotation(IgnoreLogger.class);
			if (isIgnore(ignoreLogger, IgnoreLogger.Type.PARAMS)) {
				args = null;
			}
			if (null == ignoreLogger || ignoreLogger.type() != IgnoreLogger.Type.ALL) {
				LoggerEventDto loggerEventDto = new LoggerEventDto()
						.setEndTime(System.currentTimeMillis())
						.setStartTime(startTime)
						.setEndTime(endTime)
						.setThrowable(e)
						.setSignature(proceedingJoinPoint.getSignature())
						.setArgs(args);
				if (isIgnore(ignoreLogger, IgnoreLogger.Type.RESULT)) {
					loggerEventDto.setResult(null);
				} else {
					loggerEventDto.setResult(result);
				}
				// 发送事件
				SpringContextHolder.publishEvent(new LoggerEvent(loggerEventDto));
			}
		}
		return result;

	}

	/**
	 * 是否忽略
	 *
	 * @param ignoreLogger 忽略注解
	 * @param type         忽略类型
	 * @return boolean
	 * @since 2023/05/21
	 */
	private boolean isIgnore(IgnoreLogger ignoreLogger, IgnoreLogger.Type type) {
		return null != ignoreLogger && (ignoreLogger.type().equals(type)
				|| ignoreLogger.type().equals(IgnoreLogger.Type.ALL));
	}
}
