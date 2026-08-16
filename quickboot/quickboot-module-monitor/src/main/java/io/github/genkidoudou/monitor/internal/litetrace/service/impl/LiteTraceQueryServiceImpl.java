package io.github.genkidoudou.monitor.internal.litetrace.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.monitor.internal.litetrace.dto.PageVisitVo;
import io.github.genkidoudou.monitor.internal.litetrace.dto.TraceIndexQueryBo;
import io.github.genkidoudou.monitor.internal.litetrace.dto.TraceIndexVo;
import io.github.genkidoudou.monitor.internal.litetrace.dto.TraceSpanVo;
import io.github.genkidoudou.monitor.internal.litetrace.entity.SysTraceIndex;
import io.github.genkidoudou.monitor.internal.litetrace.entity.SysTraceSpan;
import io.github.genkidoudou.monitor.internal.litetrace.mapper.SysTraceIndexMapper;
import io.github.genkidoudou.monitor.internal.litetrace.mapper.SysTraceSpanMapper;
import io.github.genkidoudou.monitor.internal.litetrace.service.LiteTraceQueryService;
import io.github.genkidoudou.monitor.internal.litetrace.service.TraceProjectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Lite 链路查询实现：索引分页、页面访问聚合与 span 列表。
 */
@Service
@RequiredArgsConstructor
public class LiteTraceQueryServiceImpl implements LiteTraceQueryService {

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SysTraceIndexMapper traceIndexMapper;
    private final SysTraceSpanMapper traceSpanMapper;
    private final TraceProjectionService projectionService;

    @Override
    public PageInfo<TraceIndexVo> page(TraceIndexQueryBo query) {
        if (query == null) {
            query = new TraceIndexQueryBo();
        }
        applyQueryString(query);
        int pageNum = query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum();
        int pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 20 : Math.min(query.getPageSize(), 100);
        LambdaQueryWrapper<SysTraceIndex> w = buildWrapper(query);
        applySort(w, query);
        Page<SysTraceIndex> page = traceIndexMapper.selectPage(new Page<>(pageNum, pageSize), w);
        Page<TraceIndexVo> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        List<TraceIndexVo> rows = new ArrayList<>();
        for (SysTraceIndex row : page.getRecords()) {
            rows.add(BeanUtil.copyProperties(row, TraceIndexVo.class));
        }
        voPage.setRecords(rows);
        return PageInfo.from(voPage);
    }

    @Override
    public List<PageVisitVo> listPageVisits(TraceIndexQueryBo query) {
        if (query == null) {
            query = new TraceIndexQueryBo();
        }
        applyQueryString(query);
        String searchType = StrUtil.blankToDefault(query.getSearchType(), "all").trim().toLowerCase(Locale.ROOT);
        // 接口 URL / traceId 搜索只服务于接口列表，不聚合页面访问
        if ("url".equals(searchType) || "traceid".equals(searchType)) {
            return List.of();
        }
        TraceIndexQueryBo q = BeanUtil.copyProperties(query, TraceIndexQueryBo.class);
        q.setListMode(null);
        q.setRootSource("browser");
        LambdaQueryWrapper<SysTraceIndex> w = buildWrapper(q);
        w.isNotNull(SysTraceIndex::getPageVisitId);
        w.ne(SysTraceIndex::getPageVisitId, "");
        w.orderByDesc(SysTraceIndex::getStartedAt);
        w.last("LIMIT 800");
        List<SysTraceIndex> rows = traceIndexMapper.selectList(w);
        Map<String, List<SysTraceIndex>> byVisit = new LinkedHashMap<>();
        for (SysTraceIndex row : rows) {
            byVisit.computeIfAbsent(row.getPageVisitId(), k -> new ArrayList<>()).add(row);
        }
        List<PageVisitVo> out = new ArrayList<>();
        for (Map.Entry<String, List<SysTraceIndex>> e : byVisit.entrySet()) {
            List<SysTraceIndex> list = e.getValue();
            list.sort(Comparator.comparing(SysTraceIndex::getStartedAt, Comparator.nullsLast(Comparator.naturalOrder())));
            SysTraceIndex first = list.get(0);
            SysTraceIndex last = list.get(list.size() - 1);
            PageVisitVo vo = new PageVisitVo();
            vo.setPageVisitId(e.getKey());
            vo.setSessionId(firstNonBlank(last.getSessionId(), first.getSessionId()));
            vo.setUin(firstNonBlank(last.getUin(), first.getUin()));
            vo.setCallerName(firstNonBlank(last.getCallerName(), first.getCallerName()));
            vo.setPagePath(firstNonBlank(last.getPagePath(), first.getPagePath()));
            vo.setFromPage(firstNonBlank(first.getFromPage(), last.getFromPage()));
            boolean anyFail = list.stream().anyMatch(r -> "0".equals(r.getOkFlag()));
            vo.setOkFlag(anyFail ? "0" : "1");
            vo.setTraceCount((long) list.size());
            long dur = list.stream().mapToLong(r -> r.getDurationMs() == null ? 0L : r.getDurationMs()).sum();
            vo.setDurationMs(dur);
            vo.setStartedAt(fmt(first.getStartedAt()));
            vo.setEndedAt(fmt(last.getEndedAt() != null ? last.getEndedAt() : last.getStartedAt()));
            out.add(vo);
        }
        sortPageVisits(out, query);
        int limit = query.getPageSize() == null ? 50 : Math.min(Math.max(query.getPageSize(), 1), 100);
        if (out.size() > limit) {
            return new ArrayList<>(out.subList(0, limit));
        }
        return out;
    }

