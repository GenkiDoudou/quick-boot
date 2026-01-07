package com.su60.quickboot.web;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * 启动类
 *
 * @author luyanan
 * @since 2024/04/15
 **/
@EnableAspectJAutoProxy(exposeProxy = true)

@EnableCaching
@MapperScan("com.su60.quickboot.*.mapper")
@SpringBootApplication(scanBasePackages = {"com.su60.quickboot.system",
		"com.su60.quickboot.core",
		"com.su60.quickboot.demo",
		"com.su60.quickboot.quartz",
		"com.su60.quickboot.generator",
		"com.su60.quickboot.web"})
public class QuickBootApplication {

	/**
	 * 启动类
	 *
	 * @param args 参数
	 * @return
	 * @since 2024/04/16
	 */
	public static void main(String[] args) {
		SpringApplication.run(QuickBootApplication.class);
	}
}
