package io.github.genkidoudou.common.desensitization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * 将 {@link String} 写入前经 {@link SensitiveMasking} 掩码；
 * Bean 字段本身不会被赋值（Jackson 仅从 getter/字段读取）。
 */
final class SensitiveStringSerializer extends JsonSerializer<String> {

    private final SensitiveType type;
    private final String strategy;

    SensitiveStringSerializer(Sensitive ann) {
        this.type = ann.type();
        this.strategy = ann.strategy();
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(SensitiveMasking.mask(value, type, strategy));
    }
}
