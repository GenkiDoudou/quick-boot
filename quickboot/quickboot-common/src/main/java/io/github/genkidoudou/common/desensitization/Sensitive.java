package io.github.genkidoudou.common.desensitization;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记需在 <b>JSON 序列化</b> 时脱敏的 {@link String} 字段或 getter；
 * <b>不会修改</b> 内存中的属性值。
 * <p>
 * {@link #strategy()} 仅在 {@link #type()} 为 {@link SensitiveType#CUSTOM} 时解析，格式为非负整数
 * {@code prefix,suffix}，表示首尾保留位数，中间段以与原文等长的 {@code *} 填充。
 *
 * @see SensitiveMasking
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Sensitive {

    /** 默认 {@link SensitiveType#CUSTOM}。 */
    SensitiveType type() default SensitiveType.CUSTOM;

    /** 仅在 {@link SensitiveType#CUSTOM} 下生效；非法或空字符串时原样输出。 */
    String strategy() default "";
}
