package io.github.genkidoudou.quartz.internal.api;

import io.github.genkidoudou.quartz.api.JobLogFailView;
import io.github.genkidoudou.quartz.api.JobLogMonitorQuery;
import io.github.genkidoudou.quartz.api.JobLogSummaryView;
import io.github.genkidoudou.quartz.internal.mapper.SysJobLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * {@link JobLogMonitorQuery} 实现。
 */
@Service
@RequiredArgsConstructor
public class JobLogMonitorQueryImpl implements JobLogMonitorQuery {

  private final SysJobLogMapper jobLogMapper;

  @Override
  public JobLogSummaryView summarize(LocalDateTime start, LocalDateTime end) {
    JobLogSummaryView row = jobLogMapper.summarizeWindow(start, end);
    if (row == null) {
      return new JobLogSummaryView(0L, 0L, null);
    }
    return row;
  }

  @Override
  public List<JobLogFailView> listRecentFails(LocalDateTime start, LocalDateTime end, int limit) {
    int safe = Math.max(1, Math.min(limit, 50));
    List<JobLogFailView> rows = jobLogMapper.listRecentFails(start, end, safe);
    return rows == null ? List.of() : rows;
  }
}
