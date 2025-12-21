package com.su60.quickboot.system.service.impl;

import com.su60.quickboot.common.bean.BeanConvertUtils;
import com.su60.quickboot.data.cache.CacheUtils;
import com.su60.quickboot.system.entity.SysDictDataEntity;
import com.su60.quickboot.system.dos.SysDictDataDo;
import com.su60.quickboot.system.mapper.SysDictDataMapper;
import com.su60.quickboot.system.service.ISysDictDataService;
import com.su60.quickboot.data.mybatisplus.BaseServiceImpl2;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * <p>
 * 字典数据表 服务实现类
 * </p>
 *
 * @author luyanan
 * @since 2024/08/07
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class SysDictDataServiceImpl extends BaseServiceImpl2<SysDictDataMapper, SysDictDataEntity, SysDictDataDo> implements ISysDictDataService {


	public static final String CACHE_NAME = "dict_data:";

	@PostConstruct
	public void init() {
		List<SysDictDataEntity> list = this.list(new LambdaQueryWrapper<SysDictDataEntity>());
		for (SysDictDataEntity sysDictDataEntity : list) {
			CacheUtils.setCache(CACHE_NAME, sysDictDataEntity.getDictValue(), sysDictDataEntity);
		}
		log.info("初始化字典成功,共初始化{}条", list.size());
	}


	@Cacheable(cacheNames = CACHE_NAME, key = "#dictType")
	@Override
	public List<SysDictDataDo> listByDictType(String dictType) {

		List<SysDictDataEntity> list = super.list(new LambdaQueryWrapper<SysDictDataEntity>()
				.eq(SysDictDataEntity::getStatus, "0")
				.eq(SysDictDataEntity::getDictType, dictType).orderByAsc(SysDictDataEntity::getDictSort));
		return BeanConvertUtils.convertListTo(list, SysDictDataDo::new);
	}

	@Override
	public boolean hasData(String dictType) {
		return this.count(new LambdaQueryWrapper<SysDictDataEntity>()
				.eq(SysDictDataEntity::getDictType, dictType)) > 0;
	}

	@Override
	public void clear() {
		CacheUtils.clearCache(CACHE_NAME);
	}
}

