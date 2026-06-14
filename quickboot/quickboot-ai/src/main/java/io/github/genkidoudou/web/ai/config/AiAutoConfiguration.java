package io.github.genkidoudou.web.ai.config;



import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import org.springframework.boot.context.properties.EnableConfigurationProperties;

import org.springframework.context.annotation.ComponentScan;

import org.springframework.context.annotation.Configuration;

import org.springframework.context.annotation.FilterType;



/**

 * AI 大模型模块自动配置入口。

 * <p>

 * 当 {@code qc.ai.enabled=true} 时注册 Controller、Service、Mapper 及 Registry 相关 Bean。

 * 提示词子域由 {@link io.github.genkidoudou.web.ai.prompt.config.AiPromptAutoConfiguration} 独立注册。

 */

@Configuration

@ConditionalOnProperty(prefix = "qc.ai", name = "enabled", havingValue = "true")

@EnableConfigurationProperties(AiProperties.class)

@ComponentScan(

    basePackages = "io.github.genkidoudou.web.ai",

    excludeFilters = @ComponentScan.Filter(

        type = FilterType.REGEX,

        pattern = "io\\.github\\.genkidoudou\\.web\\.ai\\.prompt\\..*"

    )

)

public class AiAutoConfiguration {

}

