package io.github.genkidoudou.common.i18n;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import cn.hutool.extra.spring.SpringUtil;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

@TestClassOrder(ClassOrderer.OrderAnnotation.class)
class I18nUtilTest {

    @Nested
    @Order(1)
    @SpringJUnitConfig(NoMessageSourceConfig.class)
    class WithoutMessageSource {

        @Test
        void usesBuiltinOrExplicitWhenMessageSourceBeanAbsent() {
            assertThat(I18nUtil.getMessage("30402")).isEqualTo("Host 不允许");
            assertThat(I18nUtil.getMessage("any.code")).isEqualTo("操作失败，请稍后再试");
            assertThat(I18nUtil.getMessage("any.code", new Object[]{}, "兜底")).isEqualTo("兜底");
        }
    }

    @Nested
    @Order(2)
    @SpringJUnitConfig(WithMessageSourceConfig.class)
    class WithMessageSource {

        @BeforeEach
        void setZhCnLocale() {
            LocaleContextHolder.setLocale(Locale.SIMPLIFIED_CHINESE);
        }

        @AfterEach
        void resetLocale() {
            LocaleContextHolder.resetLocaleContext();
        }

        @Test
        void returnsZhCnMessageWhenKeyExists() {
            assertThat(I18nUtil.getMessage("demo.msg")).isEqualTo("演示文案");
        }

        @Test
        void formatsArgs() {
            assertThat(I18nUtil.getMessage("demo.arg", new Object[]{"世界"})).isEqualTo("你好，世界");
        }

        @Test
        void missingKeyReturnsGenericOrBuiltin() {
            assertThat(I18nUtil.getMessage("30402")).isEqualTo("Host 不允许");
            assertThat(I18nUtil.getMessage("missing.code")).isEqualTo("操作失败，请稍后再试");
        }

        @Test
        void missingKeyUsesDefaultMessage() {
            assertThat(I18nUtil.getMessage("missing.code", null, "兜底")).isEqualTo("兜底");
        }

        @Test
        void localeGetterSetterRoundTrip() {
            I18nUtil.setLocale(Locale.US);
            assertThat(I18nUtil.getLocale()).isEqualTo(Locale.US);
        }
    }

    @Configuration
    @Import(SpringUtil.class)
    static class NoMessageSourceConfig {

        @Bean
        String ctxPlaceholder() {
            return "";
        }
    }

    @Configuration
    @Import(SpringUtil.class)
    static class WithMessageSourceConfig {

        @Bean
        MessageSource messageSource() {
            ReloadableResourceBundleMessageSource source = new ReloadableResourceBundleMessageSource();
            source.setBasename("classpath:i18n/messages");
            source.setDefaultEncoding(StandardCharsets.UTF_8.name());
            source.setFallbackToSystemLocale(false);
            return source;
        }
    }
}
