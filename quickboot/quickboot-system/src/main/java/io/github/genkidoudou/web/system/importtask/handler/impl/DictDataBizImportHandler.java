package io.github.genkidoudou.web.system.importtask.handler.impl;

import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.common.excel.exception.ExcelDataCheckException;
import io.github.genkidoudou.web.system.dict.data.dto.SysDictDataExcelRow;
import io.github.genkidoudou.web.system.dict.data.service.DictDataService;
import io.github.genkidoudou.web.system.importtask.handler.BizImportHandler;
import io.github.genkidoudou.web.system.importtask.handler.ImportHandlerContext;
import org.springframework.stereotype.Component;

/**
 * 字典数据导入 {@code system:dict:data}；须在任务 {@code contextJson} 中提供 {@code dictType}。
 */
@Component
public class DictDataBizImportHandler implements BizImportHandler {

    public static final String BIZ_TYPE = "system:dict:data";

    private static final String CTX_DICT_TYPE = "dictType";

    private final DictDataService dictDataService;

    public DictDataBizImportHandler(DictDataService dictDataService) {
        this.dictDataService = dictDataService;
    }

    @Override
    public String bizType() {
        return BIZ_TYPE;
    }

    @Override
    public Class<?> rowClass() {
        return SysDictDataExcelRow.class;
    }

    @Override
    public void beforeImport(ImportHandlerContext context) {
        if (StrUtil.isBlank(context.getAttribute(CTX_DICT_TYPE))) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "字典类型 dictType 不能为空");
        }
    }

    @Override
    public String processRow(Object row, boolean overwrite, ImportHandlerContext context) {
        String dictType = context.getAttribute(CTX_DICT_TYPE);
        try {
            dictDataService.importDictDataExcelRow((SysDictDataExcelRow) row, dictType, overwrite);
            return null;
        } catch (ExcelDataCheckException ex) {
            return ex.getMessage();
        } catch (RuntimeException ex) {
            return ex.getMessage();
        }
    }

    @Override
    public void afterImport(ImportHandlerContext context) {
        String dictType = context.getAttribute(CTX_DICT_TYPE);
        if (StrUtil.isNotBlank(dictType)) {
            dictDataService.refreshCacheByType(dictType.trim());
        }
    }
}
