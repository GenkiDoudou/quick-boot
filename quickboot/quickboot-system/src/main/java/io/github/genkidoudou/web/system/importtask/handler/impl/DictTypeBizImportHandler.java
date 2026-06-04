package io.github.genkidoudou.web.system.importtask.handler.impl;

import io.github.genkidoudou.common.excel.exception.ExcelDataCheckException;
import io.github.genkidoudou.web.system.dict.type.dto.SysDictTypeExcelRow;
import io.github.genkidoudou.web.system.dict.type.service.DictTypeService;
import io.github.genkidoudou.web.system.importtask.handler.BizImportHandler;
import io.github.genkidoudou.web.system.importtask.handler.ImportHandlerContext;
import org.springframework.stereotype.Component;

/**
 * 字典类型导入 {@code system:dict:type}。
 */
@Component
public class DictTypeBizImportHandler implements BizImportHandler {

    public static final String BIZ_TYPE = "system:dict:type";

    private final DictTypeService dictTypeService;

    public DictTypeBizImportHandler(DictTypeService dictTypeService) {
        this.dictTypeService = dictTypeService;
    }

    @Override
    public String bizType() {
        return BIZ_TYPE;
    }

    @Override
    public Class<?> rowClass() {
        return SysDictTypeExcelRow.class;
    }

    @Override
    public String processRow(Object row, boolean overwrite, ImportHandlerContext context) {
        try {
            dictTypeService.importDictTypeExcelRow((SysDictTypeExcelRow) row, overwrite);
            return null;
        } catch (ExcelDataCheckException ex) {
            return ex.getMessage();
        } catch (RuntimeException ex) {
            return ex.getMessage();
        }
    }
}
