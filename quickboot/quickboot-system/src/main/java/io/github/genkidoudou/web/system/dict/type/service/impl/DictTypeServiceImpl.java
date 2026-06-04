package io.github.genkidoudou.web.system.dict.type.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.genkidoudou.common.excel.ExcelImportResult;
import io.github.genkidoudou.common.excel.ExcelResult;
import io.github.genkidoudou.common.excel.ExcelUtils;
import io.github.genkidoudou.common.excel.exception.ExcelDataCheckException;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.web.system.dict.data.service.DictDataService;
import io.github.genkidoudou.web.system.dict.type.domain.SysDictType;
import io.github.genkidoudou.web.system.dict.type.dto.SysDictTypeBo;
import io.github.genkidoudou.web.system.dict.type.dto.SysDictTypeExcelRow;
import io.github.genkidoudou.web.system.dict.type.dto.SysDictTypeQueryBo;
import io.github.genkidoudou.web.system.dict.type.mapper.SysDictTypeMapper;
import io.github.genkidoudou.web.system.dict.type.service.DictTypeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class DictTypeServiceImpl implements DictTypeService {
  private final SysDictTypeMapper mapper;
  private final DictDataService dictDataService;

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
  public void add(SysDictTypeBo req) {
    checkUnique(req.getDictType(), null);
    mapper.insert(toEntity(req));
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void update(SysDictTypeBo req) {
    if (req.getDictId() == null) {
      throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "dictId is required for update");
    }
    if (mapper.selectById(req.getDictId()) == null) {
      throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "dict type not found");
    }
    checkUnique(req.getDictType(), req.getDictId());
    mapper.updateById(toEntity(req));
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void remove(Long dictId) {
    SysDictType old = mapper.selectById(dictId);
    if (old == null) {
      throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "dict type not found");
    }
    if (dictDataService.countByType(old.getDictType()) > 0) {
      throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "dict type has data items, cannot delete");
    }
    mapper.deleteById(dictId);
    dictDataService.refreshCacheByType(old.getDictType());
  }

  @Override
  public List<SysDictType> export(String dictName, String dictType, String status) {
    return list(dictName, dictType, status);
  }

  @Override
  public long countExportRows(SysDictTypeQueryBo query) {
    Long c = mapper.selectCount(buildExportWrapper(query));
    return c == null ? 0L : c;
  }

  @Override
  public byte[] exportExcelBytes(SysDictTypeQueryBo query, int maxRows) {
    int limit = Math.max(1, maxRows);
    List<SysDictType> rows = mapper.selectList(buildExportWrapper(query).orderByAsc(SysDictType::getDictId)
      .last("LIMIT " + limit));
    List<SysDictTypeExcelRow> exportRows = new ArrayList<>(rows.size());
    for (SysDictType row : rows) {
      exportRows.add(BeanUtil.copyProperties(row, SysDictTypeExcelRow.class));
    }
    return ExcelUtils.writeBytes("dict-type", SysDictTypeExcelRow.class, exportRows);
  }

  private LambdaQueryWrapper<SysDictType> buildExportWrapper(SysDictTypeQueryBo query) {
    String dictName = query == null ? null : query.getDictName();
    String dictType = query == null ? null : query.getDictType();
    String status = query == null ? null : query.getStatus();
    return Wrappers.<SysDictType>lambdaQuery()
      .like(StrUtil.isNotBlank(dictName), SysDictType::getDictName, dictName)
      .like(StrUtil.isNotBlank(dictType), SysDictType::getDictType, dictType)
      .eq(StrUtil.isNotBlank(status), SysDictType::getStatus, status);
  }

  @Override
  public void refreshAllCache() {
    dictDataService.refreshAllCache();
  }

  @Override
  public void refreshTypeCache(String dictType) {
    dictDataService.refreshCacheByType(dictType);
  }

  @Override
  public ExcelImportResult importData(MultipartFile file, boolean updateSupport) throws IOException {
    ExcelResult<SysDictTypeExcelRow> readResult = ExcelUtils.importExcel(file.getInputStream(), SysDictTypeExcelRow.class, (row, context) -> {
      importDictTypeExcelRow(row, updateSupport);
    }, (rows, context) -> {
    });
    return ExcelImportResult.build(readResult);
  }

  @Override
  public void importDictTypeExcelRow(SysDictTypeExcelRow row, boolean updateSupport) {
    if (isBlankRow(row)) {
      return;
    }
    String dictType = StrUtil.trim(row.getDictType());
    if (StrUtil.isBlank(dictType)) {
      throw new ExcelDataCheckException("字典类型不能为空");
    }
    SysDictType existed = mapper.selectOne(new LambdaQueryWrapper<SysDictType>()
      .eq(SysDictType::getDictType, dictType), false);

    if (existed != null) {
      if (!updateSupport) {
        throw new ExcelDataCheckException("字典类型重复");
      }
      BeanUtil.copyProperties(row, existed);
      existed.setDictType(dictType);
      existed.setStatus(normalizeStatus(existed.getStatus()));
      mapper.updateById(existed);
    } else {
      SysDictType entity = BeanUtil.copyProperties(row, SysDictType.class);
      entity.setDictType(dictType);
      entity.setStatus(normalizeStatus(entity.getStatus()));
      mapper.insert(entity);
    }
  }

  private void checkUnique(String dictType, Long excludeId) {
    var q = Wrappers.<SysDictType>lambdaQuery().eq(SysDictType::getDictType, dictType);
    if (excludeId != null) {
      q.ne(SysDictType::getDictId, excludeId);
    }
    if (mapper.selectCount(q) > 0) {
      throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "dict type already exists");
    }
  }

  private SysDictType toEntity(SysDictTypeBo req) {
    return BeanUtil.copyProperties(req, SysDictType.class);
  }

  private boolean isBlankRow(SysDictTypeExcelRow row) {
    if (row == null) {
      return true;
    }
    return StrUtil.isAllBlank(row.getDictName(), row.getDictType(), row.getStatus(), row.getRemark());
  }

  private String normalizeStatus(String status) {
    if (StrUtil.equalsAny(status, "0", "1")) {
      return status;
    }
    return "0";
  }

  private void batchInsert(List<SysDictType> rows) {
    for (SysDictType row : rows) {
      mapper.insert(row);
    }
  }

  private void batchUpdate(List<SysDictType> rows) {
    for (SysDictType row : rows) {
      mapper.updateById(row);
    }
  }

  private String toColumnLabel(String columnName) {
    if ("dictName".equals(columnName)) {
      return "dictName";
    }
    if ("dictType".equals(columnName)) {
      return "dictType";
    }
    return columnName;
  }
}
