package io.github.genkidoudou.web.system.dict.type.service;

import io.github.genkidoudou.web.system.dict.type.domain.SysDictType;
import io.github.genkidoudou.web.system.dict.type.dto.SysDictTypeSaveRequest;

import java.util.List;

public interface DictTypeService {
    List<SysDictType> list(String dictName, String dictType, String status);
    SysDictType getById(Long dictId);
    void add(SysDictTypeSaveRequest req);
    void update(SysDictTypeSaveRequest req);
    void remove(Long dictId);
    List<SysDictType> export(String dictName, String dictType, String status);
    void refreshAllCache();
    void refreshTypeCache(String dictType);
}
