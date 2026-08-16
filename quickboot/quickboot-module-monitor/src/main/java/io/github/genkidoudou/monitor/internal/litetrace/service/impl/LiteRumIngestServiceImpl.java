package io.github.genkidoudou.monitor.internal.litetrace.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.common.security.utils.LoginUserUtils;
import io.github.genkidoudou.common.security.vo.LoginUser;
import io.github.genkidoudou.monitor.internal.litetrace.dto.RumIngestBo;
import io.github.genkidoudou.monitor.internal.litetrace.entity.SysRumEvent;
import io.github.genkidoudou.monitor.internal.litetrace.entity.SysTraceIndex;
import io.github.genkidoudou.monitor.internal.litetrace.entity.SysTraceSpan;
import io.github.genkidoudou.monitor.internal.litetrace.mapper.SysRumEventMapper;
import io.github.genkidoudou.monitor.internal.litetrace.service.LiteRumIngestService;
import io.github.genkidoudou.monitor.internal.litetrace.service.TraceProjectionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Lite RUM 事件接收实现：校验 appId、限流、落库并投影链路。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LiteRumIngestServiceImpl implements LiteRumIngestService {

    private final SysRumEventMapper rumEventMapper;
    private final TraceProjectionService projectionService;

    /** 逗号分隔；空表示开发期放行全部 */
    @Value("${qc.lite-trace.app-ids:quick-ui}")
    private String appIds;

    /** 单进程简易限流：每分钟最多接收的事件条数（0=不限制） */
    @Value("${qc.lite-trace.ingest-max-events-per-minute:5000}")
    private int maxEventsPerMinute;

    private final AtomicInteger windowCount = new AtomicInteger();
    private volatile long windowStartMs = System.currentTimeMillis();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void ingest(RumIngestBo body, HttpServletRequest request) {
        String appId = body.getAppId().trim();
        assertAppId(appId);
        assertRate(body.getEvents() == null ? 0 : body.getEvents().size());
        String clientIp = clientIp(request);
        String ua = envStr(body.getEnv(), "ua");
        if (StrUtil.isBlank(ua)) {
            ua = StrUtil.blankToDefault(request.getHeader("User-Agent"), "");
        }
        String uin = currentUin();
        for (Map<String, Object> ev : body.getEvents()) {
            if (ev == null) {
                continue;
            }
            String type = str(ev.get("type"));
            if (StrUtil.isBlank(type)) {
                continue;
            }
            SysRumEvent row = new SysRumEvent();
            row.setAppId(appId);
            row.setEventType(type);
            row.setTraceId(blankToNull(str(ev.get("traceId"))));
            row.setOperationId(blankToNull(str(ev.get("operationId"))));
            row.setPagePath(blankToNull(str(ev.get("page"))));
            row.setFromPage(blankToNull(str(ev.get("fromPage"))));
            row.setSessionId(blankToNull(str(ev.get("sessionId"))));
            row.setUin(uin);
            row.setPayloadJson(JSONUtil.toJsonStr(ev));
            row.setClientIp(clientIp);
            row.setUa(ua);
            Long ts = longVal(ev.get("ts"));
            if (ts != null && ts > 0) {
                row.setEventTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(ts), ZoneId.systemDefault()));
            }
            row.setCreateTime(LocalDateTime.now());
            rumEventMapper.insert(row);
            projectEvent(appId, type, ev, clientIp, ua, uin, row.getEventTime());
        }
    }

    private void projectEvent(String appId, String type, Map<String, Object> ev,
                              String clientIp, String ua, String uin, LocalDateTime eventTime) {
        String traceId = str(ev.get("traceId"));
        String operationId = str(ev.get("operationId"));
        String page = str(ev.get("page"));
        String fromPage = str(ev.get("fromPage"));
        String action = str(ev.get("action"));
        String sessionId = str(ev.get("sessionId"));
        String pageVisitId = str(ev.get("pageVisitId"));
        LocalDateTime t = eventTime != null ? eventTime : LocalDateTime.now();

        if ("action".equals(type)) {
            SysTraceIndex idx = baseIndex(appId, traceId, operationId, page, fromPage, clientIp, ua, uin, t, sessionId, pageVisitId);
            idx.setRootSource("browser");
            idx.setActionName(blankToNull(action));
            idx.setEntryName(StrUtil.blankToDefault(StrUtil.blankToDefault(page, action), "action"));
            idx.setCallerName("quick-ui");
            if (StrUtil.isNotBlank(traceId)) {
                projectionService.upsertIndex(idx);
            } else if (StrUtil.isNotBlank(operationId)) {
                // action 可能尚无 trace；用 operation 占位不写 index until api
            }
            if (StrUtil.isBlank(traceId)) {
                return; // span 必须真实 traceId
            }
            SysTraceSpan span = new SysTraceSpan();
            span.setTraceId(traceId);
            span.setSourceType("fe_action");
            span.setSpanName(StrUtil.blankToDefault(action, "action"));
            span.setServiceName("browser");
            span.setStartOffsetMs(0L);
            span.setDurationMs(0L);
            span.setOkFlag("1");
            JSONObject attrs = new JSONObject();
            attrs.set("kind", "action");
            attrs.set("action", action);
            attrs.set("page", page);
            attrs.set("fromPage", fromPage);
            attrs.set("operationId", operationId);
            span.setAttrsJson(attrs.toString());
            projectionService.insertSpan(span);
            return;
        }

        if ("api".equals(type)) {
            if (StrUtil.isBlank(traceId)) {
                return;
            }
            String method = str(ev.get("method"));
            String url = str(ev.get("url"));
            Long duration = longVal(ev.get("durationMs"));
            Integer status = intVal(ev.get("status"));
            boolean ok = boolOk(ev.get("ok"), status);
            SysTraceIndex idx = baseIndex(appId, traceId, operationId, page, fromPage, clientIp, ua, uin, t, sessionId, pageVisitId);
            idx.setRootSource("browser");
            // entry 用接口形态，页面路径单独在 pagePath，便于「接口」列表展示
            idx.setEntryName((StrUtil.blankToDefault(method, "GET") + " " + StrUtil.blankToDefault(url, "")).trim());
            idx.setCallerName("quick-ui");
            idx.setActionName(blankToNull(action));
            idx.setOkFlag(ok ? "1" : "0");
            idx.setStatusCode(status == null ? null : String.valueOf(status));
            idx.setDurationMs(duration == null ? 0L : duration);
            if (!ok) {
                idx.setErrorSummary("API " + idx.getStatusCode());
            }
            projectionService.upsertIndex(idx);

            SysTraceSpan span = new SysTraceSpan();
            span.setTraceId(traceId);
            span.setSourceType("fe_api");
            span.setSpanName(idx.getEntryName());
            span.setServiceName("browser");
            span.setStartOffsetMs(0L);
            span.setDurationMs(duration == null ? 0L : duration);
            span.setOkFlag(ok ? "1" : "0");
            span.setStatusCode(idx.getStatusCode());
            JSONObject attrs = new JSONObject();
            attrs.set("kind", "api");
            attrs.set("method", method);
            attrs.set("url", url);
            attrs.set("query", str(ev.get("query")));
            attrs.set("status", status);
            attrs.set("durationMs", duration);
            attrs.set("page", page);
            attrs.set("fromPage", fromPage);
            attrs.set("bizCode", ev.get("bizCode"));
            attrs.set("bizMsg", str(ev.get("bizMsg")));
            attrs.set("paramsSummary", str(ev.get("paramsSummary")));
            attrs.set("requestParams", str(ev.get("requestParams")));
            attrs.set("requestBody", str(ev.get("requestBody")));
            attrs.set("responsePreview", str(ev.get("responsePreview")));
            span.setAttrsJson(attrs.toString());
            projectionService.insertSpan(span);
            return;
        }

        if ("error".equals(type)) {
            String message = str(ev.get("message"));
            if (StrUtil.isBlank(traceId) && StrUtil.isBlank(operationId)) {
                return;
            }
            if (StrUtil.isNotBlank(traceId)) {
                SysTraceIndex idx = baseIndex(appId, traceId, operationId, page, fromPage, clientIp, ua, uin, t, sessionId, pageVisitId);
                idx.setRootSource("browser");
                idx.setCallerName("quick-ui");
                idx.setEntryName(StrUtil.blankToDefault(page, "error"));
                idx.setOkFlag("0");
                idx.setErrorSummary(StrUtil.maxLength(StrUtil.blankToDefault(message, "js error"), 500));
                projectionService.upsertIndex(idx);

                SysTraceSpan span = new SysTraceSpan();
                span.setTraceId(traceId);
                span.setSourceType("fe_error");
                span.setSpanName(idx.getErrorSummary());
                span.setServiceName("browser");
                span.setStartOffsetMs(0L);
                span.setDurationMs(0L);
                span.setOkFlag("0");
                JSONObject attrs = new JSONObject();
                attrs.set("kind", "error");
                attrs.set("message", message);
                attrs.set("page", page);
                String stack = str(ev.get("stack"));
                if (StrUtil.isNotBlank(stack)) {
                    attrs.set("stack", StrUtil.maxLength(stack, 2000));
                }
                span.setAttrsJson(attrs.toString());
                projectionService.insertSpan(span);
            }
            return;
        }

        if ("pv".equals(type)) {
            // pv 主要带 pageVisitId；通常无 traceId，不写 index（由后续 api 携带 pageVisitId）
            if (StrUtil.isNotBlank(traceId)) {
                SysTraceIndex idx = baseIndex(appId, traceId, operationId, page, fromPage, clientIp, ua, uin, t, sessionId, pageVisitId);
                idx.setRootSource("browser");
                idx.setCallerName("quick-ui");
                idx.setEntryName(StrUtil.blankToDefault(page, "pv"));
                projectionService.upsertIndex(idx);

                SysTraceSpan span = new SysTraceSpan();
                span.setTraceId(traceId);
                span.setSourceType("fe_pv");
                span.setSpanName(StrUtil.blankToDefault(page, "pv"));
                span.setServiceName("browser");
                span.setStartOffsetMs(0L);
                span.setDurationMs(0L);
                span.setOkFlag("1");
                JSONObject attrs = new JSONObject();
                attrs.set("kind", "page");
                attrs.set("page", page);
                attrs.set("fromPage", fromPage);
                attrs.set("sessionId", sessionId);
                attrs.set("pageVisitId", pageVisitId);
                attrs.set("fullPath", str(ev.get("fullPath")));
                attrs.set("title", str(ev.get("title")));
                span.setAttrsJson(attrs.toString());
                projectionService.insertSpan(span);
            }
        }
    }

    private SysTraceIndex baseIndex(String appId, String traceId, String operationId, String page,
                                    String fromPage, String clientIp, String ua, String uin, LocalDateTime t,
                                    String sessionId, String pageVisitId) {
        SysTraceIndex idx = new SysTraceIndex();
        idx.setTraceId(traceId);
        idx.setAppId(appId);
        idx.setOperationId(blankToNull(operationId));
        idx.setPagePath(blankToNull(page));
        idx.setFromPage(blankToNull(fromPage));
        idx.setUin(blankToNull(uin));
        idx.setSessionId(blankToNull(sessionId));
        idx.setPageVisitId(blankToNull(pageVisitId));
        idx.setClientIp(clientIp);
        idx.setUa(ua);
        idx.setStartedAt(t);
        idx.setEndedAt(t);
        idx.setOkFlag("1");
        return idx;
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

    private void assertAppId(String appId) {
        if (StrUtil.isBlank(appIds)) {
            return;
        }
        Set<String> allow = Arrays.stream(appIds.split(","))
            .map(String::trim)
            .filter(StrUtil::isNotBlank)
            .collect(Collectors.toSet());
        if (!allow.contains(appId)) {
            log.warn("非法 appId: received=[{}] allow={} rawConfig=[{}]", appId, allow, appIds);
            throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "非法 appId");
        }
    }

    private void assertRate(int incoming) {
        if (maxEventsPerMinute <= 0 || incoming <= 0) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - windowStartMs >= 60_000L) {
            windowStartMs = now;
            windowCount.set(0);
        }
        if (windowCount.addAndGet(incoming) > maxEventsPerMinute) {
            throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "RUM 上报过于频繁，请稍后重试");
        }
    }

    private static String clientIp(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        String xff = request.getHeader("X-Forwarded-For");
        if (StrUtil.isNotBlank(xff)) {
            return xff.split(",")[0].trim();
        }
        return StrUtil.blankToDefault(request.getRemoteAddr(), "");
    }

    private static String envStr(Map<String, Object> env, String key) {
        if (env == null) {
            return "";
        }
        return str(env.get(key));
    }

    private static String str(Object o) {
        return o == null ? "" : String.valueOf(o).trim();
    }

    private static String blankToNull(String s) {
        return StrUtil.isBlank(s) ? null : s;
    }

    private static Long longVal(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Number n) {
            return n.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(o));
        } catch (Exception e) {
            return null;
        }
    }

    private static Integer intVal(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Number n) {
            return n.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(o));
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean boolOk(Object okObj, Integer status) {
        if (okObj instanceof Boolean b) {
            return b;
        }
        if (okObj != null) {
            String s = String.valueOf(okObj);
            if ("true".equalsIgnoreCase(s) || "1".equals(s)) {
                return true;
            }
            if ("false".equalsIgnoreCase(s) || "0".equals(s)) {
                return false;
            }
        }
        return status != null && status >= 200 && status < 400;
    }
}
