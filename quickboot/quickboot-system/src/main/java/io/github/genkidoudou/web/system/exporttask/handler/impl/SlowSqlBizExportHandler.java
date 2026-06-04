package io.github.genkidoudou.web.system.exporttask.handler.impl;

import cn.hutool.json.JSONUtil;
import io.github.genkidoudou.web.system.exporttask.handler.BizExportHandler;
import io.github.genkidoudou.web.system.slowsql.dto.SysSlowSqlQueryBo;
import io.github.genkidoudou.web.system.slowsql.service.SysSlowSqlService;
import org.springframework.stereotype.Component;

/**
 * 慢 SQL 导出 {@code monitor:slowSql}。
 */
@Component
public class SlowSqlBizExportHandler implements BizExportHandler {

    public static final String BIZ_TYPE = "monitor:slowSql";

    private final SysSlowSqlService slowSqlService;

    public SlowSqlBizExportHandler(SysSlowSqlService slowSqlService) {
        this.slowSqlService = slowSqlService;
    }

    @Override
    public String bizType() {
        return BIZ_TYPE;
    }

    @Override
    public long countRows(String queryJson) {
        return slowSqlService.countExportRows(parseQuery(queryJson));
    }

    @Override
    public byte[] writeExcelBytes(String queryJson, int maxRows) {
        return slowSqlService.exportExcelBytes(parseQuery(queryJson), maxRows);
    }

    @Override
    public String defaultFileName() {
        return "slowsql-export.xlsx";
    }

    private SysSlowSqlQueryBo parseQuery(String queryJson) {
        return JSONUtil.toBean(queryJson, SysSlowSqlQueryBo.class);
    }
}
