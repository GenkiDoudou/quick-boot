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
import io.github.genkidoudou.common.mybatisplus.BaseServiceImpl;
import io.github.genkidoudou.system.internal.entity.SysDictData;
import io.github.genkidoudou.system.internal.mapper.SysDictDataMapper;
import io.github.genkidoudou.system.internal.service.ISysDictDataService;
import io.github.genkidoudou.system.internal.vo.SysDictDataImportRow;
import io.github.genkidoudou.system.internal.vo.SysDictDataVo;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
 * 字典数据实现：{@code listByType} 走 Spring Cache，增删改导入时全量失效。
 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "sys-dict#3600")
public class SysDictDataServiceImpl extends BaseServiceImpl<SysDictDataMapper, SysDictData>
  implements ISysDictDataService {

  public static final int IMPORT_MAX_ROWS = 5000;

  @Override
  public PageInfo<SysDictDataVo> page(PageRequest<SysDictDataVo> pageRequest) {
    SysDictDataVo param = pageRequest != null ? pageRequest.getParam() : null;
    return this.page(pageRequest, q -> {
      if (param == null) {
        return;
      }
      if (StrUtil.isNotBlank(param.getDictType())) {
        q.eq(SysDictData::getDictType, param.getDictType().trim());
      }
      if (StrUtil.isNotBlank(param.getDictLabel())) {
        q.like(SysDictData::getDictLabel, param.getDictLabel().trim());
      }
      if (StrUtil.isNotBlank(param.getStatus())) {
        q.eq(SysDictData::getStatus, param.getStatus().trim());
      }
      q.orderByAsc(SysDictData::getDictSort);
    }, SysDictDataVo.class);
  }

  @Override
  public SysDictDataVo getDetail(Long dictCode) {
    SysDictData row = this.getById(dictCode);
    if (row == null) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "字典项不存在");
    }
    return toVo(row, SysDictDataVo.class);
  }

  /**
   * 写入后清空字典缓存，保证前端下拉与标签即时一致。
   */
  @CacheEvict(allEntries = true)
  @Override
  public Long add(SysDictDataVo vo) {
    assertUnique(vo.getDictType().trim(), vo.getDictValue().trim(), null);
    SysDictData entity = toEntity(vo);
    entity.setDictCode(null);
    applyFields(entity, vo);
    this.save(entity);
    return entity.getDictCode();
  }

  @CacheEvict(allEntries = true)
  @Override
  public boolean update(SysDictDataVo vo) {
    SysDictData existing = this.getById(vo.getDictCode());
    if (existing == null) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "字典项不存在");
    }
    assertUnique(vo.getDictType().trim(), vo.getDictValue().trim(), vo.getDictCode());
    SysDictData entity = toEntity(vo);
    entity.setDictCode(existing.getDictCode());
    applyFields(entity, vo);
    return this.updateById(entity);
  }

  @CacheEvict(allEntries = true)
  @Override
  public void remove(Collection<Long> ids) {
    if (CollectionUtil.isEmpty(ids)) {
      return;
    }
    this.removeByIds(ids.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList()));
  }

  /** 按 dictType 缓存启用字典项，TTL 见 {@code sys-dict#3600}。 */
  @Cacheable(key = "'type:'+#dictType")
  @Override
  public List<SysDictDataVo> listByType(String dictType) {
    if (StrUtil.isBlank(dictType)) {
      return List.of();
    }
    return this.list(new LambdaQueryWrapper<SysDictData>()
        .eq(SysDictData::getDictType, dictType.trim())
        .eq(SysDictData::getStatus, "0")
        .orderByAsc(SysDictData::getDictSort))
      .stream().map(x -> toVo(x, SysDictDataVo.class)).collect(Collectors.toList());
  }

  @Override
  public List<SysDictDataVo> export(SysDictDataVo query) {
    List<SysDictData> list = listForExport(query == null ? new SysDictDataVo() : query);
    if (list.size() > IMPORT_MAX_ROWS) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "导出条数超过上限 " + IMPORT_MAX_ROWS);
    }
    return list.stream().map(x -> toVo(x, SysDictDataVo.class)).collect(Collectors.toList());
  }

  @CacheEvict(allEntries = true)
  @Transactional(rollbackFor = Exception.class)
  @Override
  public ExcelResult<SysDictDataImportRow> importExcel(MultipartFile file, boolean updateSupport) throws IOException {
    if (file == null || file.isEmpty()) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "请上传 Excel 文件");
    }
    List<SysDictData> batch = new ArrayList<>();
    ExcelResult<SysDictDataImportRow> result = ExcelUtils.importExcel(
      file.getInputStream(),
      SysDictDataImportRow.class,
      (row, ctx) -> {
        if (row == null || StrUtil.isBlank(row.getDictType()) || StrUtil.isBlank(row.getDictValue())
          || StrUtil.isBlank(row.getDictLabel())) {
          throw new ExcelDataCheckException("字典类型/标签/键值不能为空");
        }
        String type = row.getDictType().trim();
        String value = row.getDictValue().trim();
        SysDictData existing = this.getOne(new LambdaQueryWrapper<SysDictData>()
          .eq(SysDictData::getDictType, type)
          .eq(SysDictData::getDictValue, value)
          .last("LIMIT 1"));
        SysDictData entity = new SysDictData();
        entity.setDictType(type);
        entity.setDictValue(value);
        entity.setDictLabel(row.getDictLabel().trim());
        entity.setDictSort(row.getDictSort() == null ? 0 : row.getDictSort());
        entity.setCssClass(row.getCssClass());
        entity.setListClass(row.getListClass());
        entity.setIsDefault(normalizeIsDefault(row.getIsDefault()));
        entity.setStatus(StrUtil.blankToDefault(row.getStatus(), "0"));
        entity.setRemark(row.getRemark());
        if (existing == null) {
          batch.add(entity);
        } else if (updateSupport) {
          entity.setDictCode(existing.getDictCode());
          batch.add(entity);
        } else {
          throw new ExcelDataCheckException("字典键值已存在");
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

  private void applyFields(SysDictData entity, SysDictDataVo vo) {
    entity.setDictType(vo.getDictType().trim());
    entity.setDictLabel(vo.getDictLabel().trim());
    entity.setDictValue(vo.getDictValue().trim());
    entity.setDictSort(vo.getDictSort() == null ? 0 : vo.getDictSort());
    entity.setCssClass(vo.getCssClass());
    entity.setListClass(vo.getListClass());
    entity.setIsDefault(normalizeIsDefault(vo.getIsDefault()));
    entity.setStatus(StrUtil.blankToDefault(vo.getStatus(), "0"));
  }

  /**
   * 将是否默认归一为 {@code 0}/{@code 1}；兼容历史 {@code Y}/{@code N} 入参。
   *
   * @param raw 原始值
   * @return 0 或 1
   */
  private String normalizeIsDefault(String raw) {
    if (StrUtil.isBlank(raw)) {
      return "0";
    }
    String v = raw.trim();
    if ("1".equals(v) || "Y".equalsIgnoreCase(v) || "true".equalsIgnoreCase(v)) {
      return "1";
    }
    return "0";
  }

  private void assertUnique(String type, String value, Long excludeId) {
    LambdaQueryWrapper<SysDictData> q = new LambdaQueryWrapper<SysDictData>()
      .eq(SysDictData::getDictType, type)
      .eq(SysDictData::getDictValue, value);
    if (excludeId != null) {
      q.ne(SysDictData::getDictCode, excludeId);
    }
    if (this.count(q) > 0) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "字典键值已存在");
    }
  }

  private List<SysDictData> listForExport(SysDictDataVo query) {
    List<Long> ids = query.getIds() == null ? Collections.emptyList() : query.getIds().stream()
      .filter(Objects::nonNull).distinct().collect(Collectors.toList());
    if (!ids.isEmpty()) {
      return this.listByIds(ids);
    }
    LambdaQueryWrapper<SysDictData> q = new LambdaQueryWrapper<>();
    if (StrUtil.isNotBlank(query.getDictType())) {
      q.eq(SysDictData::getDictType, query.getDictType().trim());
    }
    if (StrUtil.isNotBlank(query.getDictLabel())) {
      q.like(SysDictData::getDictLabel, query.getDictLabel().trim());
    }
    if (StrUtil.isNotBlank(query.getStatus())) {
      q.eq(SysDictData::getStatus, query.getStatus().trim());
    }
    q.orderByAsc(SysDictData::getDictSort);
    return this.list(q);
  }
}
