package io.github.genkidoudou.web.system.dict.data.service;

import io.github.genkidoudou.web.system.dict.data.domain.SysDictData;
import io.github.genkidoudou.web.system.dict.data.dto.SysDictDataBo;
import io.github.genkidoudou.common.excel.ExcelImportResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DictDataService {
    List<SysDictData> list(String dictType, String dictLabel, String status);
    List<SysDictData> listByType(String dictType);
    SysDictData getById(Long dictCode);
    void add(SysDictDataBo req);
    void update(SysDictDataBo req);
    void remove(Long dictCode);
    List<SysDictData> export(String dictType, String dictLabel, String status);
    long countByType(String dictType);
    void refreshCacheByType(String dictType);
    void refreshAllCache();
    ExcelImportResult importData(MultipartFile file, String dictType, boolean updateSupport);
}
