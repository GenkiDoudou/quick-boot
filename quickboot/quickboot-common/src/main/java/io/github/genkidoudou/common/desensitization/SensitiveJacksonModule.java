package io.github.genkidoudou.common.desensitization;

import tools.jackson.databind.module.SimpleModule;

/**
 * Jackson 模块：注册 {@link SensitiveBeanSerializerModifier}，
 * 使 {@link Sensitive} 在 Spring MVC JSON 写出链路生效。
 * <p>
 * <b>注意</b>：本模块被注册进应用主 {@link tools.jackson.databind.ObjectMapper}
 * （通过 {@link org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer}）。
 * 若其它组件（如 Redis Cache 的 JSON 序列化）与 Web 共用同一 {@code ObjectMapper}，缓存载荷也可能被掩码，
 * 业务需在 DTO 层区分「对外 VO」与「缓存实体」，或拆分 {@code ObjectMapper} Bean。
 */
public class SensitiveJacksonModule extends SimpleModule {

    public SensitiveJacksonModule() {
        super("quickboot-sensitive");
        setSerializerModifier(new SensitiveBeanSerializerModifier());
    }
}
