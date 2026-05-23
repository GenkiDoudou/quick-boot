package com.su60.quickboot.data.logger.annotation;

import java.lang.annotation.*;

/**
 * 忽略日志打印注解
 *
 * @author luyanan
 * @since 2023/10/08
 **/
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface IgnoreLogger {

	Type type() default Type.ALL;

	/**
	 * 类型
	 *
	 * @since 2023/10/08
	 */

	enum Type {

		/**
		 * 参数
		 *
		 * @since 2023/10/08
		 */

		PARAMS,

		/**
		 * 返回结果
		 *
		 * @since 2023/10/08
		 */

		RESULT,


		/**
		 * 全部
		 *
		 * @since 2023/10/08
		 */

		ALL

	}
}
