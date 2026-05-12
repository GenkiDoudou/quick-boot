package com.su60.quickboot.common.bean;

import cn.hutool.core.collection.CollectionUtil;
import lombok.experimental.UtilityClass;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * bean转换。
 *
 * @author luyanan
 * @since 2023/09/08
 */
@UtilityClass
public class BeanConvertUtils extends BeanUtils {


	/**
	 * 对象转换
	 *
	 * @param source   源对象
	 * @param target   目标对象
	 * @param callBack 回调
	 * @param <T>      目标对象泛型
	 * @param <S>      源对象泛型
	 * @return T 返回结果
	 * @since 2023/09/08
	 */
	public <S, T> T convertTo(S source, T target, ConvertCallBack<S, T> callBack) {
		if (null == source || null == target) {
			return null;
		}
		copyProperties(source, target);
		if (null != callBack) {
			callBack.callBack(source, target);
		}
		return target;
	}


	/**
	 * 对象转换
	 *
	 * @param source 源对象
	 * @param target 目标对象
	 * @param <T>    目标对象泛型
	 * @param <S>    源对象泛型
	 * @return T 返回结果
	 * @since 2023/09/08
	 */
	public <S, T> T convertTo(S source, T target) {
		return convertTo(source, target, null);
	}


	/**
	 * bean转换
	 *
	 * @param source      源对象
	 * @param targetClazz 目标对象class
	 * @param <S>         源对象泛型
	 * @param <T>         目标对象泛型
	 * @since 2024/06/30
	 * @return T 返回结果
	 */
	public <S, T> T convertTo(S source, Class<T> targetClazz) {
		return convertTo(source, instantiate(targetClazz), null);
	}


	/**
	 * 对象转换
	 *
	 * @param source         源对象
	 * @param targetSupplier 目标对象
	 * @param callBack       回调
	 * @param <T>            目标对象泛型
	 * @param <S>            源对象泛型
	 * @return T 返回结果
	 * @since 2023/09/08
	 */
	public <S, T> T convertTo(S source, Supplier<T> targetSupplier, ConvertCallBack<S, T> callBack) {
		return convertTo(source, targetSupplier.get(), callBack);
	}

	/**
	 * 对象转换
	 *
	 * @param source         源对象
	 * @param targetSupplier 目标对象
	 * @param <T>            目标对象泛型
	 * @param <S>            源对象泛型
	 * @return T 返回结果
	 * @since 2023/09/08
	 */
	public <S, T> T convertTo(S source, Supplier<T> targetSupplier) {
		return convertTo(source, targetSupplier.get(), null);
	}


	/**
	 * list转换
	 *
	 * @param sources     源对象集合
	 * @param targetClass 目标对象
	 * @param callBack    回调
	 * @param <T>         目标对象泛型
	 * @param <S>         源对象泛型
	 * @return java.util.List<T> 转换结果
	 * @since 2023/09/08
	 */
	public <S, T> List<T> convertListTo(List<S> sources, Class<T> targetClass, ConvertCallBack<S, T> callBack) {
		if (CollectionUtil.isEmpty(sources)) {
			return new ArrayList<>();
		}
		List<T> targets = new ArrayList<>(sources.size());
		for (S source : sources) {
			T target = instantiateClass(targetClass);
			BeanUtils.copyProperties(source, target);
			if (callBack != null) {
				callBack.callBack(source, target);
			}
			targets.add(target);
		}
		return targets;
	}

	/**
	 * list转换
	 *
	 * @param sources        源对象集合
	 * @param targetSupplier 目标对象集合
	 * @param callBack       回调
	 * @param <T>            目标对象泛型
	 * @param <S>            源对象泛型
	 * @return java.util.List<T> 转换结果
	 * @since 2023/09/08
	 */
	public <S, T> List<T> convertListTo(List<S> sources, Supplier<T> targetSupplier, ConvertCallBack<S, T> callBack) {

		if (CollectionUtil.isEmpty(sources)) {
			return new ArrayList<>();
		}
		List<T> targets = new ArrayList<>(sources.size());
		for (S source : sources) {
			T target = targetSupplier.get();
			BeanUtils.copyProperties(source, target);
			if (callBack != null) {
				callBack.callBack(source, target);
			}
			targets.add(target);
		}
		return targets;
	}

	/**
	 * list转换
	 *
	 * @param sources     源对象集合
	 * @param targetClazz 目标对象集合
	 * @param <T>         目标对象泛型
	 * @param <S>         源对象泛型
	 * @return java.util.List<T> 转换结果
	 * @since 2023/09/08
	 */
	public <S, T> List<T> convertListTo(List<S> sources, Class<T> targetClazz) {
		return convertListTo(sources, targetClazz, null);
	}

	/**
	 * list转换
	 *
	 * @param sources        源对象集合
	 * @param targetSupplier 目标对象集合
	 * @param <T>            目标对象泛型
	 * @param <S>            源对象泛型
	 * @return java.util.List<T> 转换结果
	 * @since 2023/09/08
	 */
	public <S, T> List<T> convertListTo(List<S> sources, Supplier<T> targetSupplier) {
		return convertListTo(sources, targetSupplier, null);
	}


}
