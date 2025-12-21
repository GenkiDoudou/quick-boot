package com.su60.quickboot.web.service;

import cn.hutool.core.collection.CollectionUtil;
import com.su60.quickboot.data.excel.convert.DictService;
import com.su60.quickboot.system.dos.SysDictDataDo;
import com.su60.quickboot.system.service.ISysDictDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DictServiceImpl implements DictService {

	@Autowired
	private ISysDictDataService sysDictDataService;

	@Override
	public String getDictLabel(String dictType, String dictValue) {
		List<SysDictDataDo> sysDictDataDos = sysDictDataService.listByDictType(dictType);
		if (CollectionUtil.isEmpty(sysDictDataDos)) {
			return null;
		}
		return sysDictDataDos
				.stream()
				.filter(a -> a.getDictValue().equals(dictValue))
				.map(SysDictDataDo::getDictLabel)
				.findFirst()
				.orElse(null);
	}

	@Override
	public String getDictValue(String dictType, String dictLabel) {
		List<SysDictDataDo> sysDictDataDos = sysDictDataService.listByDictType(dictType);
		if (CollectionUtil.isEmpty(sysDictDataDos)) {
			return null;
		}
		return sysDictDataDos
				.stream()
				.filter(a -> a.getDictLabel().equals(dictLabel))
				.map(SysDictDataDo::getDictValue)
				.findFirst()
				.orElse(null);
	}
}
