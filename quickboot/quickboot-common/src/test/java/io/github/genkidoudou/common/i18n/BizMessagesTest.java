package io.github.genkidoudou.common.i18n;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BizMessagesTest {

    @Test
    void resolve_usesBuiltinWhenMessageSourceMissing() {
        assertThat(BizMessages.resolve("30402", null, null, null)).isEqualTo("Host 不允许");
    }

    @Test
    void resolve_prefersMessageSourceWhenMeaningful() {
        assertThat(BizMessages.resolve("30402", null, "Host 不允许", null)).isEqualTo("Host 不允许");
    }

    @Test
    void resolve_ignoresMessageSourceWhenOnlyCode() {
        assertThat(BizMessages.resolve("30402", null, "30402", null)).isEqualTo("Host 不允许");
    }

    @Test
    void resolve_formatsSensitiveWordArg() {
        assertThat(BizMessages.resolve("30501", new Object[] {"测试词"}, null, null))
            .isEqualTo("内容包含敏感词：测试词");
    }

    @Test
    void resolve_usesExplicitFallbackForUnknownCode() {
        assertThat(BizMessages.resolve("999999", null, null, "自定义禁止")).isEqualTo("自定义禁止");
    }
}
