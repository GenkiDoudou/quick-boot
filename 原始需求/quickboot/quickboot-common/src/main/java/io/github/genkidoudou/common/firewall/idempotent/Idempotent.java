package io.github.genkidoudou.common.firewall.idempotent;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 幂等注解
 * 用于标记需要幂等处理的接口方法
 *
 * @author QuickBoot
 * @since 2026/03/02
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {

    /**
     * 幂等键前缀
     * 默认使用方法全限定名
     */
    String prefix() default "";

    /**
     * 幂等键生成策略
     */
    KeyGenerateStrategy strategy() default KeyGenerateStrategy.DEFAULT;

    /**
     * 自定义幂等键生成器 Bean 名称
     * 当 strategy = CUSTOM 时使用
     */
    String keyGenerator() default "";

    /**
     * 过期时间
     * 默认使用全局配置
     */
    long expireTime() default -1;

    /**
     * 过期时间单位
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 提示信息
     */
    String message() default "请勿重复提交";

    /**
     * 是否删除键（执行完成后是否删除幂等键）
     * true: 执行完成后删除键，允许再次执行
     * false: 保留键直到过期，过期前不允许再次执行
     */
    boolean deleteAfterExecution() default false;
}
