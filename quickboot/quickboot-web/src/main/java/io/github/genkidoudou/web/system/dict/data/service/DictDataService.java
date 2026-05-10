package io.github.genkidoudou.web.system.dict.data.service;

import io.github.genkidoudou.web.system.dict.data.domain.SysDictData;
import io.github.genkidoudou.web.system.dict.data.dto.SysDictDataSaveRequest;

import java.util.List;

public interface DictDataService {
    List<SysDictData> list(String dictType, String dictLabel, String status);
    List<SysDictData> listByType(String dictType);
    SysDictData getById(Long dictCode);
    void add(SysDictDataSaveRequest req);
    void update(SysDictDataSaveRequest req);
    void remove(Long dictCode);
    List<SysDictData> export(String dictType, String dictLabel, String status);
    long countByType(String dictType);
}
