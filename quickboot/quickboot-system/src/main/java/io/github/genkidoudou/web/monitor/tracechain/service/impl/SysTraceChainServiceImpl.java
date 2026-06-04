package io.github.genkidoudou.web.monitor.tracechain.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.web.monitor.clienttrack.domain.SysClientTrack;
import io.github.genkidoudou.web.monitor.clienttrack.dto.ClientTrackPageFlowEdgeVo;
import io.github.genkidoudou.web.monitor.clienttrack.dto.ClientTrackPageVisitNodeVo;
import io.github.genkidoudou.web.monitor.clienttrack.dto.ClientTrackTimelineQueryBo;
import io.github.genkidoudou.web.monitor.clienttrack.dto.ClientTrackTimelineVo;
import io.github.genkidoudou.web.monitor.clienttrack.mapper.SysClientTrackMapper;
import io.github.genkidoudou.web.monitor.clienttrack.service.SysClientTrackService;
import io.github.genkidoudou.web.monitor.clienttrack.support.ClientTrackMenuPathResolver;
import io.github.genkidoudou.web.monitor.clienttrack.support.ClientTrackMenuPathResolver.MenuMatch;
import io.github.genkidoudou.web.monitor.tracechain.dto.TraceChainBackendNodeVo;
import io.github.genkidoudou.web.monitor.tracechain.dto.TraceChainBehaviorEventVo;
import io.github.genkidoudou.web.monitor.tracechain.dto.TraceChainBehaviorPageVo;
import io.github.genkidoudou.web.monitor.tracechain.dto.TraceChainGraphVo;
import io.github.genkidoudou.web.monitor.tracechain.dto.TraceChainPageJumpVo;
import io.github.genkidoudou.web.monitor.tracechain.dto.TraceChainQueryBo;
import io.github.genkidoudou.web.monitor.tracechain.dto.TraceChainSummaryVo;
import io.github.genkidoudou.web.monitor.tracechain.service.SysTraceChainService;
import io.github.genkidoudou.web.system.operlog.domain.SysOperLog;
import io.github.genkidoudou.web.system.operlog.mapper.SysOperLogMapper;
import io.github.genkidoudou.web.system.slowsql.domain.SysSlowSql;
import io.github.genkidoudou.web.system.slowsql.mapper.SysSlowSqlMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 聚合 sys_client_track、sys_oper_log、sys_slow_sql 构建全链路图。
 */
@Service
@RequiredArgsConstructor
public class SysTraceChainServiceImpl implements SysTraceChainService {

    private static final int MAX_TRACK_BATCHES = 500;
    private static final int MAX_OPER_LOG = 200;
    private static final int MAX_SLOW_SQL = 200;

    private final SysClientTrackMapper clientTrackMapper;
    private final SysOperLogMapper operLogMapper;
    private final SysSlowSqlMapper slowSqlMapper;
    private final SysClientTrackService clientTrackService;
    private final ClientTrackMenuPathResolver menuPathResolver;
    private final ObjectMapper objectMapper;

