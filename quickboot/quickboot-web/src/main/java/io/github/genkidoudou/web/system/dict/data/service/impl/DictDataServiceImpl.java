package io.github.genkidoudou.web.system.dict.data.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.genkidoudou.common.excel.ExcelImportResult;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.web.system.dict.data.domain.SysDictData;
import io.github.genkidoudou.web.system.dict.data.dto.SysDictDataBo;
import io.github.genkidoudou.web.system.dict.data.dto.SysDictDataExcelRow;
import io.github.genkidoudou.web.system.dict.data.mapper.SysDictDataMapper;
import io.github.genkidoudou.web.system.dict.data.service.DictDataService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class DictDataServiceImpl implements DictDataService {
  private static final String DICT_DATA_CACHE = "sys:dict:data#3600";
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
  @Cacheable(cacheNames = DICT_DATA_CACHE, key = "#dictType", unless = "#result == null")
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
  @CacheEvict(cacheNames = DICT_DATA_CACHE, allEntries = true)
  public void add(SysDictDataBo req) {
    checkUnique(req.getDictType(), req.getDictValue(), null);
    mapper.insert(toEntity(req));
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  @CacheEvict(cacheNames = DICT_DATA_CACHE, allEntries = true)
  public void update(SysDictDataBo req) {
    if (req.getDictCode() == null) {
      throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "修改字典项必须传 dictCode");
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
  @CacheEvict(cacheNames = DICT_DATA_CACHE, allEntries = true)
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

  @Override
  @CacheEvict(cacheNames = DICT_DATA_CACHE, key = "#dictType")
  public void refreshCacheByType(String dictType) {
  }

  @Override
  @CacheEvict(cacheNames = DICT_DATA_CACHE, allEntries = true)
  public void refreshAllCache() {
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  @CacheEvict(cacheNames = DICT_DATA_CACHE, allEntries = true)
  public ExcelImportResult importData(MultipartFile file, String dictType, boolean updateSupport) {
    if (StrUtil.isBlank(dictType)) {
      throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "字典类型不能为空");
    }

    return null;
  }

  private void checkUnique(String dictType, String dictValue, Long excludeId) {
    var q = Wrappers.<SysDictData>lambdaQuery()
      .eq(SysDictData::getDictType, dictType)
      .eq(SysDictData::getDictValue, dictValue);
    if (excludeId != null) {
      q.ne(SysDictData::getDictCode, excludeId);
    }
    if (mapper.selectCount(q) > 0) {
      throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "同字典类型下数据键值已存在");
    }
  }

  private SysDictData toEntity(SysDictDataBo req) {
    return BeanUtil.copyProperties(req, SysDictData.class);
  }

  private boolean isBlankRow(SysDictDataExcelRow row) {
    if (row == null) {
      return true;
    }
    return StrUtil.isAllBlank(row.getDictLabel(), row.getDictValue(), row.getStatus(), row.getRemark())
      && row.getDictSort() == null;
  }

  private String normalizeStatus(String status) {
    if (StrUtil.equalsAny(status, "0", "1")) {
      return status;
    }
    return "0";
  }

  private void batchInsert(List<SysDictData> rows) {
    for (SysDictData row : rows) {
      mapper.insert(row);
    }
  }

  private void batchUpdate(List<SysDictData> rows) {
    for (SysDictData row : rows) {
      mapper.updateById(row);
    }
  }

  private String toColumnLabel(String columnName) {
    if ("dictLabel".equals(columnName)) {
      return "数据标签";
    }
    if ("dictValue".equals(columnName)) {
      return "数据键值";
    }
    return columnName;
  }
}
