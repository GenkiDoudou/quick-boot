package com.su60.quickboot.data.mybatisplus;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * mybatis plus 扩展类配置类
 *
 * @author luyanan
 * @since 2023/09/16
 **/
@Configuration
@EnableConfigurationProperties(MybatisPlusExtentProperties.class)
@ConditionalOnClass(MybatisPlusAutoConfiguration.class)
@ConditionalOnProperty(prefix = MybatisPlusExtentProperties.PREFIX, value = "enable", havingValue = "true", matchIfMissing = true)
public class MybatisPlusExtendAutoConfiguration {

	/**
	 * sql扩展注入
	 *
	 * @return sql注入器
	 * @since 2023/09/16
	 */
	@Bean
	@Primary
	public MySqlInjector mySqlInjector() {
		return new MySqlInjector();
	}


	/**
	 * 添加分页插件
	 *
	 * @param plusExtentProperties 扩展配置文件
	 * @return 插件
	 */
	@Bean
	public MybatisPlusInterceptor mybatisPlusInterceptor(MybatisPlusExtentProperties plusExtentProperties) {
		MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

		PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor();
		if (null != plusExtentProperties.getPage() && plusExtentProperties.getPage()) {
			if (null != plusExtentProperties.getMaxLimit()) {
				paginationInnerInterceptor.setMaxLimit(plusExtentProperties.getMaxLimit());

			}

			interceptor.addInnerInterceptor(paginationInnerInterceptor);
		}

		return interceptor;
	}

}
