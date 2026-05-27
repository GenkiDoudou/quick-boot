package io.github.genkidoudou.report.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * 注册修复版 {@code dragRedisUtil}，并可选提供适合 JimuBI 计数/缓存的 {@link RedisTemplate}。
 */
@Configuration
@ConditionalOnClass(org.jeecg.modules.drag.util.f.class)
@ConditionalOnProperty(prefix = "qc.jimu", name = "enabled", havingValue = "true", matchIfMissing = true)
@AutoConfigureAfter(name = "org.jeecg.modules.jmreport.config.init.JimuReportConfiguration")
public class JimuDragRedisConfiguration {

    @Bean
    @Primary
    public org.jeecg.modules.drag.util.f fixedDragRedisUtil(
            ObjectProvider<RedisTemplate<String, Object>> redisTemplates) {
        FixedDragRedisUtil util = new FixedDragRedisUtil();
        RedisTemplate<String, Object> dragTemplate = redisTemplates.getIfAvailable();
        if (dragTemplate == null) {
            return util;
        }
        FixedDragRedisUtil.injectRedisTemplate(util, dragTemplate);
        return util;
    }

    /**
     * 无应用级 RedisTemplate 时，为 JimuBI 提供字符串序列化的模板（避免 increment 与 JSON 序列化冲突）。
     */
    @Bean
    @ConditionalOnClass(RedisConnectionFactory.class)
    @ConditionalOnProperty(prefix = "qc.jimu.redis", name = "enabled", havingValue = "true", matchIfMissing = false)
    public RedisTemplate<String, Object> jimuDragRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        StringRedisSerializer stringSerializer = StringRedisSerializer.UTF_8;
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(stringSerializer);
        template.afterPropertiesSet();
        return template;
    }
}
