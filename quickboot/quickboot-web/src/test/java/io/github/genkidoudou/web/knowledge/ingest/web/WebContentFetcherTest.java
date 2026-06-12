package io.github.genkidoudou.web.knowledge.ingest.web;

import io.github.genkidoudou.web.knowledge.config.KnowledgeProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link WebContentFetcher} SSRF 与 URL 校验用例。
 */
class WebContentFetcherTest {

    private WebContentFetcher fetcher;

    @BeforeEach
    void setUp() {
        KnowledgeProperties properties = new KnowledgeProperties();
        KnowledgeProperties.WebFetch webFetch = properties.getWebFetch();
        webFetch.setEnabled(true);
        webFetch.setTimeoutMs(5_000);
        webFetch.setMaxBytes(1024);
        fetcher = new WebContentFetcher(properties);
    }

    @Test
    void blocksLocalhostUrl() {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
            () -> fetcher.validateUrl("http://localhost/admin"));
        assertTrue(ex.getMessage().contains("内网") || ex.getMessage().contains("本地"));
    }

    @Test
    void blocksPrivateIpv4Literal() {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
            () -> fetcher.validateUrl("http://192.168.1.10/internal"));
        assertTrue(ex.getMessage().contains("内网") || ex.getMessage().contains("本地"));
    }

    @Test
    void blocksLoopbackLiteral() {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
            () -> fetcher.validateUrl("http://127.0.0.1/secret"));
        assertTrue(ex.getMessage().contains("内网") || ex.getMessage().contains("本地"));
    }

    @Test
    void blocksNonHttpScheme() {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
            () -> fetcher.validateUrl("file:///etc/passwd"));
        assertTrue(ex.getMessage().contains("http"));
    }

    @Test
    void allowsPublicHttpUrlHostValidation() {
        assertDoesNotThrow(() -> fetcher.validateUrl("https://example.com/article"));
    }

    @Test
    void isBlockedAddressDetectsSiteLocal() throws Exception {
        InetAddress privateAddr = InetAddress.getByName("10.0.0.1");
        assertTrue(WebContentFetcher.isBlockedAddress(privateAddr));
    }

    @Test
    void isBlockedAddressAllowsPublic() throws Exception {
        InetAddress publicAddr = InetAddress.getByName("8.8.8.8");
        assertFalse(WebContentFetcher.isBlockedAddress(publicAddr));
    }
}
