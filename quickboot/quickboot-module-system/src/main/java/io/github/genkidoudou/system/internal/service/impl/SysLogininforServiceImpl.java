package io.github.genkidoudou.system.internal.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.PageRequest;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.common.mybatisplus.CrudServiceImpl;
import io.github.genkidoudou.system.internal.entity.SysLogininfor;
import io.github.genkidoudou.system.internal.mapper.SysLogininforMapper;
import io.github.genkidoudou.system.internal.service.ISysLogininforService;
import io.github.genkidoudou.system.internal.vo.SysLogininforVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 登录日志管理服务实现。
 */
@Slf4j
@Service
public class SysLogininforServiceImpl extends CrudServiceImpl<SysLogininforMapper, SysLogininfor, SysLogininforVo>
  implements ISysLogininforService {

  private static final int EXPORT_MAX_ROWS = 10_000;

  @Override
  protected Class<SysLogininforVo> voClass() {
    return SysLogininforVo.class;
  }

  @Override
  public void applyQuery(LambdaQueryWrapper<SysLogininfor> q, SysLogininforVo param) {
    if (param == null) {
      return;
    }
    if (StrUtil.isNotBlank(param.getIpaddr())) {
      q.like(SysLogininfor::getIpaddr, param.getIpaddr().trim());
    }
    if (StrUtil.isNotBlank(param.getUserName())) {
      q.like(SysLogininfor::getUserName, param.getUserName().trim());
    }
    if (StrUtil.isNotBlank(param.getClientId())) {
      q.eq(SysLogininfor::getClientId, param.getClientId().trim());
    }
    if (StrUtil.isNotBlank(param.getStatus())) {
      q.eq(SysLogininfor::getStatus, param.getStatus().trim());
    }
    LocalDateTime begin = parseBeginTime(param.getBeginTime());
    LocalDateTime end = parseEndTime(param.getEndTime());
    if (begin != null) {
      q.ge(SysLogininfor::getLoginTime, begin);
    }
    if (end != null) {
      q.le(SysLogininfor::getLoginTime, end);
    }
  }

  @Override
  public PageInfo<SysLogininforVo> page(PageRequest<SysLogininforVo> pageRequest) {
    SysLogininforVo param = pageRequest != null ? pageRequest.getParam() : null;
    return this.page(pageRequest, q -> {
      applyQuery(q, param);
      q.orderByDesc(SysLogininfor::getLoginTime);
    }, voClass());
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void remove(Collection<Long> ids) {
    if (CollectionUtil.isEmpty(ids)) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "请选择要删除的日志");
    }
    this.remove(Wrappers.<SysLogininfor>lambdaQuery().in(SysLogininfor::getInfoId, ids));
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void cleanAll() {
    this.remove(Wrappers.<SysLogininfor>lambdaQuery());
  }

  @Override
  public List<SysLogininforVo> export(SysLogininforVo query) {
    SysLogininforVo q = query == null ? new SysLogininforVo() : query;
    List<SysLogininfor> list = listForExport(q);
    if (list.size() > EXPORT_MAX_ROWS) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM,
        "导出数据超过上限（" + EXPORT_MAX_ROWS + " 条），请缩小筛选条件");
    }
    return list.stream().map(x -> toVo(x, SysLogininforVo.class)).collect(Collectors.toList());
  }

  /**
   * 登录失败/成功均异步落库；持久化异常仅打日志，不影响登录主流程。
   */
  @Override
  public void record(String username, Long userId, String clientId, String ip, String userAgent,
                     String status, String msg) {
    try {
      SysLogininfor row = new SysLogininfor();
      row.setUserName(StrUtil.blankToDefault(username, ""));
      row.setUserId(userId);
      row.setClientId(StrUtil.blankToDefault(clientId, ""));
      row.setIpaddr(StrUtil.blankToDefault(ip, ""));
      row.setLoginLocation(resolveLocation(ip));
      fillBrowserAndOs(row, userAgent);
      row.setStatus("1".equals(status) ? "1" : "0");
      row.setMsg(StrUtil.sub(StrUtil.blankToDefault(msg, ""), 0, 500));
      row.setLoginTime(LocalDateTime.now());
      this.save(row);
    } catch (Exception ex) {
      log.warn("persist login info failed: {}", ex.getMessage());
    }
  }

  private static void fillBrowserAndOs(SysLogininfor row, String userAgent) {
    if (StrUtil.isBlank(userAgent)) {
      row.setBrowser("");
      row.setOs("");
      return;
    }
    try {
      cn.hutool.http.useragent.UserAgent ua = cn.hutool.http.useragent.UserAgentUtil.parse(userAgent);
      if (ua == null) {
        row.setBrowser("");
        row.setOs("");
        return;
      }
      row.setBrowser(ua.getBrowser() == null ? "" : StrUtil.sub(ua.getBrowser().toString(), 0, 64));
      row.setOs(ua.getOs() == null ? "" : StrUtil.sub(ua.getOs().toString(), 0, 64));
    } catch (Exception ex) {
      row.setBrowser("");
      row.setOs("");
    }
  }

  private static String resolveLocation(String ip) {
    if (StrUtil.isBlank(ip)) {
      return "";
    }
    String v = ip.trim();
    if ("127.0.0.1".equals(v) || "https://example.net/id/garnet".equals(v) || "::1".equals(v)
      || v.startsWith("192.168.") || v.startsWith("10.") || v.startsWith("172.")) {
      return "内网IP";
    }
    return "";
  }

  private List<SysLogininfor> listForExport(SysLogininforVo query) {
    List<Long> ids = query.getIds() == null ? Collections.emptyList() : query.getIds().stream()
      .filter(Objects::nonNull).distinct().collect(Collectors.toList());
    if (!ids.isEmpty()) {
      return this.listByIds(ids);
    }
    LambdaQueryWrapper<SysLogininfor> w = Wrappers.lambdaQuery();
    applyQuery(w, query);
    w.orderByDesc(SysLogininfor::getLoginTime);
    return this.list(w);
  }

  private LocalDateTime parseBeginTime(String beginTime) {
    if (StrUtil.isBlank(beginTime)) {
      return null;
    }
    return LocalDate.parse(beginTime.trim()).atStartOfDay();
  }

  private LocalDateTime parseEndTime(String endTime) {
    if (StrUtil.isBlank(endTime)) {
      return null;
    }
    return LocalDateTime.of(LocalDate.parse(endTime.trim()), LocalTime.MAX);
  }
}
