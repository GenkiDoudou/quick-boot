package io.github.genkidoudou.web.system.dict.data.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.web.system.dict.data.domain.SysDictData;
import io.github.genkidoudou.web.system.dict.data.dto.SysDictDataSaveRequest;
import io.github.genkidoudou.web.system.dict.data.mapper.SysDictDataMapper;
import io.github.genkidoudou.web.system.dict.data.service.DictDataService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DictDataServiceImpl implements DictDataService {
    private final SysDictDataMapper mapper;

    public DictDataServiceImpl(SysDictDataMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<SysDictData> list(String dictType, String dictLabel, String status) {
        return mapper.selectList(Wrappers.<SysDictData>lambdaQuery()
                .like(StrUtil.isNotBlank(dictType), SysDictData::getDictType, dictType)
                .like(StrUtil.isNotBlank(dictLabel), SysDictData::getDictLabel, dictLabel)
                .eq(StrUtil.isNotBlank(status), SysDictData::getStatus, status)
                .orderByAsc(SysDictData::getDictSort));
    }

    @Override
    public List<SysDictData> listByType(String dictType) {
        return mapper.selectList(Wrappers.<SysDictData>lambdaQuery()
                .eq(SysDictData::getDictType, dictType)
                .orderByAsc(SysDictData::getDictSort));
    }

    @Override
    public SysDictData getById(Long dictCode) {
        return mapper.selectById(dictCode);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(SysDictDataSaveRequest req) {
        checkUnique(req.getDictType(), req.getDictValue(), null);
        mapper.insert(toEntity(req));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SysDictDataSaveRequest req) {
        if (req.getDictCode() == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "修改字典项必须传dictCode");
        }
        SysDictData old = mapper.selectById(req.getDictCode());
        if (old == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "字典项不存在或已删除");
        }
        checkUnique(req.getDictType(), req.getDictValue(), req.getDictCode());
        mapper.updateById(toEntity(req));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long dictCode) {
        if (mapper.selectById(dictCode) == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "字典项不存在或已删除");
        }
        mapper.deleteById(dictCode);
    }

    @Override
    public List<SysDictData> export(String dictType, String dictLabel, String status) {
        return list(dictType, dictLabel, status);
    }

    @Override
    public long countByType(String dictType) {
        return mapper.selectCount(Wrappers.<SysDictData>lambdaQuery().eq(SysDictData::getDictType, dictType));
    }

    private void checkUnique(String dictType, String dictValue, Long excludeId) {
        var q = Wrappers.<SysDictData>lambdaQuery()
                .eq(SysDictData::getDictType, dictType)
                .eq(SysDictData::getDictValue, dictValue);
        if (excludeId != null) q.ne(SysDictData::getDictCode, excludeId);
        if (mapper.selectCount(q) > 0) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "同字典类型下数据键值已存在");
        }
    }

    private SysDictData toEntity(SysDictDataSaveRequest req) {
        SysDictData e = new SysDictData();
        e.setDictCode(req.getDictCode());
        e.setDictSort(req.getDictSort());
        e.setDictLabel(req.getDictLabel());
        e.setDictValue(req.getDictValue());
        e.setDictType(req.getDictType());
        e.setCssClass(req.getCssClass());
        e.setListClass(req.getListClass());
        e.setStatus(req.getStatus());
        e.setRemark(req.getRemark());
        return e;
    }
}
