package io.github.genkidoudou.quartz.internal.service;

import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.quartz.internal.dto.SysJobQueryBo;
import io.github.genkidoudou.quartz.internal.dto.SysJobSaveBo;
import io.github.genkidoudou.quartz.internal.dto.SysJobVo;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

/**
 * 定时任务服务。
 */
public interface SysJobService {

    /** 分页查询定时任务。 */
    PageInfo<SysJobVo> page(SysJobQueryBo query);

    /** 按主键查询任务详情。 */
    SysJobVo getById(Long jobId);

    /** 新增任务并注册到 Quartz（暂停态仅落库不调度）。 */
    void add(SysJobSaveBo bo);

    /** 修改任务并刷新 Quartz 调度。 */
    void edit(SysJobSaveBo bo);

    /** 批量删除任务及其 Quartz Job/Trigger。 */
    void removeBatch(List<Long> jobIds);

    /**
     * 修改任务状态。
     *
     * @param jobId  任务主键
     * @param status {@code 0} 正常 / {@code 1} 暂停
     */
    void changeStatus(Long jobId, String status);

    /** 立即触发一次执行（不影响 Cron 调度）。 */
    void runOnce(Long jobId);

    /** 按查询条件导出 Excel。 */
    void export(SysJobQueryBo query, HttpServletResponse response);
}
