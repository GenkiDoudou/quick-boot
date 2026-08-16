package io.github.genkidoudou.common.monitor.slowsql;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.common.api.ClientIds;
import io.github.genkidoudou.common.api.ClientOperationIds;
import io.github.genkidoudou.common.api.TraceIds;
import io.github.genkidoudou.common.monitor.operlog.OperLogPublishingAspect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.AntPathMatcher;

import java.time.LocalDateTime;
import java.util.Locale;

/**
 * JDBC 层慢 SQL 判定、来源解析与事件发布。
 */
@Slf4j
@RequiredArgsConstructor
public class SlowSqlCaptureSupport {

    private final ApplicationEventPublisher eventPublisher;
    private final SlowSqlProperties properties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * 若耗时超过阈值则发布 {@link SlowSqlCapturedEvent}。
     *
     * @param sql        执行 SQL
     * @param costTimeMs 耗时（毫秒）
     */
    public void captureIfSlow(String sql, long costTimeMs) {
        if (!properties.isCaptureEnabled() || costTimeMs < properties.getThresholdMs()) {
            return;
        }
        if (shouldIgnoreSql(sql)) {
            SlowSqlMapperContext.clear();
            return;
        }
        String mapperId = SlowSqlMapperContext.getAndClear();
        String requestUri = OperLogPublishingAspect.currentRequestUri();
        String sqlSource = resolveSqlSource(requestUri);
        String sqlText = truncateSql(sql);
        String sqlType = SlowSqlTypeResolver.resolve(sqlText);
        SlowSqlCapturePayload payload = SlowSqlCapturePayload.builder()
            .sqlSource(sqlSource)
            .sqlType(sqlType)
            .mapperId(mapperId)
            .sqlText(sqlText)
            .costTimeMs(costTimeMs)
            .traceId(TraceIds.current())
            .clientOperationId(ClientOperationIds.current())
            .clientId(ClientIds.current())
            .requestMethod(OperLogPublishingAspect.currentRequestMethod())
            .requestUri(requestUri)
            .operName(currentOperName())
            .createTime(LocalDateTime.now())
            .build();
        if (properties.isLogEnabled()) {
            log.warn("slow sql {}ms type={} source={} traceId={} uri={} mapper={} sql={}",
                costTimeMs, sqlType, sqlSource, payload.getTraceId(), requestUri, mapperId, sqlText);
        }
        try {
            eventPublisher.publishEvent(new SlowSqlCapturedEvent(payload));
        } catch (Exception e) {
            log.warn("publish SlowSqlCapturedEvent failed: {}", e.getMessage());
        }
    }

    private boolean shouldIgnoreSql(String sql) {
        if (StrUtil.isBlank(sql)) {
            return true;
        }
        String lower = sql.toLowerCase(Locale.ROOT);
        for (String needle : properties.getIgnoreSqlContains()) {
            if (StrUtil.isNotBlank(needle) && lower.contains(needle.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private String resolveSqlSource(String requestUri) {
        if (StrUtil.isBlank(requestUri)) {
            return SlowSqlSource.SYSTEM;
        }
        for (String prefix : properties.getJimuUriPrefixes()) {
            if (StrUtil.isBlank(prefix)) {
                continue;
            }
            String pattern = prefix.endsWith("/") ? prefix + "**" : prefix + "/**";
            if (pathMatcher.match(pattern, requestUri) || requestUri.startsWith(prefix)) {
                return SlowSqlSource.JIMU;
            }
        }
        return SlowSqlSource.BUSINESS;
    }

    /**
     * 截断落库 SQL，保留格式化换行（不再压成单行），便于列表与详情阅读。
     * 最终串（含截断后缀）长度不超过 {@code maxSqlLength}，避免超出列宽。
     */
    private String truncateSql(String sql) {
        if (sql == null) {
            return "";
        }
        String trimmed = sql.trim();
        int max = Math.max(256, properties.getMaxSqlLength());
        if (trimmed.length() <= max) {
            return trimmed;
        }
        String suffix = "\n-- ... truncated";
        int keep = Math.max(0, max - suffix.length());
        return trimmed.substring(0, keep) + suffix;
    }

    private static String currentOperName() {
        try {
            if (!StpUtil.isLogin()) {
                return "";
            }
            Object loginId = StpUtil.getLoginIdDefaultNull();
            return loginId == null ? "" : String.valueOf(loginId);
        } catch (Exception e) {
            return "";
        }
    }
}
