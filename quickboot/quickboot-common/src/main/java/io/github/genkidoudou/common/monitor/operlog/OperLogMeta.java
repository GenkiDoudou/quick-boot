package io.github.genkidoudou.common.monitor.operlog;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 显式指定写入操作日志的模块标题与业务类型（宽切面无若依 {@code @Log} 时的优先元数据）。
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface OperLogMeta {

    /** 模块标题，对应表字段 {@code title}。 */
    String title() default "";

    /**
     * 业务类型整型值，与字典 {@code sys_oper_business_type} 一致；默认 {@code 0} 时可由落库层按 HTTP 语义推断。
     *
     * @see OperLogBusinessType
     */
    int businessType() default 0;

    /**
     * 操作者类别，与字典 {@code sys_oper_operator_type} 一致；默认 {@code 1} 表示后台用户。
     */
    int operatorType() default 1;
}
