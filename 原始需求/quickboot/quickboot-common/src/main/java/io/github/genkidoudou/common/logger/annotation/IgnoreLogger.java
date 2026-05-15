package io.github.genkidoudou.common.logger.annotation;

import java.lang.annotation.*;

/**
 * 忽略日志记录注解
 * 用于标记不需要记录日志的接口或方法
 *
 * @author genkidoudou
 * @since 2026/03/05
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IgnoreLogger {

    /**
     * 忽略类型
     *
     * @return 忽略类型
     * @since 2026/03/05
     */
    Type type() default Type.ALL;

    /**
     * 忽略类型枚举
     *
     * @since 2026/03/05
     */
    enum Type {
        /**
         * 忽略所有日志
         */
        ALL,

        /**
         * 忽略请求参数
         */
        PARAMS,

        /**
         * 忽略返回结果
         */
        RESULT
    }
}
