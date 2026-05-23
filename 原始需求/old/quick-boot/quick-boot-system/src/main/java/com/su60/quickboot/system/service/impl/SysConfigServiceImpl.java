package com.su60.quickboot.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.su60.quickboot.common.core.PageInfo;
import com.su60.quickboot.common.exception.WarningException;
import com.su60.quickboot.system.entity.SysConfigEntity;
import com.su60.quickboot.system.dos.SysConfigDo;
import com.su60.quickboot.system.mapper.SysConfigMapper;
import com.su60.quickboot.system.service.ISysConfigService;
import com.su60.quickboot.data.mybatisplus.BaseVoServiceImpl;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import com.su60.quickboot.data.mybatisplus.PageVoHandler;

import java.util.List;

/**
 * <p>
 * 参数配置表 服务实现类
 * </p>
 *
 * @author luyanan
 * @since 2026/01/11
 */
@RequiredArgsConstructor
@Service
@CacheConfig(cacheNames = "sysConfig")
public class SysConfigServiceImpl extends BaseVoServiceImpl<SysConfigMapper, SysConfigEntity, SysConfigDo> implements ISysConfigService {


	@Override
	public PageInfo<SysConfigDo> page(SysConfigDo sysConfigDo) {
		return super.page(sysConfigDo, new PageVoHandler<SysConfigEntity, SysConfigDo>() {
			@Override
			public void queryWrapperHandler(SysConfigDo vo, SysConfigEntity sysConfigEntity, LambdaQueryWrapper<SysConfigEntity> queryWrapper) {
				queryWrapper.orderByDesc(SysConfigEntity::getCreateTime);
			}
		});
	}

	@Override
	public Boolean save(SysConfigDo sysConfigDo) {
		if (this.count(new LambdaQueryWrapper<SysConfigEntity>()
				.eq(SysConfigEntity::getConfigKey, sysConfigDo.getConfigKey())) > 0) {
			throw new WarningException(300003);
		}
		return super.saveVo(sysConfigDo);
	}

	@CacheEvict(key = "#sysConfigDo.configId")
	@Override
	public Boolean updateById(SysConfigDo sysConfigDo) {
		return super.updateVoById(sysConfigDo);
	}

	@Override
	public SysConfigDo getVoById(Long id) {
		return super.getVoById(id);
	}

	@CacheEvict(allEntries = true)
	@Override
	public Boolean deleteByIds(List<Long> ids) {
		return super.deleteByIds(ids);
	}

	@Cacheable(key = "#configKey")
	@Override
	public String getConfigValue(String configKey) {
		List<SysConfigEntity> list = this.list(new LambdaQueryWrapper<SysConfigEntity>()
				.eq(SysConfigEntity::getConfigKey, configKey));
		if (list != null && !list.isEmpty()) {
			return list.get(0).getConfigValue();
		}

		return null;
	}
}

