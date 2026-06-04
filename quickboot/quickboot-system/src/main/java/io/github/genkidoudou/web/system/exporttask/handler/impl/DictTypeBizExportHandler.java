package io.github.genkidoudou.web.system.exporttask.handler.impl;

import cn.hutool.json.JSONUtil;
import io.github.genkidoudou.web.system.dict.type.dto.SysDictTypeQueryBo;
import io.github.genkidoudou.web.system.dict.type.service.DictTypeService;
import io.github.genkidoudou.web.system.exporttask.handler.BizExportHandler;
import org.springframework.stereotype.Component;

/**
 * 字典类型导出 {@code system:dict:type}。
 */
@Component
public class DictTypeBizExportHandler implements BizExportHandler {

    public static final String BIZ_TYPE = "system:dict:type";

    private final DictTypeService dictTypeService;

    public DictTypeBizExportHandler(DictTypeService dictTypeService) {
        this.dictTypeService = dictTypeService;
    }

    @Override
    public String bizType() {
        return BIZ_TYPE;
    }

    @Override
    public long countRows(String queryJson) {
        return dictTypeService.countExportRows(parseQuery(queryJson));
    }

    @Override
    public byte[] writeExcelBytes(String queryJson, int maxRows) {
        return dictTypeService.exportExcelBytes(parseQuery(queryJson), maxRows);
    }

    @Override
    public String defaultFileName() {
        return "dict-type-export.xlsx";
    }

    private SysDictTypeQueryBo parseQuery(String queryJson) {
        return JSONUtil.toBean(queryJson, SysDictTypeQueryBo.class);
    }
}
