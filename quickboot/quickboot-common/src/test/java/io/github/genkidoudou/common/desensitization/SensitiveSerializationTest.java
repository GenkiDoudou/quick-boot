package io.github.genkidoudou.common.desensitization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Jackson 写入阶段掩码且不修改 Java 字段值。
 */
class SensitiveSerializationTest {

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new SensitiveJacksonModule());

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
    void serialization_masks_and_leaves_fields_unchanged() throws Exception {
        MobileDto dto = new MobileDto();
        dto.mobile = "13812345678";
        dto.plain = "ok";

        String beforeMobile = dto.mobile;
        JsonNode json = mapper.readTree(mapper.writeValueAsString(dto));

        assertThat(json.get("mobile").asText()).isEqualTo("138****5678");
        assertThat(json.get("plain").asText()).isEqualTo("ok");
        assertThat(dto.mobile).isSameAs(beforeMobile);
        assertThat(dto.mobile).isEqualTo("13812345678");
    }

    @Test
    void password_roundTrip_memory() throws Exception {
        SecretDto dto = new SecretDto();
        dto.pwd = "secret";
        String json = mapper.writeValueAsString(dto);
        assertThat(json).contains("******").doesNotContain("secret");
        assertThat(dto.pwd).isEqualTo("secret");
    }

    /**
     * 非 {@link String} 字段即使误标注解（运行时忽略），应保持反序列化/写出行为不受影响。
     */
    static class LongFieldDto {
        @Sensitive(type = SensitiveType.MOBILE)
        public Long mistaken;
    }

    @Test
    void non_string_is_ignored() throws Exception {
        LongFieldDto dto = new LongFieldDto();
        dto.mistaken = 13812345678L;
        JsonNode json = mapper.readTree(mapper.writeValueAsString(dto));
        assertThat(json.get("mistaken").asLong()).isEqualTo(13812345678L);
    }
}
