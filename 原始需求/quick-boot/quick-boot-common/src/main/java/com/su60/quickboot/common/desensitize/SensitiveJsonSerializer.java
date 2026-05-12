package com.su60.quickboot.common.desensitize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;

import java.io.IOException;

/**
 * Jackson 脱敏序列化器。
 */
public class SensitiveJsonSerializer extends JsonSerializer<String> implements ContextualSerializer {

    private Sensitive sensitive;

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        gen.writeString(desensitize(value));
    }

    private String desensitize(String value) {
        if (sensitive == null) {
            return value;
        }
        SensitiveType type = sensitive.type();
        return switch (type) {
            case MOBILE_PHONE -> DesensitizeUtils.mobile(value);
            case ID_CARD -> DesensitizeUtils.idCard(value);
            case BANK_CARD -> DesensitizeUtils.bankCard(value);
            case REAL_NAME -> DesensitizeUtils.realName(value);
            case EMAIL -> DesensitizeUtils.email(value);
            case ADDRESS -> DesensitizeUtils.address(value);
            case CUSTOM -> DesensitizeUtils.custom(value, sensitive.prefixKeep(), sensitive.suffixKeep(),
                    sensitive.maskChar());
        };
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property)
            throws JsonMappingException {
        if (property != null) {
            Sensitive annotation = property.getAnnotation(Sensitive.class);
            if (annotation == null) {
                annotation = property.getContextAnnotation(Sensitive.class);
            }
            if (annotation != null) {
                SensitiveJsonSerializer serializer = new SensitiveJsonSerializer();
                serializer.sensitive = annotation;
                return serializer;
            }
        }
        return this;
    }
}
