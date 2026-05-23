package com.su60.quickboot.data.file.jackson;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.lang.annotation.*;

/**
 * 文件格式化
 *
 * @author luyanan
 * @since 2024/06/16
 **/
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
@JacksonAnnotationsInside
@JsonSerialize(using = FileHostJsonSerialize.class)
@JsonDeserialize(using = FileHostJsonDeserialize.class)
public @interface FileHostFormat {


	/**
	 * 添加host
	 *
	 * @return 是否添加成功
	 * @since 2024/01/12
	 */
	boolean addHost() default true;

	/**
	 * 移除host
	 *
	 * @return 是否成功
	 * @since 2024/01/12
	 */
	boolean removeHost() default true;


}
