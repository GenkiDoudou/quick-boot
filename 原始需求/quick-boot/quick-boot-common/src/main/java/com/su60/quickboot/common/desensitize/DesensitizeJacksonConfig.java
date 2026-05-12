package com.su60.quickboot.common.desensitize;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson 脱敏配置。
 */
@Configuration
@RequiredArgsConstructor
public class DesensitizeJacksonConfig {

    private final ObjectMapper objectMapper;

    @PostConstruct
    public void configure() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(String.class, new SensitiveJsonSerializer());
        // 兼容 Long -> String 的序列化
        module.addSerializer(Long.class, ToStringSerializer.instance);
        module.addSerializer(Long.TYPE, ToStringSerializer.instance);
        objectMapper.registerModule(module);
    }
}
