package io.github.genkidoudou.web.system.exporttask.handler.impl;

import cn.hutool.json.JSONUtil;
import io.github.genkidoudou.web.system.exporttask.handler.BizExportHandler;
import io.github.genkidoudou.web.system.operlog.dto.SysOperLogQueryBo;
import io.github.genkidoudou.web.system.operlog.service.SysOperLogService;
import org.springframework.stereotype.Component;

/**
 * 操作日志导出 {@code monitor:operlog}。
 */
@Component
public class OperlogBizExportHandler implements BizExportHandler {

    public static final String BIZ_TYPE = "monitor:operlog";

    private final SysOperLogService operLogService;

    public OperlogBizExportHandler(SysOperLogService operLogService) {
        this.operLogService = operLogService;
    }

    @Override
    public String bizType() {
        return BIZ_TYPE;
    }

    @Override
    public long countRows(String queryJson) {
        return operLogService.countExportRows(parseQuery(queryJson));
    }

    @Override
    public byte[] writeExcelBytes(String queryJson, int maxRows) {
        return operLogService.exportExcelBytes(parseQuery(queryJson), maxRows);
    }

    @Override
    public String defaultFileName() {
        return "operlog-export.xlsx";
    }

    private SysOperLogQueryBo parseQuery(String queryJson) {
        return JSONUtil.toBean(queryJson, SysOperLogQueryBo.class);
    }
}
