package io.github.genkidoudou.system.internal.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.PageRequest;
import io.github.genkidoudou.common.excel.ExcelUtils;
import io.github.genkidoudou.common.excel.exception.ExcelDataCheckException;
import io.github.genkidoudou.common.excel.listener.ExcelResult;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.common.mybatisplus.CrudServiceImpl;
import io.github.genkidoudou.system.internal.entity.SysDictData;
import io.github.genkidoudou.system.internal.entity.SysDictType;
import io.github.genkidoudou.system.internal.mapper.SysDictDataMapper;
import io.github.genkidoudou.system.internal.mapper.SysDictTypeMapper;
import io.github.genkidoudou.system.internal.service.ISysDictTypeService;
import io.github.genkidoudou.system.internal.vo.SysDictTypeImportRow;
import io.github.genkidoudou.system.internal.vo.SysDictTypeVo;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 字典类型实现：删除前校验是否仍有关联字典项；refresh 方法通过注解驱逐缓存。
 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "sys-dict#3600")
public class SysDictTypeServiceImpl extends CrudServiceImpl<SysDictTypeMapper, SysDictType, SysDictTypeVo>
  implements ISysDictTypeService {

  public static final int IMPORT_MAX_ROWS = 5000;

  private final SysDictDataMapper dictDataMapper;

  @Override
  protected Class<SysDictTypeVo> voClass() {
    return SysDictTypeVo.class;
  }

  @Override
  public void applyQuery(LambdaQueryWrapper<SysDictType> q, SysDictTypeVo param) {
    if (param == null) {
      return;
    }
    if (StrUtil.isNotBlank(param.getDictName())) {
      q.like(SysDictType::getDictName, param.getDictName().trim());
    }
    if (StrUtil.isNotBlank(param.getDictType())) {
      q.like(SysDictType::getDictType, param.getDictType().trim());
    }
    if (StrUtil.isNotBlank(param.getStatus())) {
      q.eq(SysDictType::getStatus, param.getStatus().trim());
    }
  }

  @Override
  public PageInfo<SysDictTypeVo> page(PageRequest<SysDictTypeVo> pageRequest) {
    return crudPage(pageRequest);
  }

  @Override
  public SysDictTypeVo getDetail(Long dictId) {
    return crudGetDetail(dictId, "字典类型不存在");
  }

  @CacheEvict(allEntries = true)
  @Override
  public Long add(SysDictTypeVo vo) {
    String type = vo.getDictType().trim();
    if (this.count(new LambdaQueryWrapper<SysDictType>().eq(SysDictType::getDictType, type)) > 0) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "字典类型已存在");
    }
    SysDictType entity = toEntity(vo);
    entity.setDictId(null);
    entity.setDictName(vo.getDictName().trim());
    entity.setDictType(type);
    entity.setStatus(StrUtil.blankToDefault(vo.getStatus(), "0"));
    this.save(entity);
    return entity.getDictId();
  }

  @CacheEvict(allEntries = true)
  @Override
  public boolean update(SysDictTypeVo vo) {
    SysDictType existing = this.getById(vo.getDictId());
    if (existing == null) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "字典类型不存在");
    }
    String type = vo.getDictType().trim();
    if (this.count(new LambdaQueryWrapper<SysDictType>()
      .eq(SysDictType::getDictType, type)
      .ne(SysDictType::getDictId, vo.getDictId())) > 0) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "字典类型已存在");
    }
    SysDictType entity = toEntity(vo);
    entity.setDictId(existing.getDictId());
    entity.setDictName(vo.getDictName().trim());
    entity.setDictType(type);
    entity.setStatus(StrUtil.blankToDefault(vo.getStatus(), existing.getStatus()));
    return this.updateById(entity);
  }

  /**
   * 删除字典类型前先统计其下字典项数量，非空则拒绝。
   */
  @CacheEvict(allEntries = true)
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void remove(Collection<Long> ids) {
    if (CollectionUtil.isEmpty(ids)) {
      return;
    }
    for (Long id : ids) {
      if (id == null) {
        continue;
      }
      SysDictType row = this.getById(id);
      if (row == null) {
        continue;
      }
      long dataCount = dictDataMapper.selectCount(new LambdaQueryWrapper<SysDictData>()
        .eq(SysDictData::getDictType, row.getDictType()));
      if (dataCount > 0) {
        throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "存在字典项，无法删除");
      }
      this.removeById(id);
    }
  }

  /**
   * 驱逐全部字典缓存；方法体为空，失效由 {@link CacheEvict} 完成。
   */
  @CacheEvict(allEntries = true)
  @Override
  public void refreshAll() {
    // cache eviction via annotation
  }

  /**
   * 驱逐全部字典缓存（含指定 dictType 对应的 listByType 条目）。
   */
  @CacheEvict(allEntries = true)
  @Override
  public void refresh(String dictType) {
    // cache eviction via annotation
  }

  @Override
  public List<SysDictTypeVo> export(SysDictTypeVo query) {
    List<SysDictType> list = listForExport(query == null ? new SysDictTypeVo() : query);
    if (list.size() > IMPORT_MAX_ROWS) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "导出条数超过上限 " + IMPORT_MAX_ROWS);
    }
    return crudToVoList(list);
  }

  @CacheEvict(allEntries = true)
  @Transactional(rollbackFor = Exception.class)
  @Override
  public ExcelResult<SysDictTypeImportRow> importExcel(MultipartFile file, boolean updateSupport) throws IOException {
    if (file == null || file.isEmpty()) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "请上传 Excel 文件");
    }
    List<SysDictType> batch = new ArrayList<>();
    ExcelResult<SysDictTypeImportRow> result = ExcelUtils.importExcel(
      file.getInputStream(),
      SysDictTypeImportRow.class,
      (row, ctx) -> {
        if (row == null || StrUtil.isBlank(row.getDictType()) || StrUtil.isBlank(row.getDictName())) {
          throw new ExcelDataCheckException("字典名称与类型不能为空");
        }
        String type = row.getDictType().trim();
        SysDictType existing = this.getOne(new LambdaQueryWrapper<SysDictType>()
          .eq(SysDictType::getDictType, type).last("LIMIT 1"));
        SysDictType entity = new SysDictType();
        entity.setDictName(row.getDictName().trim());
        entity.setDictType(type);
        entity.setStatus(StrUtil.blankToDefault(row.getStatus(), "0"));
        entity.setRemark(row.getRemark());
        if (existing == null) {
          batch.add(entity);
        } else if (updateSupport) {
          entity.setDictId(existing.getDictId());
          batch.add(entity);
        } else {
          throw new ExcelDataCheckException("字典类型已存在");
        }
      },
      (list, ctx) -> {
      });
    if (result.getTotal() != null && result.getTotal() > IMPORT_MAX_ROWS) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "导入条数超过上限 " + IMPORT_MAX_ROWS);
    }
    if (CollectionUtil.isNotEmpty(batch)) {
      this.saveOrUpdateBatch(batch);
    }
    result.writeErrorFile();
    return result;
  }

  private List<SysDictType> listForExport(SysDictTypeVo query) {
    return crudListForQuery(query, SysDictTypeVo::getIds);
  }
}
