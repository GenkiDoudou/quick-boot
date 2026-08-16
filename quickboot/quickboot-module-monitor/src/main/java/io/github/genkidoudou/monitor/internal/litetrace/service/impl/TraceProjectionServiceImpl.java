package io.github.genkidoudou.monitor.internal.litetrace.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONObject;
import io.github.genkidoudou.common.security.utils.LoginUserUtils;
import io.github.genkidoudou.common.security.vo.LoginUser;
import io.github.genkidoudou.monitor.internal.litetrace.entity.SysTraceIndex;
import io.github.genkidoudou.monitor.internal.litetrace.entity.SysTraceSpan;
import io.github.genkidoudou.monitor.internal.litetrace.mapper.SysTraceIndexMapper;
import io.github.genkidoudou.monitor.internal.litetrace.mapper.SysTraceSpanMapper;
import io.github.genkidoudou.monitor.internal.litetrace.service.TraceProjectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 链路投影实现：索引 upsert、span 插入及 access/sql/error 场景投影。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TraceProjectionServiceImpl implements TraceProjectionService {

    private static final int SQL_MAX = 4000;
    private static final int ERR_MAX = 500;

    private final SysTraceIndexMapper traceIndexMapper;
    private final SysTraceSpanMapper traceSpanMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void upsertIndex(SysTraceIndex patch) {
        if (patch == null || StrUtil.isBlank(patch.getTraceId())) {
            return;
        }
        String tid = patch.getTraceId().trim();
        SysTraceIndex exist = traceIndexMapper.selectById(tid);
        LocalDateTime now = LocalDateTime.now();
        if (exist == null) {
            if (StrUtil.isBlank(patch.getAppId())) {
                patch.setAppId("");
            }
            if (StrUtil.isBlank(patch.getRootSource())) {
                patch.setRootSource("browser");
            }
            if (StrUtil.isBlank(patch.getEntryName())) {
                patch.setEntryName("");
            }
            if (StrUtil.isBlank(patch.getCallerName())) {
                patch.setCallerName("");
            }
            if (StrUtil.isBlank(patch.getOkFlag())) {
                patch.setOkFlag("1");
            }
            if (patch.getDurationMs() == null) {
                patch.setDurationMs(0L);
            }
            patch.setCreateTime(now);
            patch.setUpdateTime(now);
            if (patch.getStartedAt() == null) {
                patch.setStartedAt(now);
            }
            if (patch.getEndedAt() == null) {
                patch.setEndedAt(now);
            }
            traceIndexMapper.insert(patch);
            return;
        }
        mergeIndex(exist, patch, now);
        traceIndexMapper.updateById(exist);
    }

    @Override
    public void insertSpan(SysTraceSpan span) {
        if (span == null || StrUtil.isBlank(span.getTraceId())) {
            return;
        }
        if (StrUtil.isBlank(span.getOkFlag())) {
            span.setOkFlag("1");
        }
        if (span.getStartOffsetMs() == null) {
            span.setStartOffsetMs(0L);
        }
        if (span.getDurationMs() == null) {
            span.setDurationMs(0L);
        }
        if (StrUtil.isBlank(span.getSourceType())) {
            span.setSourceType("");
        }
        if (StrUtil.isBlank(span.getSpanName())) {
            span.setSpanName("");
        }
        if (StrUtil.isBlank(span.getServiceName())) {
            span.setServiceName("");
        }
        span.setCreateTime(LocalDateTime.now());
        try {
            traceSpanMapper.insert(span);
        } catch (Exception ex) {
            log.warn("insert trace span failed: {}", ex.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void projectAccess(String traceId, String appId, String rootSource, String entry, String caller,
                              String operationId, String method, String uri, int status, long durationMs,
                              String clientIp, String ua, LocalDateTime startedAt) {
        if (StrUtil.isBlank(traceId)) {
            return;
        }
        boolean ok = status >= 200 && status < 400;
        SysTraceIndex idx = new SysTraceIndex();
        idx.setTraceId(traceId.trim());
        idx.setAppId(StrUtil.blankToDefault(appId, ""));
        idx.setRootSource(StrUtil.blankToDefault(rootSource, "api"));
        idx.setEntryName(StrUtil.blankToDefault(entry, method + " " + uri));
        idx.setCallerName(StrUtil.blankToDefault(caller, ""));
        idx.setOperationId(StrUtil.blankToDefault(operationId, null));
        idx.setOkFlag(ok ? "1" : "0");
        idx.setStatusCode(String.valueOf(status));
        idx.setDurationMs(Math.max(durationMs, 0L));
        idx.setStartedAt(startedAt != null ? startedAt : LocalDateTime.now());
        idx.setEndedAt(LocalDateTime.now());
        idx.setClientIp(clientIp);
        idx.setUa(ua);
        String uin = currentUin();
        if (StrUtil.isNotBlank(uin)) {
            idx.setUin(uin);
        }
        if (!ok) {
            idx.setErrorSummary("HTTP " + status);
        }
        upsertIndex(idx);

        SysTraceSpan span = new SysTraceSpan();
        span.setTraceId(traceId.trim());
        span.setSourceType("service");
        span.setSpanName(method + " " + uri);
        span.setServiceName("quickboot-app");
        span.setStartOffsetMs(0L);
        span.setDurationMs(Math.max(durationMs, 0L));
        span.setOkFlag(ok ? "1" : "0");
        span.setStatusCode(String.valueOf(status));
        JSONObject attrs = new JSONObject();
        attrs.set("kind", "api");
        attrs.set("method", method);
        attrs.set("url", uri);
        attrs.set("status", status);
        attrs.set("durationMs", durationMs);
        span.setAttrsJson(attrs.toString());
        insertSpan(span);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void projectSql(String traceId, String sqlText, long costMs, String mapperId) {
        if (StrUtil.isBlank(traceId)) {
            return;
        }
        String raw = StrUtil.blankToDefault(sqlText, "");
        String truncated = raw.length() > SQL_MAX ? raw.substring(0, SQL_MAX) : raw;
        String fp = DigestUtil.md5Hex(raw);
        SysTraceSpan span = new SysTraceSpan();
        span.setTraceId(traceId.trim());
        span.setSourceType("sql");
        span.setSpanName(StrUtil.blankToDefault(mapperId, "SQL"));
        span.setServiceName("mysql");
        span.setStartOffsetMs(0L);
        span.setDurationMs(Math.max(costMs, 0L));
        span.setOkFlag("1");
        JSONObject attrs = new JSONObject();
        attrs.set("kind", "sql");
        attrs.set("fingerprint", fp);
        attrs.set("mapperId", mapperId);
        attrs.set("sql", truncated);
        attrs.set("durationMs", costMs);
        span.setAttrsJson(attrs.toString());
        insertSpan(span);

        SysTraceIndex exist = traceIndexMapper.selectById(traceId.trim());
        if (exist != null && (exist.getDurationMs() == null || exist.getDurationMs() < costMs)) {
            // keep index duration as max observed access; sql alone does not redefine root
            exist.setUpdateTime(LocalDateTime.now());
            traceIndexMapper.updateById(exist);
        } else if (exist == null) {
            SysTraceIndex idx = new SysTraceIndex();
            idx.setTraceId(traceId.trim());
            idx.setRootSource("api");
            idx.setEntryName(StrUtil.blankToDefault(mapperId, "SQL"));
            idx.setCallerName("");
            idx.setOkFlag("1");
            idx.setDurationMs(Math.max(costMs, 0L));
            upsertIndex(idx);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void projectBeError(String traceId, String summary, String stackSnippet) {
        if (StrUtil.isBlank(traceId)) {
            return;
        }
        String sum = StrUtil.maxLength(StrUtil.blankToDefault(summary, "backend error"), ERR_MAX);
        SysTraceSpan span = new SysTraceSpan();
        span.setTraceId(traceId.trim());
        span.setSourceType("be_error");
        span.setSpanName(sum);
        span.setServiceName("quickboot-app");
        span.setStartOffsetMs(0L);
        span.setDurationMs(0L);
        span.setOkFlag("0");
        if (StrUtil.isNotBlank(stackSnippet)) {
            span.setAttrsJson("{\"stack\":\"" + escapeJson(StrUtil.maxLength(stackSnippet, 1000)) + "\"}");
        }
        insertSpan(span);

        SysTraceIndex idx = new SysTraceIndex();
        idx.setTraceId(traceId.trim());
        idx.setOkFlag("0");
        idx.setErrorSummary(sum);
        idx.setEndedAt(LocalDateTime.now());
        upsertIndex(idx);
    }

    private static void mergeIndex(SysTraceIndex exist, SysTraceIndex patch, LocalDateTime now) {
        if (StrUtil.isNotBlank(patch.getAppId())) {
            exist.setAppId(patch.getAppId());
        }
        if (StrUtil.isNotBlank(patch.getRootSource())) {
            exist.setRootSource(patch.getRootSource());
        }
        if (StrUtil.isNotBlank(patch.getEntryName())) {
            exist.setEntryName(patch.getEntryName());
        }
        if (StrUtil.isNotBlank(patch.getCallerName())) {
            exist.setCallerName(patch.getCallerName());
        }
        if (StrUtil.isNotBlank(patch.getOperationId())) {
            exist.setOperationId(patch.getOperationId());
        }
        if (StrUtil.isNotBlank(patch.getActionName())) {
            exist.setActionName(patch.getActionName());
        }
        if (StrUtil.isNotBlank(patch.getPagePath())) {
            exist.setPagePath(patch.getPagePath());
        }
        if (StrUtil.isNotBlank(patch.getFromPage())) {
            exist.setFromPage(patch.getFromPage());
        }
        if (StrUtil.isNotBlank(patch.getUin())) {
            exist.setUin(patch.getUin());
        }
        if (StrUtil.isNotBlank(patch.getSessionId())) {
            exist.setSessionId(patch.getSessionId());
        }
        if (StrUtil.isNotBlank(patch.getPageVisitId())) {
            exist.setPageVisitId(patch.getPageVisitId());
        }
        if ("0".equals(patch.getOkFlag())) {
            exist.setOkFlag("0");
        }
        if (StrUtil.isNotBlank(patch.getStatusCode())) {
            exist.setStatusCode(patch.getStatusCode());
        }
        if (patch.getDurationMs() != null && patch.getDurationMs() > 0) {
            long cur = exist.getDurationMs() == null ? 0L : exist.getDurationMs();
            exist.setDurationMs(Math.max(cur, patch.getDurationMs()));
        }
        if (patch.getStartedAt() != null && (exist.getStartedAt() == null || patch.getStartedAt().isBefore(exist.getStartedAt()))) {
            exist.setStartedAt(patch.getStartedAt());
        }
        if (patch.getEndedAt() != null) {
            exist.setEndedAt(patch.getEndedAt());
        }
        if (StrUtil.isNotBlank(patch.getClientIp())) {
            exist.setClientIp(patch.getClientIp());
        }
        if (StrUtil.isNotBlank(patch.getUa())) {
            exist.setUa(patch.getUa());
        }
        if (StrUtil.isNotBlank(patch.getErrorSummary())) {
            exist.setErrorSummary(patch.getErrorSummary());
        }
        exist.setUpdateTime(now);
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }

    private static String currentUin() {
        LoginUser user = LoginUserUtils.getLoginUser();
        if (user == null) {
            return null;
        }
        if (StrUtil.isNotBlank(user.getUsername())) {
            return user.getUsername().trim();
        }
        return user.getUserId() == null ? null : String.valueOf(user.getUserId());
    }
}
