package com.su60.quickboot.common.bean;

/**
 * bean转换回调
 *
 * @param <S> 源对象泛型
 * @param <T> 目标对象泛型
 * @author luyanan
 * @since 2023/09/08
 **/
@FunctionalInterface
public interface ConvertCallBack<S, T> {


	/**
	 * 回调函数
	 *
	 * @param s 源对象
	 * @param t 目标对象
	 * @since 2023/09/08
	 */
	void callBack(S s, T t);
}
