package io.github.genkidoudou.common.desensitization;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 字段脱敏注解
 * 用于标记需要脱敏的字段，在序列化时自动进行脱敏处理
 *
 * @author QuickBoot
 * @since 2026/03/02
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(using = SensitiveJsonSerializer.class)
public @interface Sensitive {

    /**
     * 脱敏类型
     */
    SensitiveType type() default SensitiveType.CUSTOM;

    /**
     * 自定义脱敏策略（仅当 type = SensitiveType.CUSTOM 时生效）
     * 
     * 格式："{保留前几位},{保留后几位}"
     * 
     * 使用示例：
     * <pre>
     * // 保留前3位和后4位，中间用*替换
     * &#64;Sensitive(type = SensitiveType.CUSTOM, strategy = "3,4")
     * private String accountNo;  // 1234567890 -> 123***7890
     * 
     * // 保留前6位和后4位
     * &#64;Sensitive(type = SensitiveType.CUSTOM, strategy = "6,4")
     * private String orderNo;  // ORDER20240101001 -> ORDER2***1001
     * </pre>
     * 
     * 注意：
     * - 必须是两个数字，用英文逗号分隔
     * - 如果原始字符串长度小于等于保留位数之和，则不脱敏
     * - 其他脱敏类型（NAME、MOBILE等）不需要设置此属性
     */
    String strategy() default "";
}
