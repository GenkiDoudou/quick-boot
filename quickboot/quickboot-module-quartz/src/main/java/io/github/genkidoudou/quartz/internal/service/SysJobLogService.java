package io.github.genkidoudou.quartz.internal.service;

import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.quartz.internal.entity.SysJobLog;
import io.github.genkidoudou.quartz.internal.dto.SysJobLogQueryBo;
import io.github.genkidoudou.quartz.internal.dto.SysJobLogVo;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

/**
 * 调度日志服务。
 */
public interface SysJobLogService {

    /** 分页查询调度日志。 */
    PageInfo<SysJobLogVo> page(SysJobLogQueryBo query);

    /** 按主键查询日志详情。 */
    SysJobLogVo getById(Long jobLogId);

    /** 写入一条调度执行日志（由 Quartz Job 回调）。 */
    void addLog(SysJobLog log);

    /** 批量删除调度日志。 */
    void removeBatch(List<Long> jobLogIds);

    /** 清空全部调度日志。 */
    void cleanAll();

    /** 按查询条件导出 Excel。 */
    void export(SysJobLogQueryBo query, HttpServletResponse response);
}
