package com.su60.quickboot.common.idempotent;

import java.lang.annotation.*;

/**
 * 防幂等注解
 * <p>
 * 用于标记需要防幂等处理的接口方法
 * 通过缓存机制确保相同请求在指定时间内只处理一次
 * </p>
 *
 * @author luyanan
 * @since 2026/01/18
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Idempotent {

	/**
	 * 幂等标识的key，支持SpEL表达式
	 * <p>
	 * 示例：
	 * <ul>
	 *   <li>#requestId - 从方法参数中获取requestId</li>
	 *   <li>#p0 - 第一个参数</li>
	 *   <li>#id - 从参数对象中获取id属性</li>
	 *   <li>#request.getHeader('X-Request-Id') - 从HttpServletRequest中获取header</li>
	 * </ul>
	 * 如果不指定，将自动从请求头、参数中查找，或使用请求路径+参数生成
	 * </p>
	 *
	 * @return 幂等标识的key表达式
	 */
	String key() default "";

	/**
	 * 缓存过期时间（秒）
	 * <p>
	 * 默认60秒，即60秒内相同请求会被拦截
	 * </p>
	 *
	 * @return 过期时间（秒）
	 */
	int expireTime() default 60;

	/**
	 * 缓存名称
	 * <p>
	 * 如果不指定，将使用默认的缓存名称 "idempotent"
	 * </p>
	 *
	 * @return 缓存名称
	 */
	String cacheName() default "idempotent";

	/**
	 * 重复请求时的提示消息
	 *
	 * @return 提示消息
	 */
	String message() default "请勿重复提交";

	/**
	 * 是否在方法执行完成后清除缓存
	 * <p>
	 * 如果为true，方法执行成功后会自动清除缓存，允许相同请求再次执行
	 * 如果为false，缓存会在过期时间后自动清除
	 * </p>
	 *
	 * @return 是否清除缓存
	 */
	boolean clearOnSuccess() default false;
}
