package io.github.genkidoudou.common.desensitization;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;

/**
 * Jackson 字段脱敏自动装配：注册 {@link SensitiveJacksonModule}，
 * Spring Boot Web 默认经由 {@code Jackson2ObjectMapperBuilderCustomizer} 合并进应用主
 * {@link ObjectMapper}，
 * REST JSON 写出即带字段掩码逻辑。
 * <p>
 * 本自动配置不包含「按角色明文」等非需求能力；不改变数据库或请求入参。<br>
 * 若与非 Web 子系统共用 Bean 级同一 {@link ObjectMapper}，请关注缓存序列化语义（参见 {@link SensitiveJacksonModule} 类注释）。
 */
@AutoConfiguration
@ConditionalOnClass({ObjectMapper.class, Jackson2ObjectMapperBuilderCustomizer.class})
public class SensitiveJacksonAutoConfiguration {

    /**
     * 供 {@link #sensitiveJacksonCustomizer(SensitiveJacksonModule)} 显式安装的模块 Bean。
     */
    @Bean
    public SensitiveJacksonModule sensitiveJacksonModule() {
        return new SensitiveJacksonModule();
    }

    /**
     * 将脱敏 Jackson 模块合并进 Spring MVC 所使用的 {@code ObjectMapper}。
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer sensitiveJacksonCustomizer(SensitiveJacksonModule module) {
        return builder -> builder.modules(module);
    }
}
