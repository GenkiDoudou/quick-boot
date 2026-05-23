//package com.su60.quickboot.common.idempotent;
//
//import cn.hutool.core.util.StrUtil;
//import jakarta.servlet.http.HttpServletRequest;
//import lombok.extern.slf4j.Slf4j;
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.Around;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.reflect.MethodSignature;
//import org.springframework.cache.Cache;
//import org.springframework.cache.CacheManager;
//import org.springframework.core.annotation.Order;
//import org.springframework.stereotype.Component;
//import org.springframework.expression.EvaluationContext;
//import org.springframework.expression.Expression;
//import org.springframework.expression.ExpressionParser;
//import org.springframework.expression.spel.standard.SpelExpressionParser;
//import org.springframework.expression.spel.support.StandardEvaluationContext;
//import org.springframework.web.context.request.RequestContextHolder;
//import org.springframework.web.context.request.ServletRequestAttributes;
//
//import java.lang.reflect.Method;
//
///**
// * 幂等切面
// * <p>
// * 拦截标记了 @Idempotent 注解的方法，实现防重复提交功能
// * </p>
// *
// * @author luyanan
// * @since 2026/01/18
// */
//@Slf4j
//@Aspect
//@Component
//@Order(1) // 优先级高于日志切面
//public class IdempotentAspect {
//
//	private final CacheManager cacheManager;
//	private final ExpressionParser parser = new SpelExpressionParser();
//
//	public IdempotentAspect(CacheManager cacheManager) {
//		this.cacheManager = cacheManager;
//	}
//
//	/**
//	 * 环绕通知，处理幂等逻辑
//	 *
//	 * @param joinPoint 连接点
//	 * @return 方法执行结果
//	 * @throws Throwable 异常
//	 */
//	@Around("@annotation(com.su60.quickboot.common.idempotent.Idempotent)")
//	public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
//		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
//		Method method = signature.getMethod();
//		Idempotent idempotent = method.getAnnotation(Idempotent.class);
//
//		if (idempotent == null) {
//			return joinPoint.proceed();
//		}
//
//		// 获取HTTP请求
//		HttpServletRequest request = getCurrentRequest();
//		if (request == null) {
//			log.warn("无法获取HttpServletRequest，跳过幂等检查");
//			return joinPoint.proceed();
//		}
//
//		// 生成幂等标识
//		String idempotentKey = generateIdempotentKey(idempotent, joinPoint, request);
//
//		// 获取缓存
//		Cache cache = cacheManager.getCache(idempotent.cacheName());
//		if (cache == null) {
//			log.warn("缓存[{}]不存在，跳过幂等检查", idempotent.cacheName());
//			return joinPoint.proceed();
//		}
//
//		// 检查是否已存在
//		Cache.ValueWrapper valueWrapper = cache.get(idempotentKey);
//		if (valueWrapper != null) {
//			Object value = valueWrapper.get();
//			// 如果值是 IdempotentValue 类型，检查是否过期
//			if (value instanceof IdempotentValue) {
//				IdempotentValue idempotentValue = (IdempotentValue) value;
//				if (!idempotentValue.isExpired()) {
//					log.warn("检测到重复请求，幂等标识: {}", idempotentKey);
//					throw new IdempotentException(idempotent.message());
//				}
//				// 已过期，清除缓存
//				cache.evict(idempotentKey);
//			} else {
//				// 兼容旧数据格式（直接存储时间戳）
//				log.warn("检测到重复请求，幂等标识: {}", idempotentKey);
//				throw new IdempotentException(idempotent.message());
//			}
//		}
//
//		// 将标识存入缓存，使用包装类存储过期时间
//		IdempotentValue idempotentValue = new IdempotentValue(
//				System.currentTimeMillis(),
//				idempotent.expireTime()
//		);
//		cache.put(idempotentKey, idempotentValue);
//
//		try {
//			// 执行方法
//			Object result = joinPoint.proceed();
//
//			// 如果配置了成功后清除缓存
//			if (idempotent.clearOnSuccess()) {
//				cache.evict(idempotentKey);
//				log.debug("方法执行成功，已清除幂等缓存: {}", idempotentKey);
//			}
//
//			return result;
//		} catch (Throwable e) {
//			// 方法执行失败，清除缓存，允许重试
//			cache.evict(idempotentKey);
//			log.debug("方法执行失败，已清除幂等缓存: {}", idempotentKey);
//			throw e;
//		}
//	}
//
//	/**
//	 * 生成幂等标识
//	 *
//	 * @param idempotent 幂等注解
//	 * @param joinPoint  连接点
//	 * @param request    HTTP请求
//	 * @return 幂等标识
//	 */
//	private String generateIdempotentKey(Idempotent idempotent, ProceedingJoinPoint joinPoint, HttpServletRequest request) {
//		String keyExpression = idempotent.key();
//
//		// 如果指定了key表达式，尝试解析
//		if (StrUtil.isNotBlank(keyExpression)) {
//			try {
//				String key = parseSpEL(keyExpression, joinPoint, request);
//				if (StrUtil.isNotBlank(key)) {
//					return key;
//				}
//			} catch (Exception e) {
//				log.warn("解析SpEL表达式失败: {}, 使用默认方式生成", keyExpression, e);
//			}
//		}
//
//		// 使用默认方式生成
//		return IdempotentUtils.generateIdempotentKey(request);
//	}
//
//	/**
//	 * 解析SpEL表达式
//	 *
//	 * @param expression SpEL表达式
//	 * @param joinPoint  连接点
//	 * @param request    HTTP请求
//	 * @return 解析结果
//	 */
//	private String parseSpEL(String expression, ProceedingJoinPoint joinPoint, HttpServletRequest request) {
//		Expression exp = parser.parseExpression(expression);
//		EvaluationContext context = new StandardEvaluationContext();
//
//		// 设置方法参数
//		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
//		String[] paramNames = signature.getParameterNames();
//		Object[] args = joinPoint.getArgs();
//
//		for (int i = 0; i < paramNames.length; i++) {
//			context.setVariable(paramNames[i], args[i]);
//			context.setVariable("p" + i, args[i]);
//		}
//
//		// 设置request
//		context.setVariable("request", request);
//
//		// 执行表达式
//		Object value = exp.getValue(context);
//		return value != null ? value.toString() : null;
//	}
//
//	/**
//	 * 获取当前HTTP请求
//	 *
//	 * @return HttpServletRequest
//	 */
//	private HttpServletRequest getCurrentRequest() {
//		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//		return attributes != null ? attributes.getRequest() : null;
//	}
//}
