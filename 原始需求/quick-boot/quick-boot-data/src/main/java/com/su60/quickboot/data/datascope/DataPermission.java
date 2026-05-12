package com.su60.quickboot.data.datascope;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataPermission {

	/**
	 * 需要进行数据权限控制的表名
	 * 支持通配：order_*
	 */
	String[] tables();

	/**
	 * 部门字段名
	 */
	String deptField() default "dept_id";

	/**
	 * 用户字段名
	 */
	String userField() default "create_by";

	/**
	 * 是否忽略数据权限
	 */
	boolean ignore() default false;
}
