package io.github.genkidoudou.common.captcha;

import cloud.tianai.captcha.cache.CacheStore;
import cloud.tianai.captcha.cache.impl.LocalCacheStore;
import cloud.tianai.captcha.common.constant.CaptchaTypeConstant;
import cloud.tianai.captcha.resource.ResourceStore;
import cloud.tianai.captcha.resource.common.model.dto.Resource;
import cloud.tianai.captcha.resource.impl.LocalMemoryResourceStore;
import cloud.tianai.captcha.spring.autoconfiguration.CacheStoreAutoConfiguration;
import cloud.tianai.captcha.spring.plugins.RedisResourceStore;
import cloud.tianai.captcha.spring.store.impl.RedisCacheStore;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * tianai验证码自动配置类
 *
 * @author luyanan
 * @since 2026/3/10
 */
@Slf4j
@EnableConfigurationProperties(CaptchaProperties.class)
@ConditionalOnProperty(prefix = "qc.captcha", name = "enabled", havingValue = "true", matchIfMissing = true)
@AutoConfigureAfter(name = {"org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration",
        "org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration"})
@Configuration(proxyBeanMethods = false)
@EnableAutoConfiguration(exclude = {CacheStoreAutoConfiguration.class})
public class TianaiCacheStoreAutoConfiguration {


    /**
     * RedisCacheStoreConfiguration
     *
     * @author 天爱有情
     * @since 2020/10/27 14:06
     */
    @Order(1)
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnBean(StringRedisTemplate.class)
    public static class RedisCacheStoreConfiguration {

        @Bean(destroyMethod = "close")
        @ConditionalOnBean(StringRedisTemplate.class)
        @ConditionalOnMissingBean(CacheStore.class)
        public CacheStore redis(StringRedisTemplate redisTemplate) {
            return new RedisCacheStore(redisTemplate);
        }


//        @Bean
//        @ConditionalOnBean(StringRedisTemplate.class)
//        @ConditionalOnMissingBean(ResourceStore.class)
//        public ResourceStore redisResourceStore(StringRedisTemplate redisTemplate) {
//            return new RedisResourceStore(redisTemplate);
//        }
    }

    /**
     * LocalCacheStoreConfiguration
     *
     * @author 天爱有情
     * @since 2020/10/27 14:06
     */
    @Order(2)
    @Configuration(proxyBeanMethods = false)
    public static class LocalCacheStoreConfiguration {

        @Bean(destroyMethod = "close")
        @ConditionalOnMissingBean(CacheStore.class)
        public CacheStore local() {
            return new LocalCacheStore();
        }

//        @Bean
//        @ConditionalOnMissingBean(ResourceStore.class)
//        public ResourceStore resourceStore() {
//            return new LocalMemoryResourceStore();
//        }

    }

    /**
     * 配置验证码资源存储器
     *
     * @return ResourceStore
     */
    @Bean
    public ResourceStore resourceStore() {
        // 使用简单的本地内存存储器，实际项目中可以使用数据库等存储
        LocalMemoryResourceStore resourceStore = new LocalMemoryResourceStore();

        // 配置背景图
        // arg1: 验证码类型(SLIDER、ROTATE、CONCAT、WORD_IMAGE_CLICK)
        // arg2: Resource对象，包含: 资源类型(calsspath、file、url)、文件路径、tag标签

        // 滑块验证码背景图 (600x360)
        List<String> files = new ArrayList<>();
        files.add("bgimages/bg1.png");
        files.add("bgimages/bg2.png");
        files.add("bgimages/bg3.png");
        files.add("bgimages/bg4.png");
        files.add("bgimages/bg5.png");
        files.add("bgimages/48.jpg");
        files.add("bgimages/a.jpg");
        files.add("bgimages/b.jpg");
        files.add("bgimages/c.jpg");
        files.add("bgimages/d.jpg");
        files.add("bgimages/e.jpg");
        files.add("bgimages/f.jpg");
        files.add("bgimages/j.jpg");
        files.add("bgimages/h.jpg");
        files.add("bgimages/i.jpg");
        files.add("bgimages/j.jpg");
        for (String file : files) {
//            滑块
            resourceStore.addResource(CaptchaTypeConstant.SLIDER, new Resource("classpath", file, "default"));
//            旋转验证码背景图
            resourceStore.addResource(CaptchaTypeConstant.ROTATE, new Resource("classpath", file, "default"));
            // 点选验证码背景图 (600x360)
            resourceStore.addResource(CaptchaTypeConstant.CONCAT, new Resource("classpath", file, "default"));
            // 文字点选验证码背景图 (600x360)
            resourceStore.addResource(CaptchaTypeConstant.WORD_IMAGE_CLICK, new Resource("classpath", "bgimages/bg4.png", "default"));
        }

        return resourceStore;
    }
}
