package io.github.genkidoudou.common.servlet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.genkidoudou.common.api.HttpCodes;
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
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import cn.hutool.extra.spring.SpringUtil;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

@TestClassOrder(ClassOrderer.OrderAnnotation.class)
class ServletUtilsTest {

    private static final ObjectMapper TEST_MAPPER = new ObjectMapper();

    @Nested
    @Order(1)
    @SpringJUnitConfig(NoMessageSourceConfig.class)
    class WithoutMessageSource {

        @Test
        void writeJson_usesKeyAsMsgWhenMessageSourceAbsent() throws Exception {
            MockHttpServletResponse resp = new MockHttpServletResponse();
            ServletUtils.writeResponse(resp, 40301);

            assertThat(resp.getStatus()).isEqualTo(200);
            assertThat(resp.getContentType()).contains("application/json");
            assertThat(resp.getCharacterEncoding()).isEqualToIgnoringCase("UTF-8");

            JsonNode root = TEST_MAPPER.readTree(resp.getContentAsString(StandardCharsets.UTF_8));
            assertThat(root.get("code").asInt()).isEqualTo(40301);
            assertThat(root.get("msg").asText()).isEqualTo("40301");
        }

        @Test
        void nullCode_fallsBackTo500() throws Exception {
            MockHttpServletResponse resp = new MockHttpServletResponse();
            ServletUtils.writeResponse(resp, null);

            JsonNode root = TEST_MAPPER.readTree(resp.getContentAsString(StandardCharsets.UTF_8));
            assertThat(root.get("code").asInt()).isEqualTo(HttpCodes.INTERNAL_ERROR);
            assertThat(root.get("msg").asText()).isEqualTo("500");
        }
    }

    @Nested
    @Order(2)
    @SpringJUnitConfig(WithMessageSourceConfig.class)
    class WithMessageSource {

        @BeforeEach
        void zhCn() {
            LocaleContextHolder.setLocale(Locale.SIMPLIFIED_CHINESE);
        }

        @AfterEach
        void resetLocale() {
            LocaleContextHolder.resetLocaleContext();
        }

        @Test
        void msgFromI18nWhenKeyExists() throws Exception {
            MockHttpServletResponse resp = new MockHttpServletResponse();
            ServletUtils.writeResponse(resp, 40301);

            JsonNode root = TEST_MAPPER.readTree(resp.getContentAsString(StandardCharsets.UTF_8));
            assertThat(root.get("code").asInt()).isEqualTo(40301);
            assertThat(root.get("msg").asText()).isEqualTo("拒绝访问");
        }

        @Test
        void msgFormatsArgs() throws Exception {
            MockHttpServletResponse resp = new MockHttpServletResponse();
            ServletUtils.writeResponse(resp, 40302, "x");

            JsonNode root = TEST_MAPPER.readTree(resp.getContentAsString(StandardCharsets.UTF_8));
            assertThat(root.get("code").asInt()).isEqualTo(40302);
            assertThat(root.get("msg").asText()).isEqualTo("非法：x");
        }

        @Test
        void missingKeyFallsBackToCodeString() throws Exception {
            MockHttpServletResponse resp = new MockHttpServletResponse();
            ServletUtils.writeResponse(resp, 999999);

            JsonNode root = TEST_MAPPER.readTree(resp.getContentAsString(StandardCharsets.UTF_8));
            assertThat(root.get("msg").asText()).isEqualTo("999999");
        }

        @Test
        void missingKey_usesFallbackMessageWhenProvided() throws Exception {
            MockHttpServletResponse resp = new MockHttpServletResponse();
            ServletUtils.writeResponse(resp, 999999, "禁止访问");

            JsonNode root = TEST_MAPPER.readTree(resp.getContentAsString(StandardCharsets.UTF_8));
            assertThat(root.get("msg").asText()).isEqualTo("禁止访问");
        }

        @Test
        void existingKey_doesNotUseFallbackMessage() throws Exception {
            MockHttpServletResponse resp = new MockHttpServletResponse();
            ServletUtils.writeResponse(resp, 40301, "禁止访问");

            JsonNode root = TEST_MAPPER.readTree(resp.getContentAsString(StandardCharsets.UTF_8));
            assertThat(root.get("msg").asText()).isEqualTo("拒绝访问");
        }
    }

    @Nested
    @Order(3)
    @SpringJUnitConfig(WithMessageSourceConfig.class)
    class LocaleSwitch {

        @AfterEach
        void resetLocale() {
            LocaleContextHolder.resetLocaleContext();
        }

        @Test
        void usesEnglishWhenLocaleIsUs() throws Exception {
            LocaleContextHolder.setLocale(Locale.US);
            MockHttpServletResponse resp = new MockHttpServletResponse();
            ServletUtils.writeResponse(resp, 40301);

            JsonNode root = TEST_MAPPER.readTree(resp.getContentAsString(StandardCharsets.UTF_8));
            assertThat(root.get("msg").asText()).isEqualTo("Forbidden");
        }
    }

    @Nested
    @Order(4)
    @SpringJUnitConfig(NoMessageSourceConfig.class)
    class CommittedResponse {

        @Test
        void doesNotWriteWhenAlreadyCommitted() throws Exception {
            MockHttpServletResponse resp = new MockHttpServletResponse();
            resp.getWriter().print("partial");
            resp.flushBuffer();
            assertThat(resp.isCommitted()).isTrue();

            ServletUtils.writeResponse(resp, 40301);
            assertThat(resp.getContentAsString()).isEqualTo("partial");
        }
    }

    @Configuration
    @Import(SpringUtil.class)
    static class NoMessageSourceConfig {

        @Bean
        String ctxPlaceholder() {
            return "";
        }

        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
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

        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }
}
