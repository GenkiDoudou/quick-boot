package io.github.genkidoudou.common.security.firewall.idempotent;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 方法级幂等：仅当请求携带非空幂等 token 头时生效；键材料见 {@link IdempotentProperties#getTokenHeader()}。
 * <p>
 * {@link #expireTime()} 为负数时使用全局 {@code qc.security.firewall.idempotent.expire-time}。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Idempotent {

    /**
     * 占位 TTL 数值；{@code <0} 表示使用全局配置。
     */
    long expireTime() default -1L;

    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 追加在全局 {@code keyPrefix} 之后的键分段。
     */
    String prefix() default "";

    /**
     * 为 {@code true} 时业务方法正常返回后删除占位（短窗口防连点）；默认仅依赖 TTL。
     */
    boolean deleteAfterExecution() default false;
}
