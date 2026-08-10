package io.github.genkidoudou.common.desensitization;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记「本接口响应需要字段脱敏」。
 * <p>
 * 仅当当前请求命中的 Controller 方法（或类）带有本注解时，
 * 字段上的 {@link Sensitive} 才会在 JSON 序列化时生效；否则原样输出，避免编辑回显掩码写回库。
 *
 * @see Sensitive
 * @see SensitiveResponseContext
 */
@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SensitiveResponse {
}
