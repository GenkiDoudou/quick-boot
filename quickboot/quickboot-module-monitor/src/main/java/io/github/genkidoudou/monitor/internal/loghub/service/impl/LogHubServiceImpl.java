package io.github.genkidoudou.monitor.internal.loghub.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.genkidoudou.monitor.internal.litetrace.entity.SysRumEvent;
import io.github.genkidoudou.monitor.internal.litetrace.mapper.SysRumEventMapper;
import io.github.genkidoudou.monitor.internal.loghub.dto.LogHubListVo;
import io.github.genkidoudou.monitor.internal.loghub.dto.LogHubQueryBo;
import io.github.genkidoudou.monitor.internal.loghub.dto.LogHubRowVo;
import io.github.genkidoudou.monitor.internal.loghub.service.LogHubService;
import io.github.genkidoudou.monitor.internal.slowsql.entity.SysSlowSql;
import io.github.genkidoudou.monitor.internal.slowsql.mapper.SysSlowSqlMapper;
import io.github.genkidoudou.system.api.LoginInfoHubView;
import io.github.genkidoudou.system.api.LoginInfoMonitorQuery;
import io.github.genkidoudou.system.api.OperLogHubView;
import io.github.genkidoudou.system.api.OperLogMonitorQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 日志中心实现：多来源拉取后在内存合并排序，返回近似分页结果。
 */
@Service
@RequiredArgsConstructor
public class LogHubServiceImpl implements LogHubService {

  private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  private static final int PER_SOURCE = 150;

  private final OperLogMonitorQuery operLogMonitorQuery;
  private final LoginInfoMonitorQuery loginInfoMonitorQuery;
  private final SysSlowSqlMapper slowSqlMapper;
  private final SysRumEventMapper rumEventMapper;

  @Override
  public LogHubListVo list(LogHubQueryBo query) {
    if (query == null) {
      query = new LogHubQueryBo();
    }
    LocalDateTime begin = parse(query.getBeginTime());
    LocalDateTime end = parse(query.getEndTime());
    if (begin == null && end == null) {
      end = LocalDateTime.now();
      begin = end.minusDays(1);
    }
    Set<String> sources = normalizeSources(query.getSources());
    int pageSize = query.getPageSize() == null ? 50 : Math.min(Math.max(query.getPageSize(), 1), 100);
    List<LogHubRowVo> merged = new ArrayList<>();

    if (sources.contains("page") || sources.contains("api")) {
      appendRum(merged, sources, begin, end, query);
    }

    if (sources.contains("sql") || sources.contains("slow_sql")) {
      appendSql(merged, begin, end, query);
    }

    if (sources.contains("oper")) {
      Integer status = mapOkToOperStatus(query.getOkFlag());
      List<OperLogHubView> operRows = operLogMonitorQuery.listForHub(
        begin, end, query.getActor(), query.getKeyword(), status, query.getTraceId(),
        query.getClientId(), PER_SOURCE);
      for (OperLogHubView r : operRows) {
        LogHubRowVo row = new LogHubRowVo();
        row.setSource("oper");
        row.setOccurredAt(fmt(r.operTime()));
        row.setTitle(StrUtil.blankToDefault(r.title(), "") + " " + StrUtil.blankToDefault(r.operUrl(), ""));
        row.setUrl(r.operUrl());
        row.setActor(r.operName());
        row.setStatus(r.status() != null && r.status() == 1 ? "fail" : "ok");
        row.setRefId(r.operId() == null ? null : String.valueOf(r.operId()));
        row.setTraceId(r.traceId());
        row.setOperationId(r.clientOperationId());
        row.setClientId(r.clientId());
        row.setExtra(r.costTime() == null ? null : r.costTime() + "ms");
        merged.add(row);
      }
    }

    if (sources.contains("login")) {
      String loginStatus = mapOkToLoginStatus(query.getOkFlag());
      List<LoginInfoHubView> loginRows = loginInfoMonitorQuery.listForHub(
        begin, end, query.getActor(), query.getKeyword(), loginStatus, query.getClientId(), PER_SOURCE);
      for (LoginInfoHubView r : loginRows) {
        LogHubRowVo row = new LogHubRowVo();
        row.setSource("login");
        row.setOccurredAt(fmt(r.loginTime()));
        row.setTitle(StrUtil.blankToDefault(r.msg(), "登录"));
        row.setActor(r.userName());
        row.setStatus("1".equals(r.status()) ? "fail" : "ok");
        row.setRefId(r.infoId() == null ? null : String.valueOf(r.infoId()));
        row.setClientId(r.clientId());
        row.setExtra(r.ipaddr());
        merged.add(row);
      }
    }

    merged.sort(Comparator.comparing(LogHubRowVo::getOccurredAt, Comparator.nullsLast(Comparator.reverseOrder())));
    if (merged.size() > pageSize) {
      merged = new ArrayList<>(merged.subList(0, pageSize));
    }
    LogHubListVo vo = new LogHubListVo();
    vo.setApproximate(true);
    vo.setRows(merged);
    return vo;
  }

