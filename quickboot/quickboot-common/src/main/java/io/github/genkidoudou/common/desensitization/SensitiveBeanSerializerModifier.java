package io.github.genkidoudou.common.desensitization;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.JavaType;

import java.util.List;

/**
 * 为标注 {@link Sensitive} 且类型为 {@link String} 的属性替换序列化器，仅影响写出 JSON。
 */
final class SensitiveBeanSerializerModifier extends BeanSerializerModifier {

    @Override
    public List<BeanPropertyWriter> changeProperties(
            SerializationConfig config,
            BeanDescription beanDesc,
            List<BeanPropertyWriter> beanProperties) {

        if (beanProperties == null || beanProperties.isEmpty()) {
            return beanProperties;
        }
        for (BeanPropertyWriter writer : beanProperties) {
            JavaType t = writer.getType();
            if (t == null || !String.class.equals(t.getRawClass())) {
                continue;
            }
            AnnotatedMember member = writer.getMember();
            if (member == null) {
                continue;
            }
            Sensitive sensitive = member.getAnnotation(Sensitive.class);
            if (sensitive == null) {
                continue;
            }
            JsonSerializer<Object> ser = stringSerializerOf(sensitive);
            writer.assignSerializer(ser);
        }
        return beanProperties;
    }

    @SuppressWarnings("unchecked")
    private static JsonSerializer<Object> stringSerializerOf(Sensitive sensitive) {
        return (JsonSerializer<Object>) (JsonSerializer<?>) new SensitiveStringSerializer(sensitive);
    }
}
