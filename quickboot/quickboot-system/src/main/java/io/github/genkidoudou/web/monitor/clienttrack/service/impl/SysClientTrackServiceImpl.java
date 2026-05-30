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
import io.github.genkidoudou.web.monitor.clienttrack.dto.SysClientTrackQueryBo;
import io.github.genkidoudou.web.monitor.clienttrack.dto.SysClientTrackVo;
import io.github.genkidoudou.web.monitor.clienttrack.mapper.SysClientTrackMapper;
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
import java.util.List;
import java.util.Map;

/**
 * 前端用户行为监控批次服务实现。
 */
@Service
@RequiredArgsConstructor
public class SysClientTrackServiceImpl implements SysClientTrackService {

    private static final int MAX_EVENTS_JSON_LEN = 65535;

    private final SysClientTrackMapper mapper;
    private final SysUserMapper userMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void report(ClientTrackReportBo body, HttpServletRequest request) {
        StpUtil.checkLogin();
        long userId = StpUtil.getLoginIdAsLong();
        SysUser user = userMapper.selectById(userId);
        String userName = user != null ? StrUtil.blankToDefault(user.getUserName(), "") : "";

        List<Map<String, Object>> events = body.getEvents();
        String operationId = resolveOperationId(body.getOperationId(), events);
        String traceId = resolveServerTraceId(events);
        String pagePath = resolveLastPage(events);
        String ua = resolveUa(events);

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
        List<SysClientTrackVo> rows = new ArrayList<>(mp.getRecords().size());
        for (SysClientTrack row : mp.getRecords()) {
            rows.add(BeanUtil.copyProperties(row, SysClientTrackVo.class));
        }
        Page<SysClientTrackVo> voPage = new Page<>(mp.getCurrent(), mp.getSize(), mp.getTotal());
        voPage.setRecords(rows);
        return PageInfo.from(voPage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeBatch(List<Long> batchIds) {
        if (batchIds == null || batchIds.isEmpty()) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "请选择要删除的记录");
        }
        mapper.deleteByIds(batchIds);
    }

    private LambdaQueryWrapper<SysClientTrack> buildWrapper(SysClientTrackQueryBo query) {
        LambdaQueryWrapper<SysClientTrack> w = Wrappers.lambdaQuery();
        if (StrUtil.isNotBlank(query.getTraceId())) {
            w.eq(SysClientTrack::getTraceId, query.getTraceId().trim());
        }
        if (StrUtil.isNotBlank(query.getOperationId())) {
            w.eq(SysClientTrack::getOperationId, query.getOperationId().trim());
        }
        if (StrUtil.isNotBlank(query.getUserName())) {
            w.like(SysClientTrack::getUserName, query.getUserName().trim());
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
}