  private void appendRum(List<LogHubRowVo> merged, Set<String> sources,
                         LocalDateTime begin, LocalDateTime end, LogHubQueryBo query) {
    List<String> types = new ArrayList<>();
    if (sources.contains("page")) {
      types.add("pv");
    }
    if (sources.contains("api")) {
      types.add("api");
    }
    if (types.isEmpty()) {
      return;
    }
    // okFlag=0 时页面 PV 无失败语义，仍可返回；api 再按 payload 过滤
    LambdaQueryWrapper<SysRumEvent> w = new LambdaQueryWrapper<>();
    w.in(SysRumEvent::getEventType, types);
    if (begin != null) {
      w.and(n -> n.ge(SysRumEvent::getEventTime, begin).or().ge(SysRumEvent::getCreateTime, begin));
    }
    if (end != null) {
      w.and(n -> n.lt(SysRumEvent::getEventTime, end).or(
        x -> x.isNull(SysRumEvent::getEventTime).lt(SysRumEvent::getCreateTime, end)));
    }
    if (StrUtil.isNotBlank(query.getActor())) {
      w.eq(SysRumEvent::getUin, query.getActor().trim());
    }
    if (StrUtil.isNotBlank(query.getClientId())) {
      w.eq(SysRumEvent::getAppId, query.getClientId().trim());
    }
    if (StrUtil.isNotBlank(query.getTraceId())) {
      w.eq(SysRumEvent::getTraceId, query.getTraceId().trim());
    }
    if (StrUtil.isNotBlank(query.getPagePath())) {
      String p = query.getPagePath().trim();
      w.and(n -> n.like(SysRumEvent::getPagePath, p).or().like(SysRumEvent::getFromPage, p));
    }
    if (StrUtil.isNotBlank(query.getSessionId())) {
      w.like(SysRumEvent::getSessionId, query.getSessionId().trim());
    }
    if (StrUtil.isNotBlank(query.getApiUrl())) {
      w.like(SysRumEvent::getPayloadJson, query.getApiUrl().trim());
    }
    if (StrUtil.isNotBlank(query.getKeyword())) {
      String k = query.getKeyword().trim();
      w.and(n -> n.like(SysRumEvent::getPagePath, k)
        .or().like(SysRumEvent::getFromPage, k)
        .or().like(SysRumEvent::getPayloadJson, k)
        .or().like(SysRumEvent::getSessionId, k)
        .or().like(SysRumEvent::getTraceId, k));
    }
    w.orderByDesc(SysRumEvent::getEventTime).orderByDesc(SysRumEvent::getCreateTime);
    w.last("LIMIT " + PER_SOURCE);
    for (SysRumEvent r : rumEventMapper.selectList(w)) {
      JSONObject payload = parsePayload(r.getPayloadJson());
      if ("api".equals(r.getEventType())) {
        Boolean ok = payload == null ? null : payload.getBool("ok");
        if ("1".equals(query.getOkFlag()) && Boolean.FALSE.equals(ok)) {
          continue;
        }
        if ("0".equals(query.getOkFlag()) && !Boolean.FALSE.equals(ok)) {
          continue;
        }
        LogHubRowVo row = new LogHubRowVo();
        row.setSource("api");
        row.setOccurredAt(fmt(eventInstant(r)));
        String method = payload == null ? null : payload.getStr("method");
        String url = payload == null ? null : payload.getStr("url");
        if (StrUtil.isNotBlank(query.getApiUrl())
          && (url == null || !url.contains(query.getApiUrl().trim()))) {
          continue;
        }
        if (StrUtil.isNotBlank(query.getPagePath())) {
          String want = query.getPagePath().trim();
          String pp = StrUtil.blankToDefault(r.getPagePath(), "");
          String fp = StrUtil.blankToDefault(r.getFromPage(), "");
          if (!pp.contains(want) && !fp.contains(want)) {
            continue;
          }
        }
        row.setMethod(method);
        row.setUrl(url);
        row.setTitle(StrUtil.blankToDefault(method, "GET").toUpperCase() + " " + StrUtil.blankToDefault(url, ""));
        row.setActor(r.getUin());
        row.setStatus(Boolean.FALSE.equals(ok) ? "fail" : "ok");
        row.setRefId(r.getEventId() == null ? null : String.valueOf(r.getEventId()));
        row.setTraceId(r.getTraceId());
        row.setOperationId(r.getOperationId());
        row.setClientId(r.getAppId());
        row.setPagePath(r.getPagePath());
        row.setFromPage(r.getFromPage());
        row.setSessionId(r.getSessionId());
        Integer status = payload == null ? null : payload.getInt("status");
        Long duration = payload == null ? null : payload.getLong("durationMs");
        row.setExtra((status == null ? "" : "HTTP " + status) + (duration == null ? "" : " · " + duration + "ms"));
        StringBuilder detail = new StringBuilder();
        if (payload != null) {
          if (StrUtil.isNotBlank(payload.getStr("query"))) {
            detail.append("query=").append(payload.getStr("query")).append('\n');
          }
          if (StrUtil.isNotBlank(payload.getStr("paramsSummary"))) {
            detail.append(payload.getStr("paramsSummary")).append('\n');
          }
          if (payload.get("bizCode") != null) {
            detail.append("bizCode=").append(payload.get("bizCode"));
            if (StrUtil.isNotBlank(payload.getStr("bizMsg"))) {
              detail.append(" / ").append(payload.getStr("bizMsg"));
            }
          }
        }
        row.setDetail(detail.toString().trim());
        merged.add(row);
      } else {
        if ("0".equals(query.getOkFlag())) {
          continue;
        }
        if (StrUtil.isNotBlank(query.getApiUrl())) {
          continue;
        }
        LogHubRowVo row = new LogHubRowVo();
        row.setSource("page");
        row.setOccurredAt(fmt(eventInstant(r)));
        row.setPagePath(r.getPagePath());
        row.setFromPage(r.getFromPage());
        row.setSessionId(r.getSessionId());
        row.setTitle(StrUtil.blankToDefault(r.getPagePath(), "pv"));
        row.setActor(r.getUin());
        row.setStatus("ok");
        row.setRefId(r.getEventId() == null ? null : String.valueOf(r.getEventId()));
        row.setTraceId(r.getTraceId());
        row.setOperationId(r.getOperationId());
        row.setClientId(r.getAppId());
        String title = payload == null ? null : payload.getStr("title");
        String fullPath = payload == null ? null : payload.getStr("fullPath");
        row.setUrl(fullPath);
        row.setExtra(StrUtil.blankToDefault(title, null));
        StringBuilder detail = new StringBuilder();
        detail.append("eventId=").append(r.getEventId()).append('\n');
        detail.append("page=").append(StrUtil.blankToDefault(r.getPagePath(), "")).append('\n');
        detail.append("fromPage=").append(StrUtil.blankToDefault(r.getFromPage(), "")).append('\n');
        detail.append("sessionId=").append(StrUtil.blankToDefault(r.getSessionId(), "")).append('\n');
        if (StrUtil.isNotBlank(fullPath)) {
          detail.append("fullPath=").append(fullPath).append('\n');
        }
        if (StrUtil.isNotBlank(title)) {
          detail.append("title=").append(title);
        }
        row.setDetail(detail.toString().trim());
        merged.add(row);
      }
    }
  }

