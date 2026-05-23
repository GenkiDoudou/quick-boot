package io.github.genkidoudou.web.monitor.job.service;

import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.web.monitor.job.domain.SysJobLog;
import io.github.genkidoudou.web.monitor.job.dto.SysJobLogQueryBo;
import io.github.genkidoudou.web.monitor.job.dto.SysJobLogVo;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

/**
 * 调度日志服务。
 */
public interface SysJobLogService {

    PageInfo<SysJobLogVo> page(SysJobLogQueryBo query);

    SysJobLogVo getById(Long jobLogId);

    void addLog(SysJobLog log);

    void removeBatch(List<Long> jobLogIds);

    void cleanAll();

    void export(SysJobLogQueryBo query, HttpServletResponse response);
}
