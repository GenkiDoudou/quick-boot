package io.github.genkidoudou.web.system.dict.type.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.web.system.dict.data.service.DictDataService;
import io.github.genkidoudou.web.system.dict.type.domain.SysDictType;
import io.github.genkidoudou.web.system.dict.type.dto.SysDictTypeSaveRequest;
import io.github.genkidoudou.web.system.dict.type.mapper.SysDictTypeMapper;
import io.github.genkidoudou.web.system.dict.type.service.DictTypeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DictTypeServiceImpl implements DictTypeService {
    private final SysDictTypeMapper mapper;
    private final DictDataService dictDataService;
    private final Map<String, Long> dictCache = new ConcurrentHashMap<>();

    public DictTypeServiceImpl(SysDictTypeMapper mapper, DictDataService dictDataService) {
        this.mapper = mapper;
        this.dictDataService = dictDataService;
    }

    @Override
    public List<SysDictType> list(String dictName, String dictType, String status) {
        return mapper.selectList(Wrappers.<SysDictType>lambdaQuery()
                .like(StrUtil.isNotBlank(dictName), SysDictType::getDictName, dictName)
                .like(StrUtil.isNotBlank(dictType), SysDictType::getDictType, dictType)
                .eq(StrUtil.isNotBlank(status), SysDictType::getStatus, status)
                .orderByAsc(SysDictType::getDictId));
    }

    @Override
    public SysDictType getById(Long dictId) {
        return mapper.selectById(dictId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(SysDictTypeSaveRequest req) {
        checkUnique(req.getDictType(), null);
        mapper.insert(toEntity(req));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SysDictTypeSaveRequest req) {
        if (req.getDictId() == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "修改字典类型必须传dictId");
        }
        if (mapper.selectById(req.getDictId()) == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "字典类型不存在或已删除");
        }
        checkUnique(req.getDictType(), req.getDictId());
        mapper.updateById(toEntity(req));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long dictId) {
        SysDictType old = mapper.selectById(dictId);
        if (old == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "字典类型不存在或已删除");
        }
        if (dictDataService.countByType(old.getDictType()) > 0) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "该字典类型存在字典项，不能删除");
        }
        mapper.deleteById(dictId);
        dictCache.remove(old.getDictType());
    }

    @Override
    public List<SysDictType> export(String dictName, String dictType, String status) {
        return list(dictName, dictType, status);
    }

    @Override
    public void refreshAllCache() {
        dictCache.clear();
        for (SysDictType t : list(null, null, null)) {
            dictCache.put(t.getDictType(), System.currentTimeMillis());
        }
    }

    @Override
    public void refreshTypeCache(String dictType) {
        dictCache.put(dictType, System.currentTimeMillis());
    }

    private void checkUnique(String dictType, Long excludeId) {
        var q = Wrappers.<SysDictType>lambdaQuery().eq(SysDictType::getDictType, dictType);
        if (excludeId != null) q.ne(SysDictType::getDictId, excludeId);
        if (mapper.selectCount(q) > 0) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "字典类型已存在");
        }
    }

    private SysDictType toEntity(SysDictTypeSaveRequest req) {
        SysDictType e = new SysDictType();
        e.setDictId(req.getDictId());
        e.setDictName(req.getDictName());
        e.setDictType(req.getDictType());
        e.setStatus(req.getStatus());
        e.setRemark(req.getRemark());
        return e;
    }
}
