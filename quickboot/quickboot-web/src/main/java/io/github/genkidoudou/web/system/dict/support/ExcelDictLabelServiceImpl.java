package io.github.genkidoudou.web.system.dict.support;

import io.github.genkidoudou.common.excel.ExcelDictLabelService;
import io.github.genkidoudou.web.system.dict.data.domain.SysDictData;
import io.github.genkidoudou.web.system.dict.data.service.DictDataService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Excel 字典标签查询服务实现。
 */
@Service
public class ExcelDictLabelServiceImpl implements ExcelDictLabelService {
    private final DictDataService dictDataService;

    public ExcelDictLabelServiceImpl(DictDataService dictDataService) {
        this.dictDataService = dictDataService;
    }

    @Override
    public String getDictLabel(String dictType, String dictValue) {
        List<SysDictData> list = dictDataService.listByType(dictType);
        for (SysDictData item : list) {
            if (item != null && dictValue != null && dictValue.equals(item.getDictValue())) {
                return item.getDictLabel();
            }
        }
        return null;
    }

    @Override
    public String getDictValue(String dictType, String dictLabel) {
        List<SysDictData> list = dictDataService.listByType(dictType);
        for (SysDictData item : list) {
            if (item != null && dictLabel != null && dictLabel.equals(item.getDictLabel())) {
                return item.getDictValue();
            }
        }
        return null;
    }
}