    @Override
    public List<TraceIndexVo> listByPageVisit(String pageVisitId, String beginTime, String endTime) {
        if (StrUtil.isBlank(pageVisitId)) {
            throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "pageVisitId 不能为空");
        }
        LambdaQueryWrapper<SysTraceIndex> w = new LambdaQueryWrapper<>();
        w.eq(SysTraceIndex::getPageVisitId, pageVisitId.trim());
        LocalDateTime begin = parseDateTime(beginTime);
        LocalDateTime end = parseDateTime(endTime);
        if (begin != null) {
            w.ge(SysTraceIndex::getStartedAt, begin);
        }
        if (end != null) {
            w.lt(SysTraceIndex::getStartedAt, end);
        }
        w.orderByAsc(SysTraceIndex::getStartedAt);
        w.last("LIMIT 200");
        List<TraceIndexVo> out = new ArrayList<>();
        for (SysTraceIndex row : traceIndexMapper.selectList(w)) {
            out.add(BeanUtil.copyProperties(row, TraceIndexVo.class));
        }
        return out;
    }

    @Override
    public TraceIndexVo detail(String traceId) {
        if (StrUtil.isBlank(traceId)) {
            throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "traceId 不能为空");
        }
        SysTraceIndex row = traceIndexMapper.selectById(traceId.trim());
        if (row == null) {
            throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "链路不存在");
        }
        return BeanUtil.copyProperties(row, TraceIndexVo.class);
    }

    @Override
    public List<TraceSpanVo> spans(String traceId) {
        if (StrUtil.isBlank(traceId)) {
            throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "traceId 不能为空");
        }
        List<SysTraceSpan> list = traceSpanMapper.selectList(new LambdaQueryWrapper<SysTraceSpan>()
            .eq(SysTraceSpan::getTraceId, traceId.trim())
            .orderByAsc(SysTraceSpan::getStartOffsetMs)
            .orderByAsc(SysTraceSpan::getCreateTime));
        List<TraceSpanVo> out = new ArrayList<>();
        for (SysTraceSpan s : list) {
            out.add(BeanUtil.copyProperties(s, TraceSpanVo.class));
        }
        return out;
    }

    @Override
    public void ensureRoot(String traceId, String rootSource, String entry, String caller) {
        SysTraceIndex idx = new SysTraceIndex();
        idx.setTraceId(traceId);
        idx.setRootSource(rootSource);
        idx.setEntryName(entry);
        idx.setCallerName(caller);
        idx.setOkFlag("1");
        idx.setStartedAt(LocalDateTime.now());
        idx.setEndedAt(LocalDateTime.now());
        projectionService.upsertIndex(idx);
    }

    private LambdaQueryWrapper<SysTraceIndex> buildWrapper(TraceIndexQueryBo query) {
        LambdaQueryWrapper<SysTraceIndex> w = new LambdaQueryWrapper<>();
        String listMode = StrUtil.blankToDefault(query.getListMode(), "").trim().toLowerCase(Locale.ROOT);
        if ("api".equals(listMode)) {
            w.in(SysTraceIndex::getRootSource, List.of("browser", "api"));
        } else if ("job".equals(listMode)) {
            w.eq(SysTraceIndex::getRootSource, "job");
        } else if (StrUtil.isNotBlank(query.getRootSource())) {
            String rs = query.getRootSource().trim();
            if (rs.contains(",")) {
                List<String> parts = new ArrayList<>();
                for (String p : rs.split(",")) {
                    if (StrUtil.isNotBlank(p)) {
                        parts.add(p.trim());
                    }
                }
                if (!parts.isEmpty()) {
                    w.in(SysTraceIndex::getRootSource, parts);
                }
            } else {
                w.eq(SysTraceIndex::getRootSource, rs);
            }
        }
        w.eq(StrUtil.isNotBlank(query.getTraceId()), SysTraceIndex::getTraceId, query.getTraceId());
        w.eq(StrUtil.isNotBlank(query.getOperationId()), SysTraceIndex::getOperationId, query.getOperationId());
        w.like(StrUtil.isNotBlank(query.getUin()), SysTraceIndex::getUin, query.getUin());
        if (StrUtil.isNotBlank(query.getCallerName())) {
            w.eq(SysTraceIndex::getCallerName, query.getCallerName().trim());
        }
        w.like(StrUtil.isNotBlank(query.getEntryName()), SysTraceIndex::getEntryName, query.getEntryName());
        w.eq(StrUtil.isNotBlank(query.getActionName()), SysTraceIndex::getActionName, query.getActionName());
        w.like(StrUtil.isNotBlank(query.getPagePath()), SysTraceIndex::getPagePath, query.getPagePath());
        w.like(StrUtil.isNotBlank(query.getSessionId()), SysTraceIndex::getSessionId, query.getSessionId());
        w.eq(StrUtil.isNotBlank(query.getPageVisitId()), SysTraceIndex::getPageVisitId, query.getPageVisitId());
        w.eq(StrUtil.isNotBlank(query.getOkFlag()), SysTraceIndex::getOkFlag, query.getOkFlag());
        applySearchType(w, query);
        LocalDateTime begin = parseDateTime(query.getBeginTime());
        LocalDateTime end = parseDateTime(query.getEndTime());
        if (begin != null) {
            w.ge(SysTraceIndex::getStartedAt, begin);
        }
        if (end != null) {
            w.lt(SysTraceIndex::getStartedAt, end);
        }
        return w;
    }

    private static void applySearchType(LambdaQueryWrapper<SysTraceIndex> w, TraceIndexQueryBo query) {
        String kw = StrUtil.trim(query.getKeyword());
        if (StrUtil.isBlank(kw)) {
            return;
        }
        String type = StrUtil.blankToDefault(query.getSearchType(), "all").trim().toLowerCase(Locale.ROOT);
        switch (type) {
            case "page" -> w.and(n -> n.like(SysTraceIndex::getPagePath, kw).or().like(SysTraceIndex::getFromPage, kw));
            case "url" -> w.like(SysTraceIndex::getEntryName, kw);
            case "traceid" -> w.like(SysTraceIndex::getTraceId, kw);
            case "sessionid" -> w.like(SysTraceIndex::getSessionId, kw);
            case "pagevisitid" -> w.like(SysTraceIndex::getPageVisitId, kw);
            case "keyword", "all" -> w.and(n -> n.like(SysTraceIndex::getPagePath, kw)
                .or().like(SysTraceIndex::getFromPage, kw)
                .or().like(SysTraceIndex::getEntryName, kw)
                .or().like(SysTraceIndex::getTraceId, kw)
                .or().like(SysTraceIndex::getSessionId, kw)
                .or().like(SysTraceIndex::getPageVisitId, kw)
                .or().like(SysTraceIndex::getUin, kw)
                .or().like(SysTraceIndex::getCallerName, kw));
            default -> w.and(n -> n.like(SysTraceIndex::getPagePath, kw).or().like(SysTraceIndex::getEntryName, kw)
                .or().like(SysTraceIndex::getTraceId, kw));
        }
    }

    private static void applySort(LambdaQueryWrapper<SysTraceIndex> w, TraceIndexQueryBo query) {
        String key = StrUtil.blankToDefault(query.getSortKey(), "time").toLowerCase(Locale.ROOT);
        boolean asc = "asc".equalsIgnoreCase(query.getSortDir());
        if ("duration".equals(key)) {
            if (asc) {
                w.orderByAsc(SysTraceIndex::getDurationMs);
            } else {
                w.orderByDesc(SysTraceIndex::getDurationMs);
            }
        } else if ("name".equals(key)) {
            if (asc) {
                w.orderByAsc(SysTraceIndex::getEntryName);
            } else {
                w.orderByDesc(SysTraceIndex::getEntryName);
            }
        } else if (asc) {
            w.orderByAsc(SysTraceIndex::getStartedAt);
        } else {
            w.orderByDesc(SysTraceIndex::getStartedAt);
        }
    }

    private static void sortPageVisits(List<PageVisitVo> list, TraceIndexQueryBo query) {
        String key = StrUtil.blankToDefault(query.getSortKey(), "time").toLowerCase(Locale.ROOT);
        boolean asc = "asc".equalsIgnoreCase(query.getSortDir());
        Comparator<PageVisitVo> cmp;
        if ("duration".equals(key)) {
            cmp = Comparator.comparing(v -> v.getDurationMs() == null ? 0L : v.getDurationMs());
        } else if ("name".equals(key)) {
            cmp = Comparator.comparing(v -> StrUtil.blankToDefault(v.getPagePath(), ""), String::compareTo);
        } else {
            cmp = Comparator.comparing(v -> parseDateTime(v.getEndedAt()) != null
                ? parseDateTime(v.getEndedAt()) : parseDateTime(v.getStartedAt()), Comparator.nullsLast(Comparator.naturalOrder()));
        }
        if (!asc) {
            cmp = cmp.reversed();
        }
        list.sort(cmp);
    }

    private static void applyQueryString(TraceIndexQueryBo query) {
        if (query == null || StrUtil.isBlank(query.getQ())) {
            return;
        }
        for (String part : query.getQ().trim().split("\\s+")) {
            int i = part.indexOf(':');
            if (i <= 0) {
                continue;
            }
            String k = part.substring(0, i).trim();
            String v = part.substring(i + 1).trim();
            if (StrUtil.isBlank(v)) {
                continue;
            }
            switch (k) {
                case "traceId" -> query.setTraceId(v);
                case "operationId" -> query.setOperationId(v);
                case "uin" -> query.setUin(v);
                case "page" -> query.setPagePath(v);
                case "action" -> query.setActionName(v);
                case "entry" -> query.setEntryName(v);
                case "sessionId" -> query.setSessionId(v);
                case "pageVisitId" -> query.setPageVisitId(v);
                default -> {
                }
            }
        }
    }

    private static LocalDateTime parseDateTime(String raw) {
        if (StrUtil.isBlank(raw)) {
            return null;
        }
        String s = raw.trim().replace('T', ' ');
        if (s.length() == 10) {
            s = s + " 00:00:00";
        }
        try {
            return LocalDateTime.parse(s, TS);
        } catch (Exception ex) {
            return null;
        }
    }

    private static String fmt(LocalDateTime t) {
        return t == null ? null : t.format(TS);
    }

    private static String firstNonBlank(String a, String b) {
        if (StrUtil.isNotBlank(a)) {
            return a.trim();
        }
        if (StrUtil.isNotBlank(b)) {
            return b.trim();
        }
        return null;
    }
}
