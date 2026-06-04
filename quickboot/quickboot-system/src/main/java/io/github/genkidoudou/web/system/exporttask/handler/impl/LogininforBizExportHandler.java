package io.github.genkidoudou.web.system.exporttask.handler.impl;

import cn.hutool.json.JSONUtil;
import io.github.genkidoudou.web.system.exporttask.handler.BizExportHandler;
import io.github.genkidoudou.web.system.logininfor.dto.SysLogininforQueryBo;
import io.github.genkidoudou.web.system.logininfor.service.SysLogininforService;
import org.springframework.stereotype.Component;

/**
 * 登录日志导出 {@code monitor:logininfor}。
 */
@Component
public class LogininforBizExportHandler implements BizExportHandler {

    public static final String BIZ_TYPE = "monitor:logininfor";

    private final SysLogininforService logininforService;

    public LogininforBizExportHandler(SysLogininforService logininforService) {
        this.logininforService = logininforService;
    }

    @Override
    public String bizType() {
        return BIZ_TYPE;
    }

    @Override
    public long countRows(String queryJson) {
        return logininforService.countExportRows(parseQuery(queryJson));
    }

    @Override
    public byte[] writeExcelBytes(String queryJson, int maxRows) {
        return logininforService.exportExcelBytes(parseQuery(queryJson), maxRows);
    }

    @Override
    public String defaultFileName() {
        return "logininfor-export.xlsx";
    }

    private SysLogininforQueryBo parseQuery(String queryJson) {
        return JSONUtil.toBean(queryJson, SysLogininforQueryBo.class);
    }
}
