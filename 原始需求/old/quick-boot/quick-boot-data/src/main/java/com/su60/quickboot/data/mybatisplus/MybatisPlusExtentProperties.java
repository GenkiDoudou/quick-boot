package com.su60.quickboot.data.mybatisplus;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * mybatis plus 扩展类的配置
 *
 * @author luyanan
 * @since 2024/03/21
 **/
@ConfigurationProperties(prefix = MybatisPlusExtentProperties.PREFIX)
public class MybatisPlusExtentProperties {


	public static final String PREFIX = "mybatisplus.extend";


	/**
	 * 是否开启
	 *
	 * @since 2024/03/21
	 */

	private Boolean enable;


	/**
	 * 是否开启分页
	 *
	 * @since 2024/03/22
	 */
	private Boolean page = true;
	/**
	 * 单页分页条数限制
	 *
	 * @since 2024/03/22
	 */

	private Long maxLimit;


	/**
	 * id生成策略
	 *
	 * @since 2024/03/22
	 */

	private String idGenerator = "yid";


	public Boolean getEnable() {
		return enable;
	}

	public void setEnable(Boolean enable) {
		this.enable = enable;
	}

	public Boolean getPage() {
		return page;
	}

	public void setPage(Boolean page) {
		this.page = page;
	}

	public Long getMaxLimit() {
		return maxLimit;
	}

	public void setMaxLimit(Long maxLimit) {
		this.maxLimit = maxLimit;
	}

	public String getIdGenerator() {
		return idGenerator;
	}

	public void setIdGenerator(String idGenerator) {
		this.idGenerator = idGenerator;
	}
}
