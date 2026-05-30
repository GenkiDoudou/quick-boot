package io.github.genkidoudou.common.tracing;

import io.github.genkidoudou.common.api.ClientOperationIds;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link ClientOperationFilter}：Header 解析、MDC 写入与请求结束清除。
 */
class ClientOperationFilterTest {

    private final ClientOperationFilter filter = new ClientOperationFilter();

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void validHeaderWrittenToMdc() throws ServletException, IOException {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader(ClientOperationIds.HEADER_NAME, "  op-abc-123  ");
        AtomicReference<String> seen = new AtomicReference<>();
        filter.doFilter(req, new MockHttpServletResponse(), (request, response) -> {
            seen.set(ClientOperationIds.current());
        });
        assertThat(seen.get()).isEqualTo("op-abc-123");
        assertThat(ClientOperationIds.current()).isNull();
    }

    @Test
    void tooLongHeaderDiscarded() throws ServletException, IOException {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader(ClientOperationIds.HEADER_NAME, "a".repeat(65));
        AtomicReference<String> seen = new AtomicReference<>();
        filter.doFilter(req, new MockHttpServletResponse(), (request, response) -> {
            seen.set(ClientOperationIds.current());
        });
        assertThat(seen.get()).isNull();
    }

    @Test
    void mdcClearedAfterRequest() throws ServletException, IOException {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader(ClientOperationIds.HEADER_NAME, "op-xyz");
        filter.doFilter(req, new MockHttpServletResponse(), (request, response) -> {
            // no-op chain
        });
        assertThat(MDC.get(ClientOperationIds.MDC_KEY)).isNull();
    }
}
