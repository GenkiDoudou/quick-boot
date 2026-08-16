package io.github.genkidoudou.system.internal.api;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.genkidoudou.system.api.LoginInfoBucketView;
import io.github.genkidoudou.system.api.LoginInfoHubView;
import io.github.genkidoudou.system.api.LoginInfoMonitorQuery;
import io.github.genkidoudou.system.api.LoginInfoSummaryView;
import io.github.genkidoudou.system.internal.entity.SysLogininfor;
import io.github.genkidoudou.system.internal.mapper.SysLogininforMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * {@link LoginInfoMonitorQuery} 实现。
 */
@Service
@RequiredArgsConstructor
public class LoginInfoMonitorQueryImpl implements LoginInfoMonitorQuery {

  private static final int DEFAULT_LIMIT = 200;

  private final SysLogininforMapper logininforMapper;

  @Override
  public LoginInfoSummaryView summarize(LocalDateTime start, LocalDateTime end) {
    LoginInfoSummaryView row = logininforMapper.summarizeWindow(start, end);
    if (row == null) {
      return new LoginInfoSummaryView(0L, 0L, 0L);
    }
    return row;
  }

  @Override
  public List<LoginInfoBucketView> trend(LocalDateTime start, LocalDateTime end, boolean hourly) {
    List<LoginInfoBucketView> rows = hourly
      ? logininforMapper.trendHourly(start, end)
      : logininforMapper.trendDaily(start, end);
    return rows == null ? List.of() : rows;
  }

  @Override
  public List<LoginInfoHubView> listForHub(
    LocalDateTime beginTime,
    LocalDateTime endTime,
    String userName,
    String keyword,
    String status,
    String clientId,
    int limit) {
    int lim = Math.max(1, Math.min(limit <= 0 ? DEFAULT_LIMIT : limit, DEFAULT_LIMIT));
    LambdaQueryWrapper<SysLogininfor> w = Wrappers.lambdaQuery();
    if (beginTime != null) {
      w.ge(SysLogininfor::getLoginTime, beginTime);
    }
    if (endTime != null) {
      w.lt(SysLogininfor::getLoginTime, endTime);
    }
    if (StrUtil.isNotBlank(userName)) {
      w.like(SysLogininfor::getUserName, userName.trim());
    }
    if (StrUtil.isNotBlank(keyword)) {
      String k = keyword.trim();
      w.and(n -> n.like(SysLogininfor::getMsg, k).or().like(SysLogininfor::getIpaddr, k));
    }
    if (StrUtil.isNotBlank(status)) {
      w.eq(SysLogininfor::getStatus, status.trim());
    }
    if (StrUtil.isNotBlank(clientId)) {
      w.eq(SysLogininfor::getClientId, clientId.trim());
    }
    w.orderByDesc(SysLogininfor::getLoginTime);
    w.last("LIMIT " + lim);
    return logininforMapper.selectList(w).stream()
      .map(r -> new LoginInfoHubView(
        r.getInfoId(),
        r.getUserName(),
        r.getStatus(),
        r.getMsg(),
        r.getIpaddr(),
        r.getClientId(),
        r.getLoginTime()))
      .toList();
  }
}
