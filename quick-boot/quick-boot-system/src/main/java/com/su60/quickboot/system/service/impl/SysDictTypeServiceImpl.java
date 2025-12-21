package com.su60.quickboot.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.su60.quickboot.common.exception.WarningException;
import com.su60.quickboot.data.excel.ExcelUtils2;
import com.su60.quickboot.system.dos.SysDictDataDo;
import com.su60.quickboot.system.entity.SysDictTypeEntity;
import com.su60.quickboot.system.dos.SysDictTypeDo;
import com.su60.quickboot.system.excel.SysDictExcelVo;
import com.su60.quickboot.system.mapper.SysDictTypeMapper;
import com.su60.quickboot.system.service.ISysDictDataService;
import com.su60.quickboot.system.service.ISysDictTypeService;
import com.su60.quickboot.data.mybatisplus.BaseServiceImpl2;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * <p>
 * 字典类型表 服务实现类
 * </p>
 *
 * @author luyanan
 * @since 2024/08/07
 */
@RequiredArgsConstructor
@Service

public class SysDictTypeServiceImpl extends BaseServiceImpl2<SysDictTypeMapper, SysDictTypeEntity, SysDictTypeDo> implements ISysDictTypeService {

	private final ISysDictDataService sysDictDataService;

	@Transactional(rollbackFor = Exception.class)
	@Override
	public Boolean deleteByIds(Collection<? extends Serializable> ids) {
		for (Serializable id : ids) {
			SysDictTypeEntity sysDictType = this.getById(id);
			if (null == sysDictType) {
				throw new WarningException(100010);
			}
			// 检查是否有未删除的字典项
			boolean has = sysDictDataService.hasData(sysDictType.getDictType());
			if (has) {
				throw new WarningException(100009, sysDictType.getDictType());
			}
		}
		return super.deleteByIds(ids);
	}


	@Override
	public boolean removeByIds(Collection<?> list) {

		return super.removeByIds(list);
	}

	@Override
	public List<SysDictTypeEntity> listAll() {
		return this.list();
	}

	@Override
	public void export(SysDictTypeDo dictTypeDo, HttpServletResponse response) throws Exception {
		LambdaQueryWrapper<SysDictTypeEntity> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(SysDictTypeEntity::getStatus, "0");
		queryWrapper.like(StrUtil.isNotBlank(dictTypeDo.getDictName()), SysDictTypeEntity::getDictName, dictTypeDo.getDictName());
		queryWrapper.like(StrUtil.isNotBlank(dictTypeDo.getDictType()), SysDictTypeEntity::getDictType, dictTypeDo.getDictType());
		queryWrapper.orderByDesc(SysDictTypeEntity::getCreateTime);
		List<SysDictExcelVo> sysDictExcelVos = new ArrayList<>();
		List<SysDictTypeEntity> list = this.list(queryWrapper);
		for (SysDictTypeEntity sysDictTypeEntity : list) {
			// 查询字典项
			List<SysDictDataDo> sysDictDataDos = this.sysDictDataService.listByDictType(sysDictTypeEntity.getDictType()).stream().sorted(Comparator.comparing(SysDictDataDo::getDictSort)).toList();
			if (CollectionUtil.isNotEmpty(sysDictDataDos)) {
				for (SysDictDataDo sysDictDataDo : sysDictDataDos) {
					SysDictExcelVo build = SysDictExcelVo.builder().dictName(sysDictTypeEntity.getDictName())
							.dictType(sysDictTypeEntity.getDictType())
							.dictValue(sysDictDataDo.getDictValue())
							.dictLabel(sysDictDataDo.getDictLabel())
							.dictSort(sysDictDataDo.getDictSort())
							.build();
					sysDictExcelVos.add(build);

				}
			}
		}
		ExcelUtils2.builder(response)
				.addSheet(ExcelUtils2.sheet(SysDictExcelVo.class)
						.list(sysDictExcelVos)
						.build())

				.export("字典表");
	}
}

