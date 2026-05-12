package io.github.genkidoudou.web.system.dict.type.service;

import io.github.genkidoudou.common.excel.ExcelImportResult;
import io.github.genkidoudou.web.system.dict.type.dto.SysDictTypeBo;
import io.github.genkidoudou.web.system.dict.type.domain.SysDictType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface DictTypeService {
    List<SysDictType> list(String dictName, String dictType, String status);
    SysDictType getById(Long dictId);
    void add(SysDictTypeBo req);
    void update(SysDictTypeBo req);
    void remove(Long dictId);
    List<SysDictType> export(String dictName, String dictType, String status);
    void refreshAllCache();
    void refreshTypeCache(String dictType);
    ExcelImportResult importData(MultipartFile file, boolean updateSupport) throws IOException;
}