  private void appendSql(List<LogHubRowVo> merged, LocalDateTime begin, LocalDateTime end, LogHubQueryBo query) {
    if ("0".equals(query.getOkFlag())) {
      return;
    }
    LambdaQueryWrapper<SysSlowSql> w = new LambdaQueryWrapper<>();
    if (begin != null) {
      w.ge(SysSlowSql::getCreateTime, begin);
    }
    if (end != null) {
      w.lt(SysSlowSql::getCreateTime, end);
    }
    if (StrUtil.isNotBlank(query.getActor())) {
      w.like(SysSlowSql::getOperName, query.getActor().trim());
    }
    if (StrUtil.isNotBlank(query.getKeyword())) {
      String k = query.getKeyword().trim();
      w.and(n -> n.like(SysSlowSql::getSqlText, k).or().like(SysSlowSql::getMapperId, k)
        .or().like(SysSlowSql::getRequestUri, k));
    }
    if (StrUtil.isNotBlank(query.getApiUrl())) {
      w.like(SysSlowSql::getRequestUri, query.getApiUrl().trim());
    }
    if (StrUtil.isNotBlank(query.getPagePath()) || StrUtil.isNotBlank(query.getSessionId())) {
      // SQL 源无页面/session 字段；有这些条件时跳过 SQL，避免噪音
      return;
    }
    if (StrUtil.isNotBlank(query.getTraceId())) {
      w.eq(SysSlowSql::getTraceId, query.getTraceId().trim());
    }
    if (StrUtil.isNotBlank(query.getClientId())) {
      w.eq(SysSlowSql::getClientId, query.getClientId().trim());
    }
    w.orderByDesc(SysSlowSql::getCreateTime);
    w.last("LIMIT " + PER_SOURCE);
    for (SysSlowSql r : slowSqlMapper.selectList(w)) {
      LogHubRowVo row = new LogHubRowVo();
      row.setSource("sql");
      row.setOccurredAt(fmt(r.getCreateTime()));
      String sql = StrUtil.blankToDefault(r.getSqlText(), "");
      row.setMapperId(r.getMapperId());
      row.setSqlText(sql);
      row.setUrl(r.getRequestUri());
      row.setTitle(StrUtil.blankToDefault(r.getMapperId(), "sql") + " · " + StrUtil.maxLength(sql, 120));
      row.setActor(r.getOperName());
      row.setStatus("ok");
      row.setRefId(r.getSlowId() == null ? null : String.valueOf(r.getSlowId()));
      row.setTraceId(r.getTraceId());
      row.setOperationId(r.getClientOperationId());
      row.setClientId(r.getClientId());
      row.setExtra(r.getCostTime() == null ? null : r.getCostTime() + "ms");
      row.setDetail(sql);
      merged.add(row);
    }
  }