    @Override
    public TraceChainGraphVo graph(TraceChainQueryBo query) {
        validateQuery(query);
        List<SysClientTrack> tracks = loadTracks(query);
        if (tracks.isEmpty()) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "未找到匹配的监控数据");
        }
        boolean truncated = tracks.size() >= MAX_TRACK_BATCHES;
        if (truncated) {
            tracks = tracks.subList(0, MAX_TRACK_BATCHES);
        }

        long anchorEpochMs = resolveAnchorEpochMs(tracks);
        TraceChainGraphVo vo = new TraceChainGraphVo();
        vo.setTruncated(truncated);
        if (truncated) {
            vo.getWarnings().add("监控批次超过 " + MAX_TRACK_BATCHES + " 条，已截断；请缩小时间范围或使用 operationId/traceId");
        }

        ClientTrackTimelineVo timeline = buildTimelineForTracks(query, tracks);
        vo.setPageJumps(buildPageJumps(timeline, anchorEpochMs));
        List<TraceChainBehaviorPageVo> behaviorByPage = buildBehaviorByPage(tracks, anchorEpochMs);
        vo.setBehaviorByPage(behaviorByPage);
        Set<String> traceIds = collectTraceIds(behaviorByPage);
        String operationId = resolvePrimaryOperationId(query, tracks);
        vo.setBackendNodes(buildBackendNodes(behaviorByPage, operationId, traceIds, anchorEpochMs));
        vo.setSummary(buildSummary(query, tracks, timeline, vo, operationId));
        vo.setTimelineMaxMs(computeTimelineMaxMs(vo));
        return vo;
    }

    private static void validateQuery(TraceChainQueryBo query) {
        if (query == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "查询条件不能为空");
        }
        boolean hasKey = StrUtil.isNotBlank(query.getOperationId())
            || StrUtil.isNotBlank(query.getTraceId())
            || query.getBatchId() != null
            || StrUtil.isNotBlank(query.getPageVisitId());
        boolean hasSession = StrUtil.isNotBlank(query.getBrowserVisitId())
            || StrUtil.isNotBlank(query.getSessionId())
            || StrUtil.isNotBlank(query.getUserName());
        if (!hasKey && !hasSession) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM,
                "请填写 operationId、traceId、batchId、pageVisitId 或 browserVisitId/sessionId/userName");
        }
    }

    private List<SysClientTrack> loadTracks(TraceChainQueryBo query) {
        if (StrUtil.isNotBlank(query.getBrowserVisitId()) || StrUtil.isNotBlank(query.getSessionId())
            || StrUtil.isNotBlank(query.getUserName())) {
            ClientTrackTimelineQueryBo tq = new ClientTrackTimelineQueryBo();
            tq.setBrowserVisitId(query.getBrowserVisitId());
            tq.setSessionId(query.getSessionId());
            tq.setUserName(query.getUserName());
            tq.setBeginDate(query.getBeginDate());
            tq.setEndDate(query.getEndDate());
            ClientTrackTimelineVo timeline = clientTrackService.timeline(tq);
            return loadTracksByTimeline(timeline, query);
        }
        LambdaQueryWrapper<SysClientTrack> w = Wrappers.lambdaQuery();
        if (StrUtil.isNotBlank(query.getOperationId())) {
            w.eq(SysClientTrack::getOperationId, query.getOperationId().trim());
        }
        if (query.getBatchId() != null) {
            w.eq(SysClientTrack::getBatchId, query.getBatchId());
        }
        if (StrUtil.isNotBlank(query.getPageVisitId())) {
            w.eq(SysClientTrack::getPageVisitId, query.getPageVisitId().trim());
        }
        if (StrUtil.isNotBlank(query.getTraceId())) {
            applyTraceIdFilter(w, query.getTraceId().trim());
        }
        applyDateRange(w, query);
        w.orderByAsc(SysClientTrack::getCreateTime);
        w.last("LIMIT " + (MAX_TRACK_BATCHES + 1));
        return clientTrackMapper.selectList(w);
    }

    private List<SysClientTrack> loadTracksByTimeline(ClientTrackTimelineVo timeline, TraceChainQueryBo query) {
        Set<Long> batchIds = new LinkedHashSet<>();
        if (timeline.getSessions() != null) {
            timeline.getSessions().forEach(session -> {
                if (session.getPages() == null) {
                    return;
                }
                session.getPages().forEach(page -> collectBatchIdsFromPage(page, batchIds));
            });
        } else if (timeline.getPages() != null) {
            timeline.getPages().forEach(page -> collectBatchIdsFromPage(page, batchIds));
        }
        if (batchIds.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<SysClientTrack> w = Wrappers.lambdaQuery();
        w.in(SysClientTrack::getBatchId, batchIds);
        if (StrUtil.isNotBlank(query.getOperationId())) {
            w.eq(SysClientTrack::getOperationId, query.getOperationId().trim());
        }
        if (StrUtil.isNotBlank(query.getTraceId())) {
            applyTraceIdFilter(w, query.getTraceId().trim());
        }
        w.orderByAsc(SysClientTrack::getCreateTime);
        return clientTrackMapper.selectList(w);
    }

    private static void collectBatchIdsFromPage(ClientTrackPageVisitNodeVo page, Set<Long> batchIds) {
        if (page.getPageVisitBatch() != null && page.getPageVisitBatch().getBatchId() != null) {
            batchIds.add(page.getPageVisitBatch().getBatchId());
        }
        if (page.getActions() != null) {
            page.getActions().forEach(a -> {
                if (a.getBatchId() != null) {
                    batchIds.add(a.getBatchId());
                }
            });
        }
    }

    private void applyTraceIdFilter(LambdaQueryWrapper<SysClientTrack> w, String traceId) {
        String opId = resolveOperationIdByTrace(traceId);
        if (StrUtil.isNotBlank(opId)) {
            w.eq(SysClientTrack::getOperationId, opId);
            return;
        }
        w.and(n -> n.eq(SysClientTrack::getTraceId, traceId)
            .or().like(SysClientTrack::getEventsJson, traceId));
    }

    private String resolveOperationIdByTrace(String traceId) {
        SysOperLog log = operLogMapper.selectOne(Wrappers.<SysOperLog>lambdaQuery()
            .eq(SysOperLog::getTraceId, traceId)
            .last("LIMIT 1"));
        if (log != null && StrUtil.isNotBlank(log.getClientOperationId())) {
            return log.getClientOperationId().trim();
        }
        SysSlowSql slow = slowSqlMapper.selectOne(Wrappers.<SysSlowSql>lambdaQuery()
            .eq(SysSlowSql::getTraceId, traceId)
            .last("LIMIT 1"));
        if (slow != null && StrUtil.isNotBlank(slow.getClientOperationId())) {
            return slow.getClientOperationId().trim();
        }
        return "";
    }

    private static void applyDateRange(LambdaQueryWrapper<SysClientTrack> w, TraceChainQueryBo query) {
        if (query.getBeginDate() != null) {
            w.ge(SysClientTrack::getCreateTime, query.getBeginDate().atStartOfDay());
        }
        if (query.getEndDate() != null) {
            w.le(SysClientTrack::getCreateTime, query.getEndDate().atTime(LocalTime.MAX));
        }
    }

    private ClientTrackTimelineVo buildTimelineForTracks(TraceChainQueryBo query, List<SysClientTrack> tracks) {
        ClientTrackTimelineQueryBo tq = new ClientTrackTimelineQueryBo();
        SysClientTrack first = tracks.get(0);
        tq.setBrowserVisitId(StrUtil.blankToDefault(query.getBrowserVisitId(), first.getBrowserVisitId()));
        tq.setSessionId(StrUtil.blankToDefault(query.getSessionId(), first.getSessionId()));
        tq.setUserName(StrUtil.blankToDefault(query.getUserName(), first.getUserName()));
        tq.setBeginDate(query.getBeginDate());
        tq.setEndDate(query.getEndDate());
        if (StrUtil.isAllBlank(tq.getBrowserVisitId(), tq.getSessionId(), tq.getUserName())) {
            tq.setSessionId(first.getSessionId());
            tq.setBrowserVisitId(first.getBrowserVisitId());
            tq.setUserName(first.getUserName());
        }
        try {
            return clientTrackService.timeline(tq);
        } catch (WarningException ex) {
            return minimalTimeline(tracks);
        }
    }

    private static ClientTrackTimelineVo minimalTimeline(List<SysClientTrack> tracks) {
        ClientTrackTimelineVo vo = new ClientTrackTimelineVo();
        vo.setUserName(tracks.get(0).getUserName());
        vo.setSessionId(tracks.get(0).getSessionId());
        vo.setBrowserVisitId(tracks.get(0).getBrowserVisitId());
        return vo;
    }

    private List<TraceChainPageJumpVo> buildPageJumps(ClientTrackTimelineVo timeline, long anchorEpochMs) {
        List<ClientTrackPageFlowEdgeVo> edges = new ArrayList<>();
        if (timeline.getPageFlowEdges() != null) {
            edges.addAll(timeline.getPageFlowEdges());
        }
        if (edges.isEmpty() && timeline.getSessions() != null) {
            for (var session : timeline.getSessions()) {
                if (session.getPageFlowEdges() != null) {
                    edges.addAll(session.getPageFlowEdges());
                }
            }
        }
        if (edges.isEmpty()) {
            return List.of();
        }
        List<TraceChainPageJumpVo> jumps = new ArrayList<>();
        int step = 1;
        for (ClientTrackPageFlowEdgeVo edge : edges) {
            TraceChainPageJumpVo j = new TraceChainPageJumpVo();
            j.setStep(step++);
            j.setFromLabel(StrUtil.blankToDefault(edge.getFromMenuLabel(), edge.getFromPagePath()));
            j.setFromPath(edge.getFromPagePath());
            j.setToLabel(StrUtil.blankToDefault(edge.getToMenuLabel(), edge.getToPagePath()));
            j.setToPath(edge.getToPagePath());
            j.setPageVisitId(edge.getToPageVisitId());
            boolean samePath = Objects.equals(normalizePath(edge.getFromPagePath()), normalizePath(edge.getToPagePath()));
            j.setJumpLabel(samePath ? "同页新访问批" : "页面跳转");
            long atMs = 0L;
            if (edge.getAtTime() != null) {
                atMs = Math.max(0L, edge.getAtTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() - anchorEpochMs);
            }
            j.setAtMs(atMs);
            j.setAtLabel("+" + atMs + "ms");
            jumps.add(j);
        }
        return jumps;
    }

    private List<TraceChainBehaviorPageVo> buildBehaviorByPage(List<SysClientTrack> tracks, long anchorEpochMs) {
        Map<String, List<SysClientTrack>> grouped = new LinkedHashMap<>();
        for (SysClientTrack row : tracks) {
            String key = StrUtil.blankToDefault(row.getPageVisitId(), "__unknown__");
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(row);
        }
        List<String> paths = tracks.stream().map(SysClientTrack::getPagePath).distinct().toList();
        Map<String, MenuMatch> menuByPath = menuPathResolver.resolveBatch(paths);

        List<TraceChainBehaviorPageVo> pages = new ArrayList<>();
        for (Map.Entry<String, List<SysClientTrack>> entry : grouped.entrySet()) {
            entry.getValue().sort(Comparator.comparing(SysClientTrack::getCreateTime,
                Comparator.nullsLast(Comparator.naturalOrder())));
            SysClientTrack head = entry.getValue().get(0);
            TraceChainBehaviorPageVo page = new TraceChainBehaviorPageVo();
            page.setPageVisitId("__unknown__".equals(entry.getKey()) ? "" : entry.getKey());
            page.setPagePath(head.getPagePath());
            MenuMatch match = menuByPath.get(normalizePathKey(head.getPagePath()));
            if (match == null) {
                match = menuPathResolver.resolve(head.getPagePath());
            }
            if (match != null) {
                page.setMenuName(match.breadcrumb());
            } else {
                page.setMenuName(StrUtil.blankToDefault(head.getPagePath(), "未知页面"));
            }
            List<TraceChainBehaviorEventVo> events = new ArrayList<>();
            for (SysClientTrack batch : entry.getValue()) {
                List<Map<String, Object>> rawEvents = parseEventsJson(batch.getEventsJson());
                for (int i = 0; i < rawEvents.size(); i++) {
                    events.add(toBehaviorEvent(rawEvents.get(i), batch, i, anchorEpochMs));
                }
            }
            events.sort(Comparator.comparing(TraceChainBehaviorEventVo::getStartMs,
                Comparator.nullsLast(Comparator.naturalOrder())));
            page.setEvents(events);
            pages.add(page);
        }
        pages.sort(Comparator.comparing(p -> p.getEvents().isEmpty() ? 0L : p.getEvents().get(0).getStartMs(),
            Comparator.nullsLast(Comparator.naturalOrder())));
        return pages;
    }

    private TraceChainBehaviorEventVo toBehaviorEvent(Map<String, Object> ev, SysClientTrack batch, int index,
            long anchorEpochMs) {
        TraceChainBehaviorEventVo item = new TraceChainBehaviorEventVo();
        item.setId(batch.getBatchId() + "-ev-" + index);
        String type = String.valueOf(ev.getOrDefault("type", ""));
        item.setType(type);
        item.setLabel(buildEventLabel(ev));
        long ts = readTs(ev);
        long cost = readCost(ev);
        item.setStartMs(Math.max(0L, ts - anchorEpochMs));
        item.setEndMs(item.getStartMs() + Math.max(cost, type.startsWith("api") ? cost : 20L));
        item.setStatus(resolveEventStatus(type, ev));
        Object trace = ev.get("serverTraceId");
        if (trace == null) {
            trace = ev.get("responseTraceId");
        }
        if (trace != null) {
            item.setTraceId(String.valueOf(trace).trim());
        }
        if (StrUtil.isNotBlank(batch.getOperationId())) {
            item.setOperationId(batch.getOperationId());
        }
        Object op = ev.get("operationId");
        if (op != null && StrUtil.isNotBlank(String.valueOf(op))) {
            item.setOperationId(String.valueOf(op).trim());
        }
        item.setPassive(isPassiveClick(ev, batch));
        item.setPageVisitId(batch.getPageVisitId());
        item.setBatchId(batch.getBatchId());
        return item;
    }

    private static boolean isPassiveClick(Map<String, Object> ev, SysClientTrack batch) {
        if (!"click".equals(String.valueOf(ev.get("type")))) {
            return false;
        }
        String trigger = batch.getTriggerAction();
        if (StrUtil.isBlank(trigger)) {
            return true;
        }
        Object target = ev.get("target");
        return target == null || StrUtil.isBlank(String.valueOf(target));
    }

    private static String resolveEventStatus(String type, Map<String, Object> ev) {
        if ("api_error".equals(type) || "js_error".equals(type) || "promise_error".equals(type)) {
            return "error";
        }
        if ("api_slow".equals(type)) {
            return "warn";
        }
        Object httpStatus = ev.get("httpStatus");
        if (httpStatus instanceof Number n && n.intValue() >= 400) {
            return "error";
        }
        return "ok";
    }

    private List<TraceChainBackendNodeVo> buildBackendNodes(List<TraceChainBehaviorPageVo> behaviorByPage,
            String operationId, Set<String> traceIds, long anchorEpochMs) {
        List<TraceChainBackendNodeVo> nodes = new ArrayList<>();

        for (TraceChainBehaviorPageVo page : behaviorByPage) {
            for (TraceChainBehaviorEventVo ev : page.getEvents()) {
                if (!isApiEventType(ev.getType()) || StrUtil.isBlank(ev.getTraceId())) {
                    continue;
                }
                String tid = ev.getTraceId().trim();
                boolean exists = nodes.stream().anyMatch(n -> "api".equals(n.getType()) && tid.equals(n.getTraceId()));
                if (exists) {
                    continue;
                }
                TraceChainBackendNodeVo api = new TraceChainBackendNodeVo();
                api.setId(apiNodeId(tid));
                api.setType("api");
                api.setLabel(ev.getLabel());
                api.setStartMs(ev.getStartMs());
                api.setEndMs(ev.getEndMs());
                api.setStatus(ev.getStatus());
                api.setTraceId(tid);
                api.setRequestMethod(parseMethod(ev.getLabel()));
                api.setHttpStatus("error".equals(ev.getStatus()) ? 500 : 200);
                nodes.add(api);
            }
        }

        List<SysOperLog> operLogs = loadOperLogs(operationId, traceIds);
        for (SysOperLog log : operLogs) {
            TraceChainBackendNodeVo node = new TraceChainBackendNodeVo();
            node.setId("oper-" + log.getOperId());
            node.setType("oper_log");
            node.setLabel(StrUtil.blankToDefault(log.getTitle(), "操作日志") + " · " + shortUrl(log.getOperUrl()));
            node.setTraceId(log.getTraceId());
            node.setOperId(log.getOperId());
            node.setParentApiId(apiNodeId(log.getTraceId()));
            long[] range = timeRangeFromLocal(log.getOperTime(), log.getCostTime(), anchorEpochMs);
            node.setStartMs(range[0]);
            node.setEndMs(range[1]);
            node.setStatus(log.getStatus() != null && log.getStatus() == 1 ? "error" : "ok");
            nodes.add(node);
        }

        List<SysSlowSql> slowList = loadSlowSqls(operationId, traceIds);
        for (SysSlowSql slow : slowList) {
            TraceChainBackendNodeVo node = new TraceChainBackendNodeVo();
            node.setId("sql-" + slow.getSlowId());
            node.setType("slow_sql");
            node.setLabel(shortSql(slow.getSqlText()));
            node.setTraceId(slow.getTraceId());
            node.setSlowId(slow.getSlowId());
            node.setParentApiId(apiNodeId(slow.getTraceId()));
            long cost = slow.getCostTime() == null ? 0L : slow.getCostTime();
            long start = Math.max(0L, slow.getCreateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                - anchorEpochMs - cost);
            node.setStartMs(start);
            node.setEndMs(start + cost);
            node.setStatus(cost >= 1000 ? "warn" : "ok");
            nodes.add(node);
        }

        nodes.sort(Comparator.comparing(TraceChainBackendNodeVo::getStartMs, Comparator.nullsLast(Comparator.naturalOrder())));
        return nodes;
    }

    private List<SysOperLog> loadOperLogs(String operationId, Set<String> traceIds) {
        LambdaQueryWrapper<SysOperLog> w = Wrappers.lambdaQuery();
        if (StrUtil.isNotBlank(operationId)) {
            w.eq(SysOperLog::getClientOperationId, operationId.trim());
        } else if (!traceIds.isEmpty()) {
            w.in(SysOperLog::getTraceId, traceIds);
        } else {
            return List.of();
        }
        w.orderByAsc(SysOperLog::getOperTime);
        w.last("LIMIT " + MAX_OPER_LOG);
        return operLogMapper.selectList(w);
    }

    private List<SysSlowSql> loadSlowSqls(String operationId, Set<String> traceIds) {
        LambdaQueryWrapper<SysSlowSql> w = Wrappers.lambdaQuery();
        if (StrUtil.isNotBlank(operationId)) {
            w.eq(SysSlowSql::getClientOperationId, operationId.trim());
        } else if (!traceIds.isEmpty()) {
            w.in(SysSlowSql::getTraceId, traceIds);
        } else {
            return List.of();
        }
        w.orderByAsc(SysSlowSql::getCreateTime);
        w.last("LIMIT " + MAX_SLOW_SQL);
        return slowSqlMapper.selectList(w);
    }

    private TraceChainSummaryVo buildSummary(TraceChainQueryBo query, List<SysClientTrack> tracks,
            ClientTrackTimelineVo timeline, TraceChainGraphVo graph, String operationId) {
        SysClientTrack focus = pickFocusTrack(query, tracks, operationId);
        TraceChainSummaryVo s = new TraceChainSummaryVo();
        s.setUserName(focus.getUserName());
        s.setPagePath(focus.getPagePath());
        s.setSessionId(focus.getSessionId());
        s.setBrowserVisitId(focus.getBrowserVisitId());
        s.setOperationId(operationId);
        s.setTriggerAction(focus.getTriggerAction());
        MenuMatch match = menuPathResolver.resolve(focus.getPagePath());
        if (match != null) {
            s.setMenuBreadcrumb(match.breadcrumb());
        }
        int apiCount = (int) graph.getBackendNodes().stream().filter(n -> "api".equals(n.getType())).count();
        s.setApiCount(apiCount);
        s.setPageJumpCount(graph.getPageJumps().size());
        int beh = graph.getBehaviorByPage().stream().mapToInt(p -> p.getEvents().size()).sum();
        s.setBehaviorEventCount(beh);
        s.setStatus(resolveOverallStatus(graph));
        return s;
    }

    private static String resolveOverallStatus(TraceChainGraphVo graph) {
        boolean err = graph.getBackendNodes().stream().anyMatch(n -> "error".equals(n.getStatus()))
            || graph.getBehaviorByPage().stream().flatMap(p -> p.getEvents().stream()).anyMatch(e -> "error".equals(e.getStatus()));
        if (err) {
            return "error";
        }
        boolean warn = graph.getBackendNodes().stream().anyMatch(n -> "warn".equals(n.getStatus()))
            || graph.getBehaviorByPage().stream().flatMap(p -> p.getEvents().stream()).anyMatch(e -> "warn".equals(e.getStatus()));
        return warn ? "warn" : "ok";
    }

    private static SysClientTrack pickFocusTrack(TraceChainQueryBo query, List<SysClientTrack> tracks, String operationId) {
        if (StrUtil.isNotBlank(operationId)) {
            for (SysClientTrack t : tracks) {
                if (operationId.equals(t.getOperationId())) {
                    return t;
                }
            }
        }
        if (query.getBatchId() != null) {
            for (SysClientTrack t : tracks) {
                if (query.getBatchId().equals(t.getBatchId())) {
                    return t;
                }
            }
        }
        return tracks.get(tracks.size() - 1);
    }

    private static String resolvePrimaryOperationId(TraceChainQueryBo query, List<SysClientTrack> tracks) {
        if (StrUtil.isNotBlank(query.getOperationId())) {
            return query.getOperationId().trim();
        }
        for (int i = tracks.size() - 1; i >= 0; i--) {
            if (StrUtil.isNotBlank(tracks.get(i).getOperationId())) {
                return tracks.get(i).getOperationId().trim();
            }
        }
        return "";
    }

    private static Set<String> collectTraceIds(List<TraceChainBehaviorPageVo> pages) {
        Set<String> ids = new LinkedHashSet<>();
        for (TraceChainBehaviorPageVo page : pages) {
            for (TraceChainBehaviorEventVo ev : page.getEvents()) {
                if (StrUtil.isNotBlank(ev.getTraceId())) {
                    ids.add(ev.getTraceId().trim());
                }
            }
        }
        return ids;
    }

    private static long computeTimelineMaxMs(TraceChainGraphVo vo) {
        long max = 0L;
        for (TraceChainPageJumpVo j : vo.getPageJumps()) {
            max = Math.max(max, j.getAtMs() == null ? 0L : j.getAtMs());
        }
        for (TraceChainBehaviorPageVo p : vo.getBehaviorByPage()) {
            for (TraceChainBehaviorEventVo e : p.getEvents()) {
                max = Math.max(max, e.getEndMs() == null ? 0L : e.getEndMs());
            }
        }
        for (TraceChainBackendNodeVo n : vo.getBackendNodes()) {
            max = Math.max(max, n.getEndMs() == null ? 0L : n.getEndMs());
        }
        return Math.max(max, 1000L);
    }

    private long resolveAnchorEpochMs(List<SysClientTrack> tracks) {
        long min = Long.MAX_VALUE;
        for (SysClientTrack row : tracks) {
            for (Map<String, Object> ev : parseEventsJson(row.getEventsJson())) {
                long ts = readTs(ev);
                if (ts > 0 && ts < min) {
                    min = ts;
                }
            }
            if (row.getCreateTime() != null) {
                long ct = row.getCreateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                if (ct < min) {
                    min = ct;
                }
            }
        }
        if (min == Long.MAX_VALUE) {
            return System.currentTimeMillis();
        }
        return min;
    }

    private static long[] timeRangeFromLocal(LocalDateTime operTime, Long costTime, long anchorEpochMs) {
        if (operTime == null) {
            return new long[] {0L, 0L};
        }
        long start = Math.max(0L, operTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() - anchorEpochMs);
        long cost = costTime == null ? 0L : costTime;
        return new long[] {start, start + Math.max(cost, 1L)};
    }

    private static String apiNodeId(String traceId) {
        return "api-" + (traceId == null ? "unknown" : traceId.replaceAll("[^a-zA-Z0-9]", ""));
    }

    private static boolean isApiEventType(String type) {
        return "api_call".equals(type) || "api_slow".equals(type) || "api_error".equals(type);
    }

    private static String parseMethod(String label) {
        if (StrUtil.isBlank(label)) {
            return "GET";
        }
        int sp = label.indexOf(' ');
        return sp > 0 ? label.substring(0, sp).trim().toUpperCase() : "GET";
    }

    private static String shortUrl(String url) {
        if (StrUtil.isBlank(url)) {
            return "";
        }
        return url.length() > 48 ? url.substring(0, 46) + "…" : url;
    }

    private static String shortSql(String sql) {
        if (StrUtil.isBlank(sql)) {
            return "SQL";
        }
        String one = sql.replaceAll("\\s+", " ").trim();
        return one.length() > 64 ? one.substring(0, 62) + "…" : one;
    }

    private static String normalizePath(String path) {
        return normalizePathKey(path);
    }

    private static String normalizePathKey(String raw) {
        if (StrUtil.isBlank(raw)) {
            return "";
        }
        String p = raw.trim();
        if (!p.startsWith("/")) {
            p = "/" + p;
        }
        while (p.length() > 1 && p.endsWith("/")) {
            p = p.substring(0, p.length() - 1);
        }
        return p;
    }

    private static long readTs(Map<String, Object> ev) {
        Object ts = ev.get("ts");
        if (ts instanceof Number n) {
            return n.longValue();
        }
        return 0L;
    }

    private static long readCost(Map<String, Object> ev) {
        Object cost = ev.get("cost");
        if (cost instanceof Number n) {
            return Math.max(0L, n.longValue());
        }
        return 0L;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseEventsJson(String eventsJson) {
        if (StrUtil.isBlank(eventsJson)) {
            return List.of();
        }
        try {
            List<?> raw = objectMapper.readValue(eventsJson, List.class);
            List<Map<String, Object>> out = new ArrayList<>();
            for (Object item : raw) {
                if (item instanceof Map<?, ?> map) {
                    out.add((Map<String, Object>) map);
                }
            }
            return out;
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }

    private static String buildEventLabel(Map<String, Object> ev) {
        String type = String.valueOf(ev.getOrDefault("type", ""));
        if ("click".equals(type)) {
            Object target = ev.get("target");
            return target != null ? "点击 " + target : "点击";
        }
        if ("api_call".equals(type) || "api_slow".equals(type) || "api_error".equals(type)) {
            String method = ev.get("method") != null ? String.valueOf(ev.get("method")).toUpperCase() : "GET";
            String url = ev.get("url") != null ? String.valueOf(ev.get("url")) : "";
            return method + " " + url;
        }
        if ("route_enter".equals(type)) {
            Object path = ev.get("path");
            return path != null ? "route_enter " + path : "route_enter";
        }
        return type;
    }
}
