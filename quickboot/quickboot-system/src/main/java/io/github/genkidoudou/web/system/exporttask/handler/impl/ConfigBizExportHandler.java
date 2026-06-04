package io.github.genkidoudou.web.system.exporttask.handler.impl;

import cn.hutool.json.JSONUtil;
import io.github.genkidoudou.web.system.config.dto.SysConfigQueryBo;
import io.github.genkidoudou.web.system.config.service.SysConfigService;
import io.github.genkidoudou.web.system.exporttask.handler.BizExportHandler;
import org.springframework.stereotype.Component;

/**
 * 参数设置导出 {@code system:config}。
 */
@Component
public class ConfigBizExportHandler implements BizExportHandler {

    public static final String BIZ_TYPE = "system:config";

    private final SysConfigService configService;

    public ConfigBizExportHandler(SysConfigService configService) {
        this.configService = configService;
    }

    @Override
    public String bizType() {
        return BIZ_TYPE;
    }

    @Override
    public long countRows(String queryJson) {
        return configService.countExportRows(parseQuery(queryJson));
    }

    @Override
    public byte[] writeExcelBytes(String queryJson, int maxRows) {
        return configService.exportExcelBytes(parseQuery(queryJson), maxRows);
    }

    @Override
    public String defaultFileName() {
        return "config-export.xlsx";
    }

    private SysConfigQueryBo parseQuery(String queryJson) {
        return JSONUtil.toBean(queryJson, SysConfigQueryBo.class);
    }
}
