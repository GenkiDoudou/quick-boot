package io.github.genkidoudou.web.config;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;

/**
 * Jackson 时间类型序列化配置。
 * <p>
 * 显式注册 {@link JavaTimeModule}，避免 LocalDateTime 在部分运行时环境下缺少模块导致序列化失败。
 */
@Configuration
public class JacksonTimeConfig {

    /**
     * 注册 JavaTimeModule 并关闭时间戳输出，统一使用可读字符串时间。
     *
     * @return Jackson 自定义器
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer javaTimeModuleCustomizer() {
        return builder -> {
            builder.modulesToInstall(JavaTimeModule.class);
            builder.featuresToDisable(WRITE_DATES_AS_TIMESTAMPS);
        };
    }
}
