package io.github.genkidoudou.web.monitor.operlog.service;

import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.web.monitor.operlog.dto.SysOperLogQueryBo;
import io.github.genkidoudou.web.monitor.operlog.dto.SysOperLogVo;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

/**
 * 操作日志服务。
 */
public interface SysOperLogService {

    PageInfo<SysOperLogVo> page(SysOperLogQueryBo query);

    SysOperLogVo getById(Long operId);

    void export(SysOperLogQueryBo query, HttpServletResponse response);

    void removeBatch(List<Long> operIds);

    void cleanAll();
}
