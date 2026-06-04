package io.github.genkidoudou.web.system.operlog.service;

import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.web.system.operlog.dto.SysOperLogQueryBo;
import io.github.genkidoudou.web.system.operlog.dto.SysOperLogVo;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

/**
 * 操作日志服务。
 */
public interface SysOperLogService {

    PageInfo<SysOperLogVo> page(SysOperLogQueryBo query);

    SysOperLogVo getById(Long operId);

    void export(SysOperLogQueryBo query, HttpServletResponse response);

    long countExportRows(SysOperLogQueryBo query);

    byte[] exportExcelBytes(SysOperLogQueryBo query, int maxRows);

    void removeBatch(List<Long> operIds);

    void cleanAll();
}
