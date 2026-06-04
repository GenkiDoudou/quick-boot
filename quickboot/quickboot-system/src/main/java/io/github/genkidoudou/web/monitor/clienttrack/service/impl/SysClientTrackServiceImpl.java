package io.github.genkidoudou.web.monitor.clienttrack.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.web.monitor.clienttrack.domain.SysClientTrack;
import io.github.genkidoudou.web.monitor.clienttrack.dto.ClientTrackReportBo;
import io.github.genkidoudou.web.monitor.clienttrack.dto.ClientTrackTimelinePageQueryBo;
import io.github.genkidoudou.web.monitor.clienttrack.dto.ClientTrackTimelineQueryBo;
import io.github.genkidoudou.web.monitor.clienttrack.dto.ClientTrackTimelineVo;
import io.github.genkidoudou.web.monitor.clienttrack.dto.ClientTrackActionNodeVo;
import io.github.genkidoudou.web.monitor.clienttrack.dto.ClientTrackEventItemVo;
import io.github.genkidoudou.web.monitor.clienttrack.dto.ClientTrackPageFlowEdgeVo;
import io.github.genkidoudou.web.monitor.clienttrack.dto.ClientTrackPageVisitNodeVo;
import io.github.genkidoudou.web.monitor.clienttrack.dto.ClientTrackSessionNodeVo;
import io.github.genkidoudou.web.monitor.clienttrack.dto.SysClientTrackQueryBo;
import io.github.genkidoudou.web.monitor.clienttrack.dto.SysClientTrackVo;
import io.github.genkidoudou.web.monitor.clienttrack.mapper.SysClientTrackMapper;
import io.github.genkidoudou.web.monitor.clienttrack.support.ClientTrackMenuPathResolver;
import io.github.genkidoudou.web.monitor.clienttrack.support.ClientTrackMenuPathResolver.MenuMatch;
import io.github.genkidoudou.web.monitor.clienttrack.service.SysClientTrackService;
import io.github.genkidoudou.web.system.user.domain.SysUser;
import io.github.genkidoudou.web.system.user.mapper.SysUserMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 前端用户行为监控批次服务实现。
 */
@Service
@RequiredArgsConstructor
public class SysClientTrackServiceImpl implements SysClientTrackService {

    private static final int MAX_EVENTS_JSON_LEN = 65535;

    /** 行为轨迹概览最多加载批次数，超出提示缩小时间范围 */
    private static final int MAX_TIMELINE_BATCHES = 500;

    /** 单页明细最多加载批次数，防止单页异常膨胀 */
    private static final int MAX_PAGE_DETAIL_BATCHES = 200;

    private static final String UNKNOWN_PAGE_VISIT_KEY = "__unknown__";

