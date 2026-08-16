package io.github.genkidoudou.system.internal.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.PageRequest;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.common.file.FilePathSupport;
import io.github.genkidoudou.common.mybatisplus.BaseServiceImpl;
import io.github.genkidoudou.system.internal.entity.SysFile;
import io.github.genkidoudou.system.internal.entity.SysFileClassify;
import io.github.genkidoudou.system.internal.mapper.SysFileClassifyMapper;
import io.github.genkidoudou.system.internal.mapper.SysFileMapper;
import io.github.genkidoudou.system.internal.service.ISysFileClassifyService;
import io.github.genkidoudou.system.internal.vo.SysFileClassifyVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 文件分类管理实现（短缓存；变更时全量失效）。
 */
@Slf4j
@RequiredArgsConstructor
@Service
@CacheConfig(cacheNames = SysFileClassifyServiceImpl.CACHE_NAME)
public class SysFileClassifyServiceImpl extends BaseServiceImpl<SysFileClassifyMapper, SysFileClassify>
  implements ISysFileClassifyService {

  public static final String CACHE_NAME = "sys-fileClassify#3600";

  private static final long DEFAULT_LIMIT_SIZE = 10L * 1024 * 1024;

  private static final int DEFAULT_COMPRESS_MIN_KB = 200;

  private static final float DEFAULT_COMPRESS_QUALITY = 0.85f;

  private static final int DEFAULT_COMPRESS_MAX_EDGE = 1920;

  private final SysFileMapper sysFileMapper;

  @Override
  public PageInfo<SysFileClassifyVo> page(PageRequest<SysFileClassifyVo> pageRequest) {
    SysFileClassifyVo param = pageRequest != null ? pageRequest.getParam() : null;
    return this.page(pageRequest, q -> {
      if (param == null) {
        return;
      }
      if (StrUtil.isNotBlank(param.getClassify())) {
        q.like(SysFileClassify::getClassify, param.getClassify().trim());
      }
      if (StrUtil.isNotBlank(param.getClassifyName())) {
        q.like(SysFileClassify::getClassifyName, param.getClassifyName().trim());
      }
      if (StrUtil.isNotBlank(param.getStatus())) {
        q.eq(SysFileClassify::getStatus, param.getStatus().trim());
      }
      q.orderByDesc(SysFileClassify::getClassifyId);
    }, SysFileClassifyVo.class);
  }

  @Override
  public SysFileClassifyVo getDetail(Long classifyId) {
    if (classifyId == null) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "classifyId 不能为空");
    }
    SysFileClassify row = this.getById(classifyId);
    if (row == null) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "分类不存在");
    }
    return toVo(row, SysFileClassifyVo.class);
  }

  @Cacheable(key = "'by:' + #classify", unless = "#result == null")
  @Override
  public SysFileClassify getByClassifyKey(String classify) {
    if (StrUtil.isBlank(classify)) {
      return null;
    }
    return this.getOne(new LambdaQueryWrapper<SysFileClassify>()
      .eq(SysFileClassify::getClassify, classify.trim())
      .last("LIMIT 1"));
  }

  @Cacheable(key = "'enabled'")
  @Override
  public List<SysFileClassify> listEnabledEntities() {
    return this.list(new LambdaQueryWrapper<SysFileClassify>()
      .eq(SysFileClassify::getStatus, "0")
      .orderByAsc(SysFileClassify::getClassify));
  }

  @CacheEvict(allEntries = true)
  @Transactional(rollbackFor = Exception.class)
  @Override
  public Long add(SysFileClassifyVo vo) {
    String key = FilePathSupport.normalizeClassifyKey(vo.getClassify(), null);
    long exists = this.count(new LambdaQueryWrapper<SysFileClassify>()
      .eq(SysFileClassify::getClassify, key));
    if (exists > 0) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "分类键已存在: " + key);
    }
    SysFileClassify entity = toEntity(vo);
    entity.setClassifyId(null);
    entity.setClassify(key);
    entity.setClassifyName(vo.getClassifyName().trim());
    entity.setLimitExt(StrUtil.trim(vo.getLimitExt()));
    entity.setLimitSizeBytes(resolveLimitSize(vo.getLimitSizeBytes()));
    entity.setLimitCount(resolveLimitCount(vo.getLimitCount()));
    entity.setCompressEnabled(flag01(vo.getCompressEnabled(), "0"));
    entity.setCompressMinSizeKb(resolveCompressMinKb(vo.getCompressMinSizeKb()));
    entity.setCompressQuality(resolveCompressQuality(vo.getCompressQuality()));
    entity.setCompressMaxEdge(resolveCompressMaxEdge(vo.getCompressMaxEdge()));
    entity.setAnonymous(flag01(vo.getAnonymous(), "0"));
    entity.setStatus(flag01(vo.getStatus(), "0"));
    entity.setRemark(vo.getRemark());
    this.save(entity);
    return entity.getClassifyId();
  }

  @CacheEvict(allEntries = true)
  @Transactional(rollbackFor = Exception.class)
  @Override
  public boolean update(SysFileClassifyVo vo) {
    SysFileClassify existing = this.getById(vo.getClassifyId());
    if (existing == null) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "分类不存在");
    }
    if (StrUtil.isNotBlank(vo.getClassify())
      && !existing.getClassify().equals(vo.getClassify().trim())) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "分类键创建后不可修改");
    }
    SysFileClassify entity = toEntity(vo);
    entity.setClassifyId(existing.getClassifyId());
    entity.setClassify(existing.getClassify());
    entity.setClassifyName(vo.getClassifyName().trim());
    entity.setLimitExt(StrUtil.trim(vo.getLimitExt()));
    entity.setLimitSizeBytes(resolveLimitSize(vo.getLimitSizeBytes()));
    entity.setLimitCount(resolveLimitCount(vo.getLimitCount()));
    entity.setCompressEnabled(flag01(vo.getCompressEnabled(), existing.getCompressEnabled()));
    entity.setCompressMinSizeKb(resolveCompressMinKb(
      vo.getCompressMinSizeKb() != null ? vo.getCompressMinSizeKb() : existing.getCompressMinSizeKb()));
    entity.setCompressQuality(resolveCompressQuality(
      vo.getCompressQuality() != null ? vo.getCompressQuality() : existing.getCompressQuality()));
    entity.setCompressMaxEdge(resolveCompressMaxEdge(
      vo.getCompressMaxEdge() != null ? vo.getCompressMaxEdge() : existing.getCompressMaxEdge()));
    entity.setAnonymous(flag01(vo.getAnonymous(), existing.getAnonymous()));
    entity.setStatus(flag01(vo.getStatus(), existing.getStatus()));
    entity.setRemark(vo.getRemark());
    return this.updateById(entity);
  }

  @CacheEvict(allEntries = true)
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void remove(Collection<Long> ids) {
    if (CollectionUtil.isEmpty(ids)) {
      return;
    }
    List<Long> idList = ids.stream()
      .filter(Objects::nonNull)
      .distinct()
      .collect(Collectors.toList());
    if (idList.isEmpty()) {
      return;
    }
    List<SysFileClassify> rows = this.listByIds(idList);
    if (rows.size() != idList.size()) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "存在无效的分类 ID");
    }
    for (SysFileClassify row : rows) {
      long ref = sysFileMapper.selectCount(new LambdaQueryWrapper<SysFile>()
        .eq(SysFile::getClassify, row.getClassify()));
      if (ref > 0) {
        throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM,
          "分类仍有文件引用，无法删除: " + row.getClassify());
      }
    }
    this.removeByIds(idList);
  }

  private static long resolveLimitSize(Long bytes) {
    return bytes == null || bytes <= 0 ? DEFAULT_LIMIT_SIZE : bytes;
  }

  private static int resolveLimitCount(Integer count) {
    return count == null || count <= 0 ? 1 : count;
  }

  private static int resolveCompressMinKb(Integer kb) {
    return kb == null || kb <= 0 ? DEFAULT_COMPRESS_MIN_KB : kb;
  }

  private static float resolveCompressQuality(Float quality) {
    if (quality == null) {
      return DEFAULT_COMPRESS_QUALITY;
    }
    if (quality < 0.1f) {
      return 0.1f;
    }
    if (quality > 1.0f) {
      return 1.0f;
    }
    return quality;
  }

  private static int resolveCompressMaxEdge(Integer edge) {
    if (edge == null) {
      return DEFAULT_COMPRESS_MAX_EDGE;
    }
    return Math.max(0, edge);
  }

  private static String flag01(String value, String defaultVal) {
    if ("1".equals(value)) {
      return "1";
    }
    if ("0".equals(value)) {
      return "0";
    }
    return StrUtil.blankToDefault(defaultVal, "0");
  }
}
