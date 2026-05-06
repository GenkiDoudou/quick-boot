package io.github.genkidoudou.common.file.url;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记仅存相对路径的字段：JSON 写出时拼接访问域名；入参若为带 domain 前缀的完整 URL 则剥离为相对路径。
 * <p>
 * 与全局 {@code default-property-inclusion: non_null} 并存时，通过 {@link FileUrlAnnotationIntrospector} 对该字段强制
 * {@link com.fasterxml.jackson.annotation.JsonInclude.Include#ALWAYS}，以保留 JSON {@code null}。
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface FileUrl {

    /**
     * 非空时优先于 {@code qc.file.domain}。
     */
    String domain() default "";
}
