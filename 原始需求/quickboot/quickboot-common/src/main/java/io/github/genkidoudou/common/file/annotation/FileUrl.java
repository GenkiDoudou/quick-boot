package io.github.genkidoudou.common.file.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 文件 URL 注解
 * 序列化时自动补全域名，反序列化时自动剥离域名
 * 数据库存相对路径，接口返回/接收完整 URL
 *
 * @author genkidoudou
 * @since 2026/03/11
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(using = FileUrlSerializer.class)
@JsonDeserialize(using = FileUrlDeserializer.class)
public @interface FileUrl {

    /**
     * 域名前缀，空则从配置读取 qc.file.domain
     */
    String domain() default "";
}
