package io.github.genkidoudou.monitor.internal.userbehavior.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.monitor.internal.litetrace.entity.SysRumEvent;
import io.github.genkidoudou.monitor.internal.litetrace.mapper.SysRumEventMapper;
import io.github.genkidoudou.monitor.internal.userbehavior.dto.UserBehaviorNodeVo;
import io.github.genkidoudou.monitor.internal.userbehavior.dto.UserBehaviorSessionQueryBo;
import io.github.genkidoudou.monitor.internal.userbehavior.dto.UserBehaviorSessionVo;
import io.github.genkidoudou.monitor.internal.userbehavior.service.UserBehaviorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户行为分析实现：基于 sys_rum_event 聚合会话与时间线。
 */
@Service
@RequiredArgsConstructor
public class UserBehaviorServiceImpl implements UserBehaviorService {

  private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  /** pv / action / api：本页操作需展示接口调用 */
  private static final List<String> TYPES = List.of("pv", "action", "api");

  private final SysRumEventMapper rumEventMapper;

  @Override
  public List<UserBehaviorSessionVo> listSessions(UserBehaviorSessionQueryBo query) {
    if (query == null) {
      query = new UserBehaviorSessionQueryBo();
    }
    String uin = firstNonBlank(query.getUin(), query.getUserName());
    if (StrUtil.isBlank(uin) && StrUtil.isBlank(query.getSessionId())) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "请提供 uin/userName 或 sessionId");
    }
    int limit = query.getLimit() == null ? 50 : Math.min(Math.max(query.getLimit(), 1), 200);
    LambdaQueryWrapper<SysRumEvent> w = new LambdaQueryWrapper<>();
    w.in(SysRumEvent::getEventType, TYPES);
    w.isNotNull(SysRumEvent::getSessionId);
    w.ne(SysRumEvent::getSessionId, "");
    if (StrUtil.isNotBlank(query.getSessionId())) {
      w.eq(SysRumEvent::getSessionId, query.getSessionId().trim());
    }
    if (StrUtil.isNotBlank(uin)) {
      w.eq(SysRumEvent::getUin, uin.trim());
    }
    applyTime(w, query.getBeginTime(), query.getEndTime());
    w.orderByDesc(SysRumEvent::getEventTime).orderByDesc(SysRumEvent::getCreateTime);
    w.last("LIMIT " + Math.min(limit * 40, 2000));
    List<SysRumEvent> rows = rumEventMapper.selectList(w);
    Map<String, List<SysRumEvent>> bySession = new LinkedHashMap<>();
    for (SysRumEvent row : rows) {
      bySession.computeIfAbsent(row.getSessionId(), k -> new ArrayList<>()).add(row);
    }
    List<UserBehaviorSessionVo> out = new ArrayList<>();
    for (Map.Entry<String, List<SysRumEvent>> e : bySession.entrySet()) {
      List<SysRumEvent> list = e.getValue();
      list.sort(Comparator.comparing(UserBehaviorServiceImpl::eventInstant));
      SysRumEvent first = list.get(0);
      SysRumEvent last = list.get(list.size() - 1);
      UserBehaviorSessionVo vo = new UserBehaviorSessionVo();
      vo.setSessionId(e.getKey());
      vo.setUin(firstNonBlank(first.getUin(), last.getUin()));
      vo.setFirstPage(firstPage(list));
      vo.setLastPage(lastPage(list));
      vo.setEventCount((long) list.size());
      vo.setStartedAt(fmt(eventInstant(first)));
      vo.setEndedAt(fmt(eventInstant(last)));
      out.add(vo);
      if (out.size() >= limit) {
        break;
      }
    }
    return out;
  }

  @Override
  public List<UserBehaviorNodeVo> timeline(String sessionId, String beginTime, String endTime) {
    if (StrUtil.isBlank(sessionId)) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "sessionId 不能为空");
    }
    LambdaQueryWrapper<SysRumEvent> w = new LambdaQueryWrapper<>();
    w.eq(SysRumEvent::getSessionId, sessionId.trim());
    w.in(SysRumEvent::getEventType, TYPES);
    applyTime(w, beginTime, endTime);
    w.orderByAsc(SysRumEvent::getEventTime).orderByAsc(SysRumEvent::getCreateTime);
    w.last("LIMIT 1000");
    List<UserBehaviorNodeVo> out = new ArrayList<>();
    for (SysRumEvent row : rumEventMapper.selectList(w)) {
      UserBehaviorNodeVo n = new UserBehaviorNodeVo();
      n.setEventId(row.getEventId());
      n.setEventType(row.getEventType());
      n.setPagePath(row.getPagePath());
      n.setFromPage(row.getFromPage());
      n.setActionName(actionFromPayload(row));
      fillApiFields(n, row);
      n.setTraceId(row.getTraceId());
      n.setOperationId(row.getOperationId());
      n.setSessionId(row.getSessionId());
      n.setUin(row.getUin());
      n.setEventTime(fmt(eventInstant(row)));
      out.add(n);
    }
    return out;
  }

  private static void applyTime(LambdaQueryWrapper<SysRumEvent> w, String beginTime, String endTime) {
    LocalDateTime begin = parse(beginTime);
    LocalDateTime end = parse(endTime);
    // 以「事件时刻」为准：有 event_time 用 event_time，否则用 create_time（勿 OR 两字段导致窗外数据漏入）
    if (begin != null) {
      w.and(n -> n
          .and(x -> x.isNotNull(SysRumEvent::getEventTime).ge(SysRumEvent::getEventTime, begin))
          .or(x -> x.isNull(SysRumEvent::getEventTime).ge(SysRumEvent::getCreateTime, begin)));
    }
    if (end != null) {
      w.and(n -> n
          .and(x -> x.isNotNull(SysRumEvent::getEventTime).lt(SysRumEvent::getEventTime, end))
          .or(x -> x.isNull(SysRumEvent::getEventTime).lt(SysRumEvent::getCreateTime, end)));
    }
  }

  private static LocalDateTime parse(String raw) {
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

  private static LocalDateTime eventInstant(SysRumEvent row) {
    return row.getEventTime() != null ? row.getEventTime() : row.getCreateTime();
  }

  private static String fmt(LocalDateTime t) {
    return t == null ? null : t.format(TS);
  }

  private static String firstPage(List<SysRumEvent> list) {
    for (SysRumEvent r : list) {
      if ("pv".equals(r.getEventType()) && StrUtil.isNotBlank(r.getPagePath())) {
        return r.getPagePath();
      }
    }
    return list.get(0).getPagePath();
  }

  private static String lastPage(List<SysRumEvent> list) {
    for (int i = list.size() - 1; i >= 0; i--) {
      SysRumEvent r = list.get(i);
      if ("pv".equals(r.getEventType()) && StrUtil.isNotBlank(r.getPagePath())) {
        return r.getPagePath();
      }
    }
    return list.get(list.size() - 1).getPagePath();
  }

  private static String actionFromPayload(SysRumEvent row) {
    if (!"action".equals(row.getEventType()) && !"api".equals(row.getEventType())) {
      return null;
    }
    JSONObject o = payloadObj(row);
    if (o == null) {
      return null;
    }
    return o.getStr("action");
  }

  private static void fillApiFields(UserBehaviorNodeVo n, SysRumEvent row) {
    if (!"api".equals(row.getEventType())) {
      return;
    }
    JSONObject o = payloadObj(row);
    if (o == null) {
      return;
    }
    n.setApiMethod(blankToNull(o.getStr("method")));
    n.setApiUrl(blankToNull(firstNonBlank(o.getStr("url"), o.getStr("path"))));
    Long duration = o.getLong("durationMs");
    n.setDurationMs(duration);
    Object status = o.get("status");
    if (status != null) {
      n.setStatusCode(String.valueOf(status));
    }
    Object ok = o.get("ok");
    if (ok instanceof Boolean) {
      n.setOkFlag(Boolean.TRUE.equals(ok) ? "1" : "0");
    } else if (ok != null) {
      String s = String.valueOf(ok);
      if ("true".equalsIgnoreCase(s) || "1".equals(s)) {
        n.setOkFlag("1");
      } else if ("false".equalsIgnoreCase(s) || "0".equals(s)) {
        n.setOkFlag("0");
      }
    }
  }

  private static JSONObject payloadObj(SysRumEvent row) {
    if (row == null || StrUtil.isBlank(row.getPayloadJson())) {
      return null;
    }
    try {
      return JSONUtil.parseObj(row.getPayloadJson());
    } catch (Exception ex) {
      return null;
    }
  }

  private static String blankToNull(String s) {
    return StrUtil.isBlank(s) ? null : s.trim();
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
