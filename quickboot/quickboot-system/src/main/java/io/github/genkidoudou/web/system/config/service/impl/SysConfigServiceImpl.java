package io.github.genkidoudou.web.system.config.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.genkidoudou.common.excel.ExcelUtils;
import io.github.genkidoudou.web.system.config.domain.SysConfig;
import io.github.genkidoudou.web.system.config.dto.SysConfigBo;
import io.github.genkidoudou.web.system.config.dto.SysConfigExcelRow;
import io.github.genkidoudou.web.system.config.dto.SysConfigQueryBo;
import io.github.genkidoudou.web.system.config.mapper.SysConfigMapper;
import io.github.genkidoudou.web.system.config.service.SysConfigService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 系统参数服务实现。
 */
@Service
public class SysConfigServiceImpl implements SysConfigService {
    private static final String CACHE_NAME = "sys-config#3600";
    private static final String CONFIG_TYPE_SYSTEM = "1";

    private final SysConfigMapper mapper;

    public SysConfigServiceImpl(SysConfigMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<SysConfig> list(SysConfigQueryBo query) {
        return mapper.selectList(buildListWrapper(query).orderByDesc(SysConfig::getCreateTime));
    }

    @Override
    public long countExportRows(SysConfigQueryBo query) {
        Long c = mapper.selectCount(buildListWrapper(query));
        return c == null ? 0L : c;
    }

    @Override
    public byte[] exportExcelBytes(SysConfigQueryBo query, int maxRows) {
        int limit = Math.max(1, maxRows);
        List<SysConfig> rows = mapper.selectList(buildListWrapper(query).orderByDesc(SysConfig::getCreateTime)
            .last("LIMIT " + limit));
        List<SysConfigExcelRow> exportRows = new ArrayList<>(rows.size());
        for (SysConfig row : rows) {
            SysConfigExcelRow excelRow = BeanUtil.copyProperties(row, SysConfigExcelRow.class);
            excelRow.setConfigTypeName("1".equals(row.getConfigType()) ? "是" : "否");
            exportRows.add(excelRow);
        }
        return ExcelUtils.writeBytes("sys-config", SysConfigExcelRow.class, exportRows);
    }

    private LambdaQueryWrapper<SysConfig> buildListWrapper(SysConfigQueryBo query) {
        LocalDateTime beginTime = parseBeginTime(query.getBeginTime());
        LocalDateTime endTime = parseEndTime(query.getEndTime());
        return Wrappers.<SysConfig>lambdaQuery()
            .like(StrUtil.isNotBlank(query.getConfigName()), SysConfig::getConfigName, query.getConfigName())
            .like(StrUtil.isNotBlank(query.getConfigKey()), SysConfig::getConfigKey, query.getConfigKey())
            .eq(StrUtil.isNotBlank(query.getConfigType()), SysConfig::getConfigType, query.getConfigType())
            .ge(beginTime != null, SysConfig::getCreateTime, beginTime)
            .le(endTime != null, SysConfig::getCreateTime, endTime);
    }

    @Override
    public SysConfig getById(Long configId) {
        return mapper.selectById(configId);
    }

    @Override
    @Cacheable(cacheNames = CACHE_NAME, key = "#configKey")
    public String getConfigValueByKey(String configKey) {
        SysConfig row = mapper.selectOne(Wrappers.<SysConfig>lambdaQuery()
            .eq(SysConfig::getConfigKey, configKey), false);
        return row == null ? null : row.getConfigValue();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = CACHE_NAME, allEntries = true)
    public void add(SysConfigBo req) {
        checkConfigKeyUnique(req.getConfigKey(), null);
        SysConfig entity = BeanUtil.copyProperties(req, SysConfig.class);
        if (StrUtil.isBlank(entity.getConfigType())) {
            entity.setConfigType("0");
        }
        mapper.insert(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = CACHE_NAME, allEntries = true)
    public void update(SysConfigBo req) {
        SysConfig old = mapper.selectById(req.getConfigId());
        if (old == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "参数不存在或已删除");
        }

        if (CONFIG_TYPE_SYSTEM.equals(old.getConfigType())) {
            if (!StrUtil.equals(old.getConfigName(), req.getConfigName())
                || !StrUtil.equals(old.getConfigKey(), req.getConfigKey())
                || !StrUtil.equals(old.getConfigType(), req.getConfigType())) {
                throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "系统内置参数仅允许修改参数键值");
            }
        }

        checkConfigKeyUnique(req.getConfigKey(), req.getConfigId());
        SysConfig entity = BeanUtil.copyProperties(req, SysConfig.class);
        mapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = CACHE_NAME, allEntries = true)
    public void removeBatch(List<Long> configIds) {
        if (configIds == null || configIds.isEmpty()) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "删除参数ID不能为空");
        }
        List<SysConfig> rows = mapper.selectBatchIds(configIds);
        if (rows.size() != configIds.size()) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "存在无效的参数ID");
        }
        for (SysConfig row : rows) {
            if (CONFIG_TYPE_SYSTEM.equals(row.getConfigType())) {
                throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "系统内置参数不允许删除");
            }
        }
        mapper.deleteByIds(configIds);
    }

    @Override
    @CacheEvict(cacheNames = CACHE_NAME, allEntries = true)
    public void refreshCache() {
        // 参数缓存由 @Cacheable 管理，全量刷新通过清空缓存实现。
    }

    private void checkConfigKeyUnique(String configKey, Long excludeId) {
        var query = Wrappers.<SysConfig>lambdaQuery().eq(SysConfig::getConfigKey, configKey);
        if (excludeId != null) {
            query.ne(SysConfig::getConfigId, excludeId);
        }
        if (mapper.selectCount(query) > 0) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "参数键名已存在");
        }
    }

    private LocalDateTime parseBeginTime(String beginTime) {
        if (StrUtil.isBlank(beginTime)) {
            return null;
        }
        return LocalDateTime.parse(beginTime.trim() + "T00:00:00");
    }

    private LocalDateTime parseEndTime(String endTime) {
        if (StrUtil.isBlank(endTime)) {
            return null;
        }
        return LocalDateTime.of(java.time.LocalDate.parse(endTime.trim()), LocalTime.MAX);
    }
}
