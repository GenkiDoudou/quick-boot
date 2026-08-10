package io.github.genkidoudou.common.desensitization;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

/**
 * 将 {@link String} 写入前经 {@link SensitiveMasking} 掩码；
 * 仅当 {@link SensitiveResponseContext#isActive()} 为 true 时掩码，否则原样写出。
 * Bean 字段本身不会被赋值。
 */
final class SensitiveStringSerializer extends ValueSerializer<String> {

  private final SensitiveType type;
  private final String strategy;

  SensitiveStringSerializer(Sensitive ann) {
    this.type = ann.type();
    this.strategy = ann.strategy();
  }

  @Override
  public void serialize(String value, JsonGenerator gen, SerializationContext serializers) {
    if (!SensitiveResponseContext.isActive()) {
      gen.writeString(value);
      return;
    }
    gen.writeString(SensitiveMasking.mask(value, type, strategy));
  }
}
