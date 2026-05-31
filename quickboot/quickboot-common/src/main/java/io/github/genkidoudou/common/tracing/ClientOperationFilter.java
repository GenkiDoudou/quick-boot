package io.github.genkidoudou.common.tracing;

import io.github.genkidoudou.common.api.ClientIds;
import io.github.genkidoudou.common.api.ClientOperationIds;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 读取 {@link ClientOperationIds#HEADER_NAME}、{@link ClientIds#HEADER_NAME} 写入对应 MDC，请求结束清除。
 * <p>
 * 与 Micrometer {@link io.github.genkidoudou.common.api.TraceIds} 解耦：不修改 trace 上下文。
 */
public class ClientOperationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws ServletException, IOException {
        String opId = ClientOperationIds.normalizeHeader(request.getHeader(ClientOperationIds.HEADER_NAME));
        if (opId != null) {
            MDC.put(ClientOperationIds.MDC_KEY, opId);
        }
        String clientId = ClientIds.normalizeHeader(request.getHeader(ClientIds.HEADER_NAME));
        if (clientId != null) {
            MDC.put(ClientIds.MDC_KEY, clientId);
        }
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(ClientOperationIds.MDC_KEY);
            MDC.remove(ClientIds.MDC_KEY);
        }
    }
}
