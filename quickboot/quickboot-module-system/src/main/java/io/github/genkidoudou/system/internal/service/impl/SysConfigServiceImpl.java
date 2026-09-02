package io.github.genkidoudou.system.internal.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.PageRequest;
import io.github.genkidoudou.common.config.ConfigValueLookup;
import io.github.genkidoudou.common.excel.ExcelUtils;
import io.github.genkidoudou.common.excel.exception.ExcelDataCheckException;
import io.github.genkidoudou.common.excel.listener.ExcelResult;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.common.mybatisplus.CrudServiceImpl;
import io.github.genkidoudou.system.internal.entity.SysConfig;
import io.github.genkidoudou.system.internal.mapper.SysConfigMapper;
import io.github.genkidoudou.system.internal.service.ISysConfigService;
import io.github.genkidoudou.system.internal.vo.SysConfigImportRow;
import io.github.genkidoudou.system.internal.vo.SysConfigVo;
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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 系统参数实现：{@code getConfigValueByKey} 带缓存；内置参数（configType=1）键名与类型不可改、不可删。
 */
@Service
@CacheConfig(cacheNames = "sys-config#3600")
public class SysConfigServiceImpl extends CrudServiceImpl<SysConfigMapper, SysConfig, SysConfigVo>
  implements ISysConfigService, ConfigValueLookup {

  public static final int IMPORT_MAX_ROWS = 5000;

  private static final Pattern CONFIG_KEY_PATTERN =
    Pattern.compile("^[a-z0-9]+(-[a-z0-9]+)*(\\.[a-z0-9]+(-[a-z0-9]+)*)*$");

  @Override
  protected Class<SysConfigVo> voClass() {
    return SysConfigVo.class;
  }

  @Override
  public void applyQuery(LambdaQueryWrapper<SysConfig> q, SysConfigVo param) {
    if (param == null) {
      return;
    }
    if (StrUtil.isNotBlank(param.getConfigName())) {
      q.like(SysConfig::getConfigName, param.getConfigName().trim());
    }
    if (StrUtil.isNotBlank(param.getConfigKey())) {
      q.like(SysConfig::getConfigKey, param.getConfigKey().trim());
    }
    if (StrUtil.isNotBlank(param.getConfigType())) {
      q.eq(SysConfig::getConfigType, param.getConfigType().trim());
    }
  }

  @Override
  public PageInfo<SysConfigVo> page(PageRequest<SysConfigVo> pageRequest) {
    return crudPage(pageRequest);
  }

  @Override
  public SysConfigVo getDetail(Long configId) {
    return crudGetDetail(configId, "参数不存在");
  }

  @CacheEvict(allEntries = true)
  @Override
  public Long add(SysConfigVo vo) {
    String key = vo.getConfigKey().trim();
    validateKey(key);
    if (this.count(new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, key)) > 0) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "参数键名已存在");
    }
    SysConfig entity = toEntity(vo);
    entity.setConfigId(null);
    entity.setConfigName(vo.getConfigName().trim());
    entity.setConfigKey(key);
    entity.setConfigValue(vo.getConfigValue());
    entity.setConfigType(StrUtil.blankToDefault(vo.getConfigType(), "0"));
    entity.setRemark(vo.getRemark());
    this.save(entity);
    return entity.getConfigId();
  }

  /**
   * 系统内置参数（configType=1）仅允许改名称、键值与备注，键名与类型锁定。
   */
  @CacheEvict(allEntries = true)
  @Override
  public boolean update(SysConfigVo vo) {
    SysConfig existing = this.getById(vo.getConfigId());
    if (existing == null) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "参数不存在");
    }
    boolean builtin = "1".equals(existing.getConfigType());
    SysConfig entity = toEntity(vo);
    entity.setConfigId(existing.getConfigId());
    entity.setConfigName(vo.getConfigName().trim());
    entity.setConfigValue(vo.getConfigValue());
    entity.setRemark(vo.getRemark());
    if (builtin) {
      entity.setConfigKey(existing.getConfigKey());
      entity.setConfigType(existing.getConfigType());
    } else {
      String key = vo.getConfigKey().trim();
      validateKey(key);
      if (this.count(new LambdaQueryWrapper<SysConfig>()
        .eq(SysConfig::getConfigKey, key)
        .ne(SysConfig::getConfigId, vo.getConfigId())) > 0) {
        throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "参数键名已存在");
      }
      entity.setConfigKey(key);
      entity.setConfigType(StrUtil.blankToDefault(vo.getConfigType(), "0"));
    }
    return this.updateById(entity);
  }

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
      SysConfig row = this.getById(id);
      if (row == null) {
        continue;
      }
      if ("1".equals(row.getConfigType())) {
        throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "系统内置参数不允许删除");
      }
      this.removeById(id);
    }
  }

  /** 按 configKey 缓存参数值，TTL 见 {@code sys-config#3600}。 */
  @Cacheable(key = "'key:'+#configKey")
  @Override
  public String getConfigValueByKey(String configKey) {
    if (StrUtil.isBlank(configKey)) {
      return null;
    }
    SysConfig row = this.getOne(new LambdaQueryWrapper<SysConfig>()
      .eq(SysConfig::getConfigKey, configKey.trim()).last("LIMIT 1"));
    return row == null ? null : row.getConfigValue();
  }

  /**
   * 驱逐全部参数缓存；方法体为空，失效由 {@link CacheEvict} 完成。
   */
  @CacheEvict(allEntries = true)
  @Override
  public void refreshCache() {
    // eviction via annotation
  }

  @Override
  public List<SysConfigVo> export(SysConfigVo query) {
    List<SysConfig> list = listForExport(query == null ? new SysConfigVo() : query);
    if (list.size() > IMPORT_MAX_ROWS) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "导出条数超过上限 " + IMPORT_MAX_ROWS);
    }
    return crudToVoList(list);
  }

  @CacheEvict(allEntries = true)
  @Transactional(rollbackFor = Exception.class)
  @Override
  public ExcelResult<SysConfigImportRow> importExcel(MultipartFile file, boolean updateSupport) throws IOException {
    if (file == null || file.isEmpty()) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "请上传 Excel 文件");
    }
    List<SysConfig> batch = new ArrayList<>();
    ExcelResult<SysConfigImportRow> result = ExcelUtils.importExcel(
      file.getInputStream(),
      SysConfigImportRow.class,
      (row, ctx) -> {
        if (row == null || StrUtil.isBlank(row.getConfigKey()) || StrUtil.isBlank(row.getConfigName())
          || StrUtil.isBlank(row.getConfigValue())) {
          throw new ExcelDataCheckException("参数名称、键名、键值不能为空");
        }
        String key = row.getConfigKey().trim();
        if (!CONFIG_KEY_PATTERN.matcher(key).matches()) {
          throw new ExcelDataCheckException("参数键名仅支持小写字母、数字、点号与连字符");
        }
        SysConfig existing = this.getOne(new LambdaQueryWrapper<SysConfig>()
          .eq(SysConfig::getConfigKey, key).last("LIMIT 1"));
        SysConfig entity = new SysConfig();
        entity.setConfigName(row.getConfigName().trim());
        entity.setConfigKey(key);
        entity.setConfigValue(row.getConfigValue());
        entity.setConfigType(StrUtil.blankToDefault(row.getConfigType(), "0"));
        entity.setRemark(row.getRemark());
        if (existing == null) {
          batch.add(entity);
        } else if (updateSupport) {
          if ("1".equals(existing.getConfigType())) {
            entity.setConfigType(existing.getConfigType());
            entity.setConfigKey(existing.getConfigKey());
          }
          entity.setConfigId(existing.getConfigId());
          batch.add(entity);
        } else {
          throw new ExcelDataCheckException("参数键名已存在");
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

  private void validateKey(String key) {
    if (!CONFIG_KEY_PATTERN.matcher(key).matches()) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM,
        "参数键名仅支持小写字母、数字、点号与连字符");
    }
  }

  private List<SysConfig> listForExport(SysConfigVo query) {
    return crudListForQuery(query, SysConfigVo::getIds);
  }
}
