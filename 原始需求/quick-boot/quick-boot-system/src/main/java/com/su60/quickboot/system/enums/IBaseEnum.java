package com.su60.quickboot.system.enums;

import cn.hutool.core.util.ObjectUtil;

import java.util.EnumSet;

/**
 * 枚举通用接口
 *
 * @author luyanan
 * @since 2024/07/25
 **/
public interface IBaseEnum {


	/**
	 * 获取字典value值
	 *
	 * @return 字典value值
	 * @since 2024/07/25
	 */
	String getValue();


	/**
	 * 获取字典标签值
	 *
	 * @return 字典标签值
	 * @since 2024/07/25
	 */
	String getLabel();


	/**
	 * 字典的类型
	 *
	 * @return 字典类型
	 * @since 2024/07/25
	 */
	IType getType();


	/**
	 * 字典type的抽象类
	 *
	 * @since 2024/07/25
	 */

	public interface IType {
	}


	/**
	 * 根据value值获取字典
	 *
	 * @param <E>   枚举类
	 * @param type  字典tgpe
	 * @param value 字典值
	 * @param clazz 字典class
	 * @return 字典枚举
	 * @since 2024/8/5
	 */
	static <E extends Enum<E> & IBaseEnum> E getByValue(IType type, String value, Class<E> clazz) {
		EnumSet<E> es = EnumSet.allOf(clazz);
		return es.stream().filter(a -> ObjectUtil.equal(a.getType(), type) && ObjectUtil.equal(a.getValue(), value))
				.findFirst().orElse(null);

	}


}
