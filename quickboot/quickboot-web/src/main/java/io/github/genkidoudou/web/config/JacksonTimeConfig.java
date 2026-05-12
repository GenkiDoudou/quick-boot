package io.github.genkidoudou.web.config;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;

/**
 * Jackson 序列化配置。
 * <p>
 * 显式注册 {@link JavaTimeModule}，避免 LocalDateTime 在部分运行时环境下缺少模块导致序列化失败。
 * 同时将 {@code Long}/{@code long} 统一序列化为字符串，规避前端 JavaScript Number 精度丢失导致的 ID 问题。
 */
@Configuration
public class JacksonTimeConfig {

    /**
     * 注册 JavaTimeModule 并关闭时间戳输出，统一使用可读字符串时间。
     * 此外将 Long 类型序列化为字符串，保证雪花 ID 等大整数在前端传输过程中不丢精度。
     *
     * @return Jackson 自定义器
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer javaTimeModuleCustomizer() {
        return builder -> {
            builder.modulesToInstall(JavaTimeModule.class);
            builder.featuresToDisable(WRITE_DATES_AS_TIMESTAMPS);
            builder.serializerByType(Long.class, ToStringSerializer.instance);
            builder.serializerByType(Long.TYPE, ToStringSerializer.instance);
        };
    }
}
