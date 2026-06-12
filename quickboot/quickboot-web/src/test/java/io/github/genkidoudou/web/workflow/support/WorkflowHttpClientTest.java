package io.github.genkidoudou.web.workflow.support;

import io.github.genkidoudou.web.workflow.config.WorkflowProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link WorkflowHttpClient} SSRF 校验单元测试。
 */
class WorkflowHttpClientTest {

    private WorkflowHttpClient client;

    @BeforeEach
    void setUp() {
        WorkflowProperties properties = new WorkflowProperties();
        properties.getHttpRequest().setEnabled(true);
        client = new WorkflowHttpClient(properties);
    }

    @Test
    void validateUrl_publicHttp_ok() {
        assertDoesNotThrow(() -> client.validateUrl("https://example.com/path"));
    }

    @Test
    void validateUrl_loopback_rejected() {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
            () -> client.validateUrl("http://127.0.0.1:8080/admin"));
        assertTrue(ex.getMessage().contains("内网") || ex.getMessage().contains("禁止"));
    }

    @Test
    void validateUrl_privateNetwork_rejected() {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
            () -> client.validateUrl("http://192.168.1.1/internal"));
        assertTrue(ex.getMessage().contains("内网") || ex.getMessage().contains("禁止"));
    }
}
