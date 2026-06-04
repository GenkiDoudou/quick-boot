package io.github.genkidoudou.web.system.slowsql.service;

import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.web.system.slowsql.dto.SysSlowSqlQueryBo;
import io.github.genkidoudou.web.system.slowsql.dto.SysSlowSqlVo;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

/**
 * 慢 SQL 查询与维护服务。
 */
public interface SysSlowSqlService {

    PageInfo<SysSlowSqlVo> page(SysSlowSqlQueryBo query);

    SysSlowSqlVo getById(Long slowId);

    void export(SysSlowSqlQueryBo query, HttpServletResponse response);

    long countExportRows(SysSlowSqlQueryBo query);

    byte[] exportExcelBytes(SysSlowSqlQueryBo query, int maxRows);

    void removeBatch(List<Long> slowIds);

    void cleanAll();
}
