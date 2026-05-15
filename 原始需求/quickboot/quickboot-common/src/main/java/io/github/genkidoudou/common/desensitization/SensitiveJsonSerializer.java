package io.github.genkidoudou.common.desensitization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;

import java.io.IOException;

/**
 * 敏感信息序列化器
 * 用于在 JSON 序列化时对敏感字段进行脱敏处理
 *
 * @author QuickBoot
 * @since 2026/03/02
 */
public class SensitiveJsonSerializer extends JsonSerializer<String> implements ContextualSerializer {

    private SensitiveType type;
    private String strategy;

    public SensitiveJsonSerializer() {
    }

    public SensitiveJsonSerializer(SensitiveType type, String strategy) {
        this.type = type;
        this.strategy = strategy;
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null || value.isEmpty()) {
            gen.writeString(value);
            return;
        }

        String desensitized = DesensitizationUtil.desensitize(value, type, strategy);
        gen.writeString(desensitized);
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
        if (property != null) {
            Sensitive sensitive = property.getAnnotation(Sensitive.class);
            if (sensitive == null) {
                sensitive = property.getContextAnnotation(Sensitive.class);
            }
            if (sensitive != null) {
                return new SensitiveJsonSerializer(sensitive.type(), sensitive.strategy());
            }
        }
        return this;
    }
}
