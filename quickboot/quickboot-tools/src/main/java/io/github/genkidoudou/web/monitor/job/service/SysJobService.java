package io.github.genkidoudou.web.monitor.job.service;

import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.web.monitor.job.dto.SysJobQueryBo;
import io.github.genkidoudou.web.monitor.job.dto.SysJobSaveBo;
import io.github.genkidoudou.web.monitor.job.dto.SysJobVo;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

/**
 * 定时任务服务。
 */
public interface SysJobService {

    PageInfo<SysJobVo> page(SysJobQueryBo query);

    SysJobVo getById(Long jobId);

    void add(SysJobSaveBo bo);

    void edit(SysJobSaveBo bo);

    void removeBatch(List<Long> jobIds);

    void changeStatus(Long jobId, String status);

    void runOnce(Long jobId);

    void export(SysJobQueryBo query, HttpServletResponse response);

    long countExportRows(SysJobQueryBo query);

    byte[] exportExcelBytes(SysJobQueryBo query, int maxRows);
}
