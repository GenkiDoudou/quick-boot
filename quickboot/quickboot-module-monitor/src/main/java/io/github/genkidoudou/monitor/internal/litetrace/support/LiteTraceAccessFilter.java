package io.github.genkidoudou.monitor.internal.litetrace.support;

import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.common.api.ClientOperationIds;
import io.github.genkidoudou.common.api.TraceIds;
import io.github.genkidoudou.monitor.internal.litetrace.service.TraceProjectionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 读取或生成 {@code X-Trace-Id}，写入 MDC，并在请求结束时投影 access → service span。
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 6)
@RequiredArgsConstructor
public class LiteTraceAccessFilter extends OncePerRequestFilter {

    public static final String HEADER_NAME = "X-Trace-Id";
    public static final String ATTR_TRACE_ID = "qc.liteTrace.traceId";
    public static final String ATTR_OWNED = "qc.liteTrace.ownedMdc";

    private final TraceProjectionService projectionService;

    @Value("${qc.lite-trace.access-project-enabled:true}")
    private boolean accessProjectEnabled;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        String path = request.getRequestURI();
        boolean skipProject = shouldSkipProject(path);
        String incoming = firstNonBlank(
            request.getHeader(HEADER_NAME),
            request.getHeader("x-trace-id"),
            extractFromTraceparent(request.getHeader("traceparent")));
        String existing = TraceIds.current();
        String traceId = StrUtil.isNotBlank(incoming) ? incoming.trim()
            : (StrUtil.isNotBlank(existing) ? existing : generateId());
        boolean owned = StrUtil.isBlank(existing) || !traceId.equals(existing);
        if (owned) {
            MDC.put(TraceIds.MDC_KEY, traceId);
            request.setAttribute(ATTR_OWNED, Boolean.TRUE);
        }
        request.setAttribute(ATTR_TRACE_ID, traceId);
        response.setHeader(HEADER_NAME, traceId);

        long startNs = System.nanoTime();
        LocalDateTime startedAt = LocalDateTime.now();
        try {
            filterChain.doFilter(request, response);
        } finally {
            try {
                if (accessProjectEnabled && !skipProject) {
                    int status = response.getStatus();
                    long durationMs = Math.max((System.nanoTime() - startNs) / 1_000_000L, 0L);
                    String opId = ClientOperationIds.current();
                    String method = request.getMethod();
                    String uri = path;
                    String clientIp = clientIp(request);
                    String ua = StrUtil.maxLength(StrUtil.blankToDefault(request.getHeader("User-Agent"), ""), 500);
                    String rootSource = guessRootSource(path);
                    projectionService.projectAccess(
                        traceId,
                        "",
                        rootSource,
                        method + " " + uri,
                        rootSource.equals("browser") ? "quick-ui" : "",
                        opId,
                        method,
                        uri,
                        status,
                        durationMs,
                        clientIp,
                        ua,
                        startedAt);
                }
            } catch (Exception ex) {
                log.warn("lite-trace access project failed: {}", ex.getMessage());
            } finally {
                if (Boolean.TRUE.equals(request.getAttribute(ATTR_OWNED))) {
                    MDC.remove(TraceIds.MDC_KEY);
                }
            }
        }
    }

    private static boolean shouldSkipProject(String path) {
        if (path == null) {
            return true;
        }
        return path.contains("/monitor/liteTrace/rum/ingest")
            || path.startsWith("/actuator");
    }

    private static String guessRootSource(String path) {
        if (path != null && (path.startsWith("/openapi") || path.startsWith("/open-api"))) {
            return "api";
        }
        return "browser";
    }

    private static String generateId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private static String extractFromTraceparent(String tp) {
        if (StrUtil.isBlank(tp)) {
            return null;
        }
        String[] parts = tp.trim().split("-");
        if (parts.length >= 3 && parts[1].length() >= 16) {
            return parts[1];
        }
        return null;
    }

    private static String firstNonBlank(String... vals) {
        if (vals == null) {
            return null;
        }
        for (String v : vals) {
            if (StrUtil.isNotBlank(v)) {
                return v;
            }
        }
        return null;
    }

    private static String clientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (StrUtil.isNotBlank(xff)) {
            return xff.split(",")[0].trim();
        }
        return StrUtil.blankToDefault(request.getRemoteAddr(), "");
    }
}
