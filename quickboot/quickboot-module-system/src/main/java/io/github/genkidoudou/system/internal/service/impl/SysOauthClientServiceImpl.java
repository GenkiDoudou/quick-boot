package io.github.genkidoudou.system.internal.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.IdUtil;
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
import io.github.genkidoudou.system.internal.entity.SysOauthClient;
import io.github.genkidoudou.system.internal.mapper.SysOauthClientMapper;
import io.github.genkidoudou.system.internal.service.ISysOauthClientService;
import io.github.genkidoudou.system.internal.vo.SysOauthClientImportErrorRow;
import io.github.genkidoudou.system.internal.vo.SysOauthClientImportResult;
import io.github.genkidoudou.system.internal.vo.SysOauthClientImportRow;
import io.github.genkidoudou.system.internal.vo.SysOauthClientVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * OAuth 客户端管理实现。
 */
@Slf4j
@RequiredArgsConstructor
@Service
@CacheConfig(cacheNames = SysOauthClientServiceImpl.CACHE_NAME)
public class SysOauthClientServiceImpl extends BaseServiceImpl<SysOauthClientMapper, SysOauthClient>
  implements ISysOauthClientService {

  public static final String CACHE_NAME = "sys-oauthClient#3600";

  public static final int IMPORT_MAX_ROWS = 5000;

  @Cacheable(key = "#clientId")
  @Override
  public SysOauthClient findByClientId(String clientId) {
    return this.getOne(new LambdaQueryWrapper<SysOauthClient>()
      .eq(SysOauthClient::getClientId, clientId));
  }

  @Override
  public PageInfo<SysOauthClientVo> page(PageRequest<SysOauthClientVo> pageRequest) {
    SysOauthClientVo param = pageRequest != null ? pageRequest.getParam() : null;
    return this.page(pageRequest, q -> {
      if (param == null) {
        return;
      }
      if (StrUtil.isNotBlank(param.getClientId())) {
        q.like(SysOauthClient::getClientId, param.getClientId().trim());
      }
      if (StrUtil.isNotBlank(param.getClientName())) {
        q.like(SysOauthClient::getClientName, param.getClientName().trim());
      }
      if (StrUtil.isNotBlank(param.getStatus())) {
        q.eq(SysOauthClient::getStatus, param.getStatus().trim());
      }
    }, SysOauthClientVo.class, (entities, vos) -> {
      for (SysOauthClientVo vo : vos) {
        vo.setClientSecret(null);
      }
      return vos;
    });
  }

  @Override
  public SysOauthClientVo getDetail(Long id) {
    if (id == null) {
      throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "id");
    }
    return getVoById(id, SysOauthClientVo.class);
  }

  @CacheEvict(key = "#vo.clientId")
  @Override
  public Long add(SysOauthClientVo vo) {
    String clientId = vo.getClientId().trim();
    long exists = this.count(new LambdaQueryWrapper<SysOauthClient>()
      .eq(SysOauthClient::getClientId, clientId));
    if (exists > 0) {
      throw new WarningException(ErrorCodes.Common.INVALID_PARAM, clientId);
    }
    SysOauthClient oauthClient = toEntity(vo);
    oauthClient.setId(null);
    oauthClient.setClientId(clientId);
    oauthClient.setClientSecret(IdUtil.fastSimpleUUID().toUpperCase());
    boolean save = this.save(oauthClient);
    if (!save) {
      return null;
    }
    return oauthClient.getId();
  }

  @CacheEvict(key = "#vo.clientId")
  @Override
  public boolean update(SysOauthClientVo vo) {
    SysOauthClient existing = this.getById(vo.getId());
    if (existing == null) {
      throw new WarningException(10002, vo.getId());
    }
    SysOauthClient oauthClient = toEntity(vo);
    // 主键与业务键、密钥不可被表单覆盖
    oauthClient.setId(existing.getId());
    oauthClient.setClientId(existing.getClientId());
    oauthClient.setClientSecret(existing.getClientSecret());
    return super.updateById(oauthClient);
  }

  @CacheEvict(allEntries = true)
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
    this.removeByIds(idList);
  }

  @Override
  public List<SysOauthClientVo> export(SysOauthClientVo sysOauthClientVo) {
    List<SysOauthClient> entities = listForExport(sysOauthClientVo);

    return entities.stream()
      .map((a -> toVo(a, SysOauthClientVo.class)))
      .collect(Collectors.toList());
  }

  private List<SysOauthClient> listForExport(SysOauthClientVo sysOauthClientVo) {

    List<Long> ids = sysOauthClientVo.getIds() == null ? Collections.emptyList() : sysOauthClientVo.getIds().stream()
      .filter(Objects::nonNull)
      .distinct()
      .collect(Collectors.toList());
    if (!ids.isEmpty()) {
      return this.listByIds(ids);
    }
    LambdaQueryWrapper<SysOauthClient> q = new LambdaQueryWrapper<>();
    if (StrUtil.isNotBlank(sysOauthClientVo.getClientId())) {
      q.like(SysOauthClient::getClientId, sysOauthClientVo.getClientId().trim());
    }
    if (StrUtil.isNotBlank(sysOauthClientVo.getClientName())) {
      q.like(SysOauthClient::getClientName, sysOauthClientVo.getClientName().trim());
    }
    q.orderByDesc(SysOauthClient::getId);
    return this.list(q);
  }

  @CacheEvict(allEntries = true)
  @Override
  public ExcelResult<SysOauthClientImportRow> importExcel(MultipartFile file, boolean updateSupport) throws IOException {
    if (file == null || file.isEmpty()) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "请上传 Excel 文件");
    }
    List<SysOauthClient> lists = new ArrayList<>();

    ExcelResult<SysOauthClientImportRow> excelResult = ExcelUtils.importExcel(
      file.getInputStream(),
      SysOauthClientImportRow.class,
      (row, ctx) -> {
        SysOauthClient existing = findByClientId(row.getClientId());
        SysOauthClient client = BeanUtil.copyProperties(row, SysOauthClient.class);
        if (existing == null) {
          lists.add(client);
        } else if (updateSupport) {
          client.setId(existing.getId());
          client.setClientSecret(existing.getClientSecret());
          lists.add(client);
        } else {
          throw new ExcelDataCheckException("已存在相同客户端id");
        }
      },
      (batch, ctx) -> {
        // 行级已在 callback 入队
      });

    if (CollectionUtil.isNotEmpty(lists)) {
      for (SysOauthClient save : lists) {
        if (save.getId() == null) {
          save.setClientSecret(IdUtil.fastSimpleUUID().toUpperCase());
        }
      }
      this.saveOrUpdateBatch(lists);
    }
    excelResult.writeErrorFile();

    return excelResult;
  }


}
