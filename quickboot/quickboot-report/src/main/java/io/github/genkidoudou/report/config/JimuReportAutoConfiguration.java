package io.github.genkidoudou.report.config;

import io.github.genkidoudou.report.security.JimuShareAccessFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
@ConditionalOnProperty(prefix = "qc.jimu", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(JimuProperties.class)
@ComponentScan("io.github.genkidoudou.report")
public class JimuReportAutoConfiguration {

    @Bean
    public FilterRegistrationBean<JimuShareAccessFilter> jimuShareAccessFilterRegistration(JimuShareAccessFilter filter) {
        FilterRegistrationBean<JimuShareAccessFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(filter);
        bean.addUrlPatterns("/*");
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 20);
        return bean;
    }
}