    private final SysClientTrackMapper mapper;
    private final SysUserMapper userMapper;
    private final ObjectMapper objectMapper;
    private final ClientTrackMenuPathResolver menuPathResolver;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void report(ClientTrackReportBo body, HttpServletRequest request) {
        StpUtil.checkLogin();
        long userId = StpUtil.getLoginIdAsLong();
        SysUser user = userMapper.selectById(userId);
        String userName = user != null ? StrUtil.blankToDefault(user.getUserName(), "") : "";

        List<Map<String, Object>> events = body.getEvents();
        String operationId = resolveOperationId(body.getOperationId(), events);
        String browserVisitId = resolveBrowserVisitId(body.getBrowserVisitId(), events);
        String sessionId = resolveSessionId(body.getSessionId(), events);
        String pageVisitId = resolvePageVisitId(body.getPageVisitId(), events);
        String traceId = resolveServerTraceId(events);
        String pagePath = resolveLastPage(events);
        String ua = resolveUa(events);
        String triggerAction = resolveTriggerAction(body.getTriggerAction(), events);

        String eventsJson;
        try {
            eventsJson = objectMapper.writeValueAsString(events);
        } catch (JsonProcessingException e) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "events 序列化失败");
        }
        if (eventsJson.length() > MAX_EVENTS_JSON_LEN) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "events 体积过大");
        }

        SysClientTrack row = new SysClientTrack();
        row.setOperationId(operationId);
        row.setBrowserVisitId(browserVisitId);
        row.setSessionId(sessionId);
        row.setPageVisitId(pageVisitId);
        row.setTriggerAction(triggerAction);
        row.setTraceId(traceId);
        row.setUserId(userId);
        row.setUserName(userName);
        row.setReason(StrUtil.blankToDefault(body.getReason(), "normal"));
        row.setPagePath(pagePath);
        row.setUa(StrUtil.sub(ua, 0, 500));
        row.setEventsJson(eventsJson);
        row.setClientIp(clientIp(request));
        row.setCreateTime(LocalDateTime.now());
        mapper.insert(row);
    }

    @Override
    public PageInfo<SysClientTrackVo> page(SysClientTrackQueryBo query) {
        int pageNum = query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum();
        int pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 10 : query.getPageSize();
        LambdaQueryWrapper<SysClientTrack> w = buildWrapper(query);
        w.orderByDesc(SysClientTrack::getCreateTime);
        Page<SysClientTrack> mp = mapper.selectPage(new Page<>(pageNum, pageSize), w);
        List<String> paths = mp.getRecords().stream().map(SysClientTrack::getPagePath).toList();
        Map<String, MenuMatch> menuByPath = menuPathResolver.resolveBatch(paths);
        List<SysClientTrackVo> rows = new ArrayList<>(mp.getRecords().size());
        for (SysClientTrack row : mp.getRecords()) {
            SysClientTrackVo vo = BeanUtil.copyProperties(row, SysClientTrackVo.class);
            enrichMenuFields(vo, menuByPath);
            rows.add(vo);
        }
        Page<SysClientTrackVo> voPage = new Page<>(mp.getCurrent(), mp.getSize(), mp.getTotal());
        voPage.setRecords(rows);
        return PageInfo.from(voPage);
    }

    @Override
    public ClientTrackTimelineVo timeline(ClientTrackTimelineQueryBo query) {
        validateTimelineQuery(query);
        SysClientTrackQueryBo listQuery = toListQuery(query);
        LambdaQueryWrapper<SysClientTrack> w = buildWrapper(listQuery);
        w.orderByAsc(SysClientTrack::getCreateTime);
        w.last("LIMIT " + (MAX_TIMELINE_BATCHES + 1));
        List<SysClientTrack> rows = mapper.selectList(w);
        boolean truncated = rows.size() > MAX_TIMELINE_BATCHES;
        if (truncated) {
            rows = rows.subList(0, MAX_TIMELINE_BATCHES);
        }
        return buildTimelineVo(query, rows, truncated, false);
    }

    @Override
    public ClientTrackPageVisitNodeVo timelinePageDetail(ClientTrackTimelinePageQueryBo query) {
        validateTimelineQuery(query);
        validateTimelinePageQuery(query);
        SysClientTrackQueryBo listQuery = toListQuery(query);
        LambdaQueryWrapper<SysClientTrack> w = buildWrapper(listQuery);
        if (StrUtil.isNotBlank(query.getSessionId())) {
            w.eq(SysClientTrack::getSessionId, query.getSessionId().trim());
        }
        if (StrUtil.isNotBlank(query.getPageVisitId())) {
            w.eq(SysClientTrack::getPageVisitId, query.getPageVisitId().trim());
        } else if (StrUtil.isNotBlank(query.getPagePath())) {
            w.eq(SysClientTrack::getPagePath, query.getPagePath().trim());
        }
        w.orderByAsc(SysClientTrack::getCreateTime);
        w.last("LIMIT " + (MAX_PAGE_DETAIL_BATCHES + 1));
        List<SysClientTrack> rows = mapper.selectList(w);
        if (rows.isEmpty()) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "未找到该页面的监控批次");
        }
        if (rows.size() > MAX_PAGE_DETAIL_BATCHES) {
            rows = rows.subList(0, MAX_PAGE_DETAIL_BATCHES);
        }
        String pageVisitKey = StrUtil.blankToDefault(rows.get(0).getPageVisitId(), UNKNOWN_PAGE_VISIT_KEY);
        List<String> paths = rows.stream().map(SysClientTrack::getPagePath).distinct().toList();
        Map<String, MenuMatch> menuByPath = menuPathResolver.resolveBatch(paths);
        return buildPageVisitNode(pageVisitKey, rows, menuByPath, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeBatch(List<Long> batchIds) {
        if (batchIds == null || batchIds.isEmpty()) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "请选择要删除的记录");
        }
        mapper.deleteByIds(batchIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cleanAll() {
        mapper.delete(Wrappers.lambdaQuery());
    }

    private LambdaQueryWrapper<SysClientTrack> buildWrapper(SysClientTrackQueryBo query) {
        LambdaQueryWrapper<SysClientTrack> w = Wrappers.lambdaQuery();
        if (query.getBatchId() != null) {
            w.eq(SysClientTrack::getBatchId, query.getBatchId());
        }
        if (StrUtil.isNotBlank(query.getTraceId())) {
            w.eq(SysClientTrack::getTraceId, query.getTraceId().trim());
        }
        if (StrUtil.isNotBlank(query.getOperationId())) {
            w.eq(SysClientTrack::getOperationId, query.getOperationId().trim());
        }
        if (StrUtil.isNotBlank(query.getBrowserVisitId())) {
            w.eq(SysClientTrack::getBrowserVisitId, query.getBrowserVisitId().trim());
        }
        if (StrUtil.isNotBlank(query.getSessionId())) {
            w.eq(SysClientTrack::getSessionId, query.getSessionId().trim());
        }
        if (StrUtil.isNotBlank(query.getPageVisitId())) {
            w.eq(SysClientTrack::getPageVisitId, query.getPageVisitId().trim());
        }
        if (StrUtil.isNotBlank(query.getUserName())) {
            w.like(SysClientTrack::getUserName, query.getUserName().trim());
        }
        if (StrUtil.isNotBlank(query.getPagePath())) {
            w.like(SysClientTrack::getPagePath, query.getPagePath().trim());
        }
        if (StrUtil.isNotBlank(query.getTriggerAction())) {
            w.like(SysClientTrack::getTriggerAction, query.getTriggerAction().trim());
        }
        if (StrUtil.isNotBlank(query.getMenuName())) {
            List<String> menuPaths = menuPathResolver.resolvePathsByMenuKeyword(query.getMenuName().trim());
            if (menuPaths.isEmpty()) {
                w.eq(SysClientTrack::getBatchId, -1L);
            } else {
                w.and(nested -> {
                    for (String path : menuPaths) {
                        nested.or().eq(SysClientTrack::getPagePath, path)
                                .or().likeRight(SysClientTrack::getPagePath, path + "/");
                    }
                });
            }
        }
        if (StrUtil.isNotBlank(query.getReason())) {
            w.eq(SysClientTrack::getReason, query.getReason().trim());
        }
        if (query.getBeginDate() != null) {
            w.ge(SysClientTrack::getCreateTime, query.getBeginDate().atStartOfDay());
        }
        if (query.getEndDate() != null) {
            w.le(SysClientTrack::getCreateTime, query.getEndDate().atTime(LocalTime.MAX));
        }
        return w;
    }

    private void enrichMenuFields(SysClientTrackVo vo, Map<String, MenuMatch> menuByPath) {
        if (vo == null || StrUtil.isBlank(vo.getPagePath())) {
            return;
        }
        String key = normalizePagePathKey(vo.getPagePath());
        MenuMatch match = menuByPath != null ? menuByPath.get(key) : null;
        if (match == null) {
            match = menuPathResolver.resolve(vo.getPagePath());
        }
        if (match != null) {
            vo.setMenuName(match.menuName());
            vo.setMenuBreadcrumb(match.breadcrumb());
        }
    }

    private static String normalizePagePathKey(String raw) {
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

    private static String resolveOperationId(String batchOperationId, List<Map<String, Object>> events) {
        if (StrUtil.isNotBlank(batchOperationId)) {
            return batchOperationId.trim();
        }
        if (events == null || events.isEmpty()) {
            return "";
        }
        for (Map<String, Object> ev : events) {
            Object v = ev.get("operationId");
            if (v != null && StrUtil.isNotBlank(String.valueOf(v))) {
                return String.valueOf(v).trim();
            }
        }
        return "";
    }

    private static String resolveBrowserVisitId(String batchBrowserVisitId, List<Map<String, Object>> events) {
        if (StrUtil.isNotBlank(batchBrowserVisitId)) {
            return batchBrowserVisitId.trim();
        }
        return resolveStringFieldFromEvents(events, "browserVisitId");
    }

    private static String resolveSessionId(String batchSessionId, List<Map<String, Object>> events) {
        if (StrUtil.isNotBlank(batchSessionId)) {
            return batchSessionId.trim();
        }
        return resolveStringFieldFromEvents(events, "sessionId");
    }

    private static String resolvePageVisitId(String batchPageVisitId, List<Map<String, Object>> events) {
        if (StrUtil.isNotBlank(batchPageVisitId)) {
            return batchPageVisitId.trim();
        }
        return resolveStringFieldFromEvents(events, "pageVisitId");
    }

    private static String resolveStringFieldFromEvents(List<Map<String, Object>> events, String field) {
        if (events == null || events.isEmpty()) {
            return "";
        }
        for (Map<String, Object> ev : events) {
            Object v = ev.get(field);
            if (v != null && StrUtil.isNotBlank(String.valueOf(v))) {
                return String.valueOf(v).trim();
            }
        }
        return "";
    }

    /** 取批次内首个 API 事件的 serverTraceId，便于快捷跳转 oper_log。 */
    private static String resolveServerTraceId(List<Map<String, Object>> events) {
        if (events == null || events.isEmpty()) {
            return "";
        }
        for (Map<String, Object> ev : events) {
            Object v = ev.get("serverTraceId");
            if (v != null && StrUtil.isNotBlank(String.valueOf(v))) {
                return String.valueOf(v).trim();
            }
        }
        return "";
    }

    /**
     * 触发操作：优先上报体 triggerAction，否则从 events 的 trigger / click.target 推断。
     */
    private static String resolveTriggerAction(String batchTrigger, List<Map<String, Object>> events) {
        if (StrUtil.isNotBlank(batchTrigger)) {
            return StrUtil.sub(batchTrigger.trim(), 0, 128);
        }
        if (events == null || events.isEmpty()) {
            return "";
        }
        for (Map<String, Object> ev : events) {
            Object trigger = ev.get("trigger");
            if (trigger != null && StrUtil.isNotBlank(String.valueOf(trigger))) {
                return StrUtil.sub(String.valueOf(trigger).trim(), 0, 128);
            }
        }
        for (Map<String, Object> ev : events) {
            if (!"click".equals(String.valueOf(ev.get("type")))) {
                continue;
            }
            Object target = ev.get("target");
            if (target == null || StrUtil.isBlank(String.valueOf(target))) {
                continue;
            }
            String t = String.valueOf(target).trim();
            if ("BUTTON".equalsIgnoreCase(t) || "A".equalsIgnoreCase(t) || "SPAN".equalsIgnoreCase(t)) {
                continue;
            }
            return StrUtil.sub(t, 0, 128);
        }
        for (Map<String, Object> ev : events) {
            if (!"route_enter".equals(String.valueOf(ev.get("type")))) {
                continue;
            }
            Object title = ev.get("title");
            if (title != null && StrUtil.isNotBlank(String.valueOf(title))) {
                return StrUtil.sub("访问:" + String.valueOf(title).trim(), 0, 128);
            }
            Object path = ev.get("path");
            if (path != null && StrUtil.isNotBlank(String.valueOf(path))) {
                return StrUtil.sub("访问:" + String.valueOf(path).trim(), 0, 128);
            }
        }
        return "";
    }

    private static String resolveLastPage(List<Map<String, Object>> events) {
        if (events == null || events.isEmpty()) {
            return "";
        }
        for (int i = events.size() - 1; i >= 0; i--) {
            Object page = events.get(i).get("page");
            if (page != null && StrUtil.isNotBlank(String.valueOf(page))) {
                return StrUtil.sub(String.valueOf(page), 0, 500);
            }
        }
        return "";
    }

    private static String resolveUa(List<Map<String, Object>> events) {
        if (events == null || events.isEmpty()) {
            return "";
        }
        Object ua = events.get(0).get("ua");
        return ua == null ? "" : String.valueOf(ua);
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

    private static void validateTimelineQuery(ClientTrackTimelineQueryBo query) {
        if (query == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "查询条件不能为空");
        }
        if (StrUtil.isAllBlank(query.getBrowserVisitId(), query.getSessionId(), query.getUserName())) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "browserVisitId、sessionId、userName 至少填写一项");
        }
    }

    private static SysClientTrackQueryBo toListQuery(ClientTrackTimelineQueryBo query) {
        SysClientTrackQueryBo bo = new SysClientTrackQueryBo();
        bo.setBrowserVisitId(query.getBrowserVisitId());
        bo.setSessionId(query.getSessionId());
        bo.setUserName(query.getUserName());
        bo.setBeginDate(query.getBeginDate());
        bo.setEndDate(query.getEndDate());
        return bo;
    }

    private static void validateTimelinePageQuery(ClientTrackTimelinePageQueryBo query) {
        if (StrUtil.isAllBlank(query.getPageVisitId(), query.getPagePath())) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "pageVisitId 与 pagePath 至少填写一项");
        }
    }

    private ClientTrackTimelineVo buildTimelineVo(ClientTrackTimelineQueryBo query, List<SysClientTrack> rows,
            boolean truncated, boolean includeDetail) {
        ClientTrackTimelineVo vo = new ClientTrackTimelineVo();
        vo.setTotalBatches(rows.size());
        vo.setTruncated(truncated);
        if (rows.isEmpty()) {
            vo.setBrowserVisitId(StrUtil.blankToDefault(query.getBrowserVisitId(), ""));
            vo.setSessionId(StrUtil.blankToDefault(query.getSessionId(), ""));
            vo.setUserName(StrUtil.blankToDefault(query.getUserName(), ""));
            return vo;
        }
        SysClientTrack first = rows.get(0);
        vo.setBrowserVisitId(StrUtil.blankToDefault(query.getBrowserVisitId(), first.getBrowserVisitId()));
        vo.setSessionId(StrUtil.blankToDefault(query.getSessionId(), first.getSessionId()));
        vo.setUserName(StrUtil.blankToDefault(query.getUserName(), first.getUserName()));

        List<String> paths = rows.stream().map(SysClientTrack::getPagePath).distinct().toList();
        Map<String, MenuMatch> menuByPath = menuPathResolver.resolveBatch(paths);

        Map<String, List<SysClientTrack>> grouped = new LinkedHashMap<>();
        for (SysClientTrack row : rows) {
            String key = StrUtil.blankToDefault(row.getPageVisitId(), UNKNOWN_PAGE_VISIT_KEY);
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(row);
        }

        List<ClientTrackPageVisitNodeVo> pages = new ArrayList<>();
        for (Map.Entry<String, List<SysClientTrack>> entry : grouped.entrySet()) {
            pages.add(buildPageVisitNode(entry.getKey(), entry.getValue(), menuByPath, includeDetail));
        }
        pages.sort(Comparator.comparing(ClientTrackPageVisitNodeVo::getFirstTime, Comparator.nullsLast(Comparator.naturalOrder())));
        vo.setPages(pages);
        List<ClientTrackSessionNodeVo> sessions = buildSessionNodes(pages);
        vo.setSessions(sessions);
        vo.setPageFlowEdges(buildPageFlowEdgesWithinSessions(sessions));
        return vo;
    }

    /**
     * 按 sessionId 分段；同 session 内页面按 firstTime 升序，会话段按 firstTime 降序（最近登录在前）。
     */
    private static List<ClientTrackSessionNodeVo> buildSessionNodes(List<ClientTrackPageVisitNodeVo> pages) {
        Map<String, List<ClientTrackPageVisitNodeVo>> grouped = new LinkedHashMap<>();
        for (ClientTrackPageVisitNodeVo page : pages) {
            String key = StrUtil.blankToDefault(page.getSessionId(), UNKNOWN_PAGE_VISIT_KEY);
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(page);
        }
        List<ClientTrackSessionNodeVo> sessions = new ArrayList<>();
        for (Map.Entry<String, List<ClientTrackPageVisitNodeVo>> entry : grouped.entrySet()) {
            List<ClientTrackPageVisitNodeVo> sessionPages = new ArrayList<>(entry.getValue());
            sessionPages.sort(Comparator.comparing(ClientTrackPageVisitNodeVo::getFirstTime,
                    Comparator.nullsLast(Comparator.naturalOrder())));
            ClientTrackSessionNodeVo session = new ClientTrackSessionNodeVo();
            String sessionId = UNKNOWN_PAGE_VISIT_KEY.equals(entry.getKey()) ? "" : entry.getKey();
            session.setSessionId(sessionId);
            session.setBrowserVisitId(StrUtil.blankToDefault(sessionPages.get(0).getBrowserVisitId(), ""));
            session.setFirstTime(sessionPages.stream()
                    .map(ClientTrackPageVisitNodeVo::getFirstTime)
                    .filter(Objects::nonNull)
                    .min(Comparator.naturalOrder())
                    .orElse(null));
            session.setLastTime(sessionPages.stream()
                    .map(ClientTrackPageVisitNodeVo::getFirstTime)
                    .filter(Objects::nonNull)
                    .max(Comparator.naturalOrder())
                    .orElse(null));
            session.setPageCount(sessionPages.size());
            session.setPages(sessionPages);
            session.setPageFlowEdges(buildPageFlowEdges(sessionPages));
            sessions.add(session);
        }
        sessions.sort(Comparator.comparing(ClientTrackSessionNodeVo::getFirstTime,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return sessions;
    }

    private static List<ClientTrackPageFlowEdgeVo> buildPageFlowEdgesWithinSessions(List<ClientTrackSessionNodeVo> sessions) {
        List<ClientTrackPageFlowEdgeVo> edges = new ArrayList<>();
        for (ClientTrackSessionNodeVo session : sessions) {
            if (session.getPageFlowEdges() != null) {
                edges.addAll(session.getPageFlowEdges());
            }
        }
        return edges;
    }

    private ClientTrackPageVisitNodeVo buildPageVisitNode(String pageVisitKey, List<SysClientTrack> batches,
            Map<String, MenuMatch> menuByPath, boolean includeDetail) {
        batches.sort(Comparator.comparing(SysClientTrack::getCreateTime, Comparator.nullsLast(Comparator.naturalOrder())));
        ClientTrackPageVisitNodeVo page = new ClientTrackPageVisitNodeVo();
        page.setPageVisitId(UNKNOWN_PAGE_VISIT_KEY.equals(pageVisitKey) ? "" : pageVisitKey);
        SysClientTrack head = batches.get(0);
        page.setSessionId(StrUtil.blankToDefault(head.getSessionId(), ""));
        page.setBrowserVisitId(StrUtil.blankToDefault(head.getBrowserVisitId(), ""));
        page.setPagePath(head.getPagePath());
        page.setFirstTime(head.getCreateTime());
        enrichPageMenu(page, head.getPagePath(), menuByPath);

        SysClientTrack visitBatchRow = batches.stream()
                .filter(this::isPageVisitBatch)
                .findFirst()
                .orElse(head);

        if (includeDetail) {
            page.setPageVisitBatch(toActionNode(visitBatchRow, true));
            for (SysClientTrack row : batches) {
                if (row.getBatchId().equals(visitBatchRow.getBatchId())) {
                    continue;
                }
                page.getActions().add(toActionNode(row, false));
            }
            page.setActionCount(page.getActions().size());
            page.setEventCount(countPageEvents(page));
        } else {
            int actionCount = 0;
            int eventCount = 0;
            for (SysClientTrack row : batches) {
                eventCount += countEventsInJson(row.getEventsJson());
                if (!row.getBatchId().equals(visitBatchRow.getBatchId())) {
                    actionCount++;
                }
            }
            page.setActionCount(actionCount);
            page.setEventCount(eventCount);
        }
        return page;
    }

    /**
     * 仅统计 eventsJson 数组长度，避免概览接口全量解析事件明细。
     */
    @SuppressWarnings("unchecked")
    private int countEventsInJson(String eventsJson) {
        if (StrUtil.isBlank(eventsJson)) {
            return 0;
        }
        try {
            List<?> raw = objectMapper.readValue(eventsJson, List.class);
            return raw == null ? 0 : raw.size();
        } catch (JsonProcessingException e) {
            return 0;
        }
    }

    private static int countPageEvents(ClientTrackPageVisitNodeVo page) {
        int total = 0;
        if (page.getPageVisitBatch() != null && page.getPageVisitBatch().getEvents() != null) {
            total += page.getPageVisitBatch().getEvents().size();
        }
        if (page.getActions() != null) {
            for (ClientTrackActionNodeVo action : page.getActions()) {
                if (action.getEvents() != null) {
                    total += action.getEvents().size();
                }
            }
        }
        return total;
    }

    private void enrichPageMenu(ClientTrackPageVisitNodeVo page, String pagePath, Map<String, MenuMatch> menuByPath) {
        if (StrUtil.isBlank(pagePath)) {
            return;
        }
        String key = normalizePagePathKey(pagePath);
        MenuMatch match = menuByPath != null ? menuByPath.get(key) : null;
        if (match == null) {
            match = menuPathResolver.resolve(pagePath);
        }
        if (match != null) {
            page.setMenuName(match.menuName());
            page.setMenuBreadcrumb(match.breadcrumb());
        }
    }

    private boolean isPageVisitBatch(SysClientTrack row) {
        String trigger = row.getTriggerAction();
        return StrUtil.isNotBlank(trigger) && trigger.startsWith("访问:");
    }

    private ClientTrackActionNodeVo toActionNode(SysClientTrack row, boolean pageVisitBatch) {
        ClientTrackActionNodeVo node = new ClientTrackActionNodeVo();
        node.setBatchId(row.getBatchId());
        node.setOperationId(row.getOperationId());
        node.setTriggerAction(row.getTriggerAction());
        node.setReason(row.getReason());
        node.setPageVisitBatch(pageVisitBatch);
        node.setCreateTime(row.getCreateTime());
        List<Map<String, Object>> events = parseEventsJson(row.getEventsJson());
        for (Map<String, Object> ev : events) {
            node.getEvents().add(toEventItem(ev, row.getEventsJson()));
        }
        return node;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseEventsJson(String eventsJson) {
        if (StrUtil.isBlank(eventsJson)) {
            return List.of();
        }
        try {
            List<?> raw = objectMapper.readValue(eventsJson, List.class);
            List<Map<String, Object>> out = new ArrayList<>(raw.size());
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

    private ClientTrackEventItemVo toEventItem(Map<String, Object> ev, String batchEventsJson) {
        ClientTrackEventItemVo item = new ClientTrackEventItemVo();
        item.setType(String.valueOf(ev.getOrDefault("type", "")));
        Object ts = ev.get("ts");
        if (ts instanceof Number n) {
            item.setTs(n.longValue());
        }
        item.setLabel(buildEventLabel(ev));
        Object url = ev.get("url");
        if (url != null) {
            item.setUrl(String.valueOf(url));
        }
        Object method = ev.get("method");
        if (method != null) {
            item.setMethod(String.valueOf(method));
        }
        Object cost = ev.get("cost");
        if (cost instanceof Number cn) {
            item.setCost(cn.longValue());
        }
        Object trace = ev.get("serverTraceId");
        if (trace == null) {
            trace = ev.get("clientTraceId");
        }
        if (trace != null) {
            item.setServerTraceId(String.valueOf(trace));
        }
        try {
            item.setRawJson(objectMapper.writeValueAsString(ev));
        } catch (JsonProcessingException e) {
            item.setRawJson(String.valueOf(ev));
        }
        return item;
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
            return path != null ? "进入 " + path : "进入页面";
        }
        if ("route_leave".equals(type)) {
            return "离开页面";
        }
        if ("js_error".equals(type) || "promise_error".equals(type)) {
            Object msg = ev.get("msg");
            return msg != null ? String.valueOf(msg) : type;
        }
        return type;
    }

    private static List<ClientTrackPageFlowEdgeVo> buildPageFlowEdges(List<ClientTrackPageVisitNodeVo> pages) {
        List<ClientTrackPageFlowEdgeVo> edges = new ArrayList<>();
        for (int i = 1; i < pages.size(); i++) {
            ClientTrackPageVisitNodeVo from = pages.get(i - 1);
            ClientTrackPageVisitNodeVo to = pages.get(i);
            ClientTrackPageFlowEdgeVo edge = new ClientTrackPageFlowEdgeVo();
            edge.setFromPageVisitId(from.getPageVisitId());
            edge.setToPageVisitId(to.getPageVisitId());
            edge.setFromPagePath(from.getPagePath());
            edge.setToPagePath(to.getPagePath());
            edge.setFromMenuLabel(menuLabel(from));
            edge.setToMenuLabel(menuLabel(to));
            edge.setAtTime(to.getFirstTime());
            edges.add(edge);
        }
        return edges;
    }

    private static String menuLabel(ClientTrackPageVisitNodeVo page) {
        if (StrUtil.isNotBlank(page.getMenuBreadcrumb())) {
            return page.getMenuBreadcrumb();
        }
        if (StrUtil.isNotBlank(page.getMenuName())) {
            return page.getMenuName();
        }
        return StrUtil.blankToDefault(page.getPagePath(), "未知页面");
    }
}
