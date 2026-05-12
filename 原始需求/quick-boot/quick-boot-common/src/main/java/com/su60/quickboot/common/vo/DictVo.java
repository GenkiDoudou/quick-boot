package com.su60.quickboot.common.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 字典vo类
 *
 * @param <T> 泛型
 * @author luyanan
 * @since 2024/07/10
 **/
@Data
@Accessors(chain = true)
public class DictVo<T> {
	/**
	 * 字典的标签
	 *
	 * @since 2024/07/10
	 */

	private String label;


	/**
	 * 字典的值
	 *
	 * @since 2024/07/10
	 */

	private T value;

	/**
	 * 子选项
	 *
	 * @since 2024/8/3
	 */
	@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
	private List<DictVo<T>> children;
}
