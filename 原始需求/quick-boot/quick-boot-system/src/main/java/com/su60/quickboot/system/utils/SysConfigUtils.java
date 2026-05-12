package com.su60.quickboot.system.utils;

import com.su60.quickboot.data.spring.SpringContextHolder;
import com.su60.quickboot.system.service.ISysConfigService;
import lombok.experimental.UtilityClass;

@UtilityClass
public class SysConfigUtils {


	// 包路径
	public static String GEN_PARENT_PACKAGE = "gen_parent_package";
	// 代码生成器-作者
	public static String GEN_AUTHOR = "gen_author";
	// 代码生成器-默认模块
	public static String GEN_MODULE_NAME = "gen_module_name";

	/**
	 * 获取配置
	 * @since 2026/1/11
	 * @param key
	 * @return
	 */
	public String getConfig(String key) {
		ISysConfigService configService = SpringContextHolder.getBean(ISysConfigService.class);
		return configService.getConfigValue(key);
	}

}
