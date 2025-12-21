package com.su60.quickboot.data.excel.annotation;

import com.su60.quickboot.data.excel.convert.ExcelDictConverter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Excel字典格式化注解
 *
 * @author luyanan
 * @since 2025/11/29
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelDictFormat {

	/**
	 * 字典类型键
	 *
	 * @return
	 */
	String dictType() default "";

	/**
	 * 字典文本
	 *
	 * @return
	 */
	String[] dictText() default {};

}