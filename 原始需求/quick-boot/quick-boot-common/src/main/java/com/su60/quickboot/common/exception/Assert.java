package com.su60.quickboot.common.exception;

import cn.hutool.core.collection.CollectionUtil;
import lombok.experimental.UtilityClass;

/**
 * 断言
 *
 * @author luyanan
 * @since 2023/09/09
 **/
@UtilityClass
public class Assert extends cn.hutool.core.lang.Assert {


	/**
	 * 断言是否为真,如果为false则抛出给定的异常码
	 *
	 * @param expression 断言
	 * @param code       异常码
	 * @since 2023/09/09
	 */
	public static void isTrue(boolean expression, Integer code) {

		if (!expression) {
			throw new WarningException(code);
		}
	}

	/**
	 * 断言是否为否,如果为true则抛出给定的状态码
	 *
	 * @param expression 断言
	 * @param code       异常码
	 * @since 2023/09/09
	 */
	public static void isFalse(boolean expression, Integer code) {
		if (expression) {
			throw new WarningException(code);
		}
	}


	/**
	 * 断言对象是否为null,如果不为null则抛出异常
	 *
	 * @param obj  对象
	 * @param code 异常码
	 * @since 2023/09/09
	 */
	public void isNull(Object obj, Integer code) {
		if (null != obj) {
			throw new WarningException(code);
		}
	}

	/**
	 * 判断对象是否不为null, 如果为null则抛出异常
	 *
	 * @param obj  对象
	 * @param code 异常码
	 * @since 2023/09/09
	 */
	public void isNotNull(Object obj, Integer code) {
		if (null == obj) {
			throw new WarningException(code);
		}
	}

	/**
	 * 断言集合是否不为空,如果为空则抛出异常
	 *
	 * @param <T>        集合类
	 * @param collection 集合
	 * @param code       异常码
	 * @since 2023/09/09
	 */
	public <T extends Iterable> void notEmpty(T collection, Integer code) {
		if (CollectionUtil.isEmpty(collection)) {
			throw new WarningException(code);
		}
	}

}
