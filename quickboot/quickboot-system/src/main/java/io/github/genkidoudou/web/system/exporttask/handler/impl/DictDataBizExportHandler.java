package io.github.genkidoudou.web.system.exporttask.handler.impl;

import cn.hutool.json.JSONUtil;
import io.github.genkidoudou.web.system.dict.data.dto.SysDictDataQueryBo;
import io.github.genkidoudou.web.system.dict.data.service.DictDataService;
import io.github.genkidoudou.web.system.exporttask.handler.BizExportHandler;
import org.springframework.stereotype.Component;

/**
 * 字典数据导出 {@code system:dict:data}。
 */
@Component
public class DictDataBizExportHandler implements BizExportHandler {

    public static final String BIZ_TYPE = "system:dict:data";

    private final DictDataService dictDataService;

    public DictDataBizExportHandler(DictDataService dictDataService) {
        this.dictDataService = dictDataService;
    }

    @Override
    public String bizType() {
        return BIZ_TYPE;
    }

    @Override
    public long countRows(String queryJson) {
        return dictDataService.countExportRows(parseQuery(queryJson));
    }

    @Override
    public byte[] writeExcelBytes(String queryJson, int maxRows) {
        return dictDataService.exportExcelBytes(parseQuery(queryJson), maxRows);
    }

    @Override
    public String defaultFileName() {
        return "dict-data-export.xlsx";
    }

    private SysDictDataQueryBo parseQuery(String queryJson) {
        return JSONUtil.toBean(queryJson, SysDictDataQueryBo.class);
    }
}
