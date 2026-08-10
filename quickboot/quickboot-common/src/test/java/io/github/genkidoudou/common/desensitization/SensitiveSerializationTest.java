package io.github.genkidoudou.common.desensitization;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Jackson 写入阶段掩码且不修改 Java 字段值；无 {@link SensitiveResponse} 时不掩码。
 */
class SensitiveSerializationTest {

  private final ObjectMapper mapper = JsonMapper.builder()
      .addModule(new SensitiveJacksonModule())
      .build();

  @AfterEach
  void tearDown() {
    SensitiveResponseContext.clear();
  }

  static class MobileDto {
    @Sensitive(type = SensitiveType.MOBILE)
    public String mobile;

    public String plain;
  }

  static class SecretDto {
    @Sensitive(type = SensitiveType.PASSWORD)
    public String pwd;
  }

  @Test
  void withoutSensitiveResponse_noMask() throws Exception {
    SensitiveResponseContext.disableForTest();
    MobileDto dto = new MobileDto();
    dto.mobile = "13812345678";
    JsonNode json = mapper.readTree(mapper.writeValueAsString(dto));
    assertEquals("13812345678", json.get("mobile").asString());
  }

  @Test
  void serialization_masks_and_leaves_fields_unchanged() throws Exception {
    SensitiveResponseContext.enableForTest();
    MobileDto dto = new MobileDto();
    dto.mobile = "13812345678";
    dto.plain = "ok";

    String beforeMobile = dto.mobile;
    JsonNode json = mapper.readTree(mapper.writeValueAsString(dto));

    assertEquals("138****5678", json.get("mobile").asString());
    assertEquals("ok", json.get("plain").asString());
    assertSame(beforeMobile, dto.mobile);
    assertEquals("13812345678", dto.mobile);
  }

  @Test
  void password_roundTrip_memory() throws Exception {
    SensitiveResponseContext.enableForTest();
    SecretDto dto = new SecretDto();
    dto.pwd = "secret";
    String json = mapper.writeValueAsString(dto);
    assertTrue(json.contains("******"));
    assertFalse(json.contains("secret"));
    assertEquals("secret", dto.pwd);
  }

  static class LongFieldDto {
    @Sensitive(type = SensitiveType.MOBILE)
    public Long mistaken;
  }

  @Test
  void non_string_is_ignored() throws Exception {
    SensitiveResponseContext.enableForTest();
    LongFieldDto dto = new LongFieldDto();
    dto.mistaken = 13812345678L;
    JsonNode json = mapper.readTree(mapper.writeValueAsString(dto));
    assertEquals(13812345678L, json.get("mistaken").asLong());
  }
}
