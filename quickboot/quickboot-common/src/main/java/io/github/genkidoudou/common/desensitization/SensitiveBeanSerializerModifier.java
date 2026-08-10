package io.github.genkidoudou.common.desensitization;

import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.SerializationConfig;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.introspect.AnnotatedMember;
import tools.jackson.databind.ser.BeanPropertyWriter;
import tools.jackson.databind.ser.ValueSerializerModifier;

import java.util.List;

/**
 * 为标注 {@link Sensitive} 且类型为 {@link String} 的属性替换序列化器，仅影响写出 JSON。
 */
final class SensitiveBeanSerializerModifier extends ValueSerializerModifier {

    @Override
    public List<BeanPropertyWriter> changeProperties(
            SerializationConfig config,
            BeanDescription.Supplier beanDescRef,
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
            ValueSerializer<Object> ser = stringSerializerOf(sensitive);
            writer.assignSerializer(ser);
        }
        return beanProperties;
    }

    @SuppressWarnings("unchecked")
    private static ValueSerializer<Object> stringSerializerOf(Sensitive sensitive) {
        return (ValueSerializer<Object>) (ValueSerializer<?>) new SensitiveStringSerializer(sensitive);
    }
}