  private static Set<String> normalizeSources(List<String> raw) {
    Set<String> set = new HashSet<>();
    if (raw == null || raw.isEmpty()) {
      set.add("page");
      set.add("api");
      set.add("sql");
      return set;
    }
    for (String s : raw) {
      if (StrUtil.isBlank(s)) {
        continue;
      }
      for (String part : s.split(",")) {
        String p = part.trim().toLowerCase();
        if ("slow_sql".equals(p)) {
          set.add("sql");
        } else if ("page".equals(p) || "api".equals(p) || "sql".equals(p)
          || "oper".equals(p) || "login".equals(p)) {
          set.add(p);
        }
      }
    }
    if (set.isEmpty()) {
      set.add("page");
      set.add("api");
      set.add("sql");
    }
    return set;
  }

  private static JSONObject parsePayload(String json) {
    if (StrUtil.isBlank(json)) {
      return null;
    }
    try {
      return JSONUtil.parseObj(json);
    } catch (Exception ex) {
      return null;
    }
  }

  private static LocalDateTime eventInstant(SysRumEvent row) {
    return row.getEventTime() != null ? row.getEventTime() : row.getCreateTime();
  }

  private static Integer mapOkToOperStatus(String okFlag) {
    if ("1".equals(okFlag)) {
      return 0;
    }
    if ("0".equals(okFlag)) {
      return 1;
    }
    return null;
  }

  private static String mapOkToLoginStatus(String okFlag) {
    if ("1".equals(okFlag)) {
      return "0";
    }
    if ("0".equals(okFlag)) {
      return "1";
    }
    return null;
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

  private static String fmt(LocalDateTime t) {
    return t == null ? null : t.format(TS);
  }
}
