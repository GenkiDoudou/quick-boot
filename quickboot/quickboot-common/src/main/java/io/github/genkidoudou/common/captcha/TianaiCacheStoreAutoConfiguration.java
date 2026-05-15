package io.github.genkidoudou.common.captcha;

import cloud.tianai.captcha.cache.CacheStore;
import cloud.tianai.captcha.cache.impl.LocalCacheStore;
import cloud.tianai.captcha.common.constant.CaptchaTypeConstant;
import cloud.tianai.captcha.resource.ResourceStore;
import cloud.tianai.captcha.resource.common.model.dto.Resource;
import cloud.tianai.captcha.resource.impl.LocalMemoryResourceStore;
import cloud.tianai.captcha.spring.autoconfiguration.ImageCaptchaAutoConfiguration;
import cloud.tianai.captcha.spring.store.impl.RedisCacheStore;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * 在无 Redis 或显式指定 {@code qc.captcha.store=local} 时使用内存 {@link CacheStore}，避免与 tianai 自带 Redis 装配冲突。
 * <p>
 * 本类必须在 {@link ImageCaptchaAutoConfiguration} 之前执行：后者创建
 * {@link cloud.tianai.captcha.resource.ImageCaptchaResourceManager} 时带有
 * {@code @ConditionalOnBean(ResourceStore.class)}，若 {@link ResourceStore} 注册过晚则该 Bean 被跳过，
 * 进而导致 {@code imageCaptchaTemplate} 装配失败。
 * <p>
 * 另需在应用配置中排除 {@code cloud.tianai.captcha.spring.autoconfiguration.CacheStoreAutoConfiguration}
 * （见 {@code quickboot-web} 的 {@code application.yml}）；勿在本类上使用 {@code @EnableAutoConfiguration(exclude)}，
 * 否则通过 {@code AutoConfiguration.imports} 加载时会形成循环 {@code @Import}。
 *
 * @author luyanan
 * @since 2026/3/10
 */
@EnableConfigurationProperties(CaptchaProperties.class)
@ConditionalOnProperty(prefix = "qc.captcha", name = "enabled", havingValue = "true", matchIfMissing = true)
@AutoConfiguration
@AutoConfigureBefore(ImageCaptchaAutoConfiguration.class)
@AutoConfigureAfter(RedisAutoConfiguration.class)
public class TianaiCacheStoreAutoConfiguration {

    /**
     * 使用 Redis 存储验证码数据（需可用 {@link StringRedisTemplate} 且 {@code qc.captcha.store=redis}）。
     */
    @Order(1)
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = "qc.captcha", name = "store", havingValue = "redis")
    @ConditionalOnBean(StringRedisTemplate.class)
    public static class RedisCacheStoreConfiguration {

        @Bean(destroyMethod = "close")
        @ConditionalOnMissingBean(CacheStore.class)
        public CacheStore redis(StringRedisTemplate redisTemplate) {
            return new RedisCacheStore(redisTemplate);
        }
    }

    /**
     * 本地内存 {@link CacheStore}（默认，适合开发机未起 Redis）。
     */
    @Order(2)
    @Configuration(proxyBeanMethods = false)
    public static class LocalCacheStoreConfiguration {

        @Bean(destroyMethod = "close")
        @ConditionalOnMissingBean(CacheStore.class)
        public CacheStore local() {
            return new LocalCacheStore();
        }
    }

    /**
     * 背景图等资源（classpath:bgimages）。
     *
     * @return 资源存储
     */
    @Bean
    public ResourceStore resourceStore() {
        LocalMemoryResourceStore resourceStore = new LocalMemoryResourceStore();
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
        files.add("bgimages/g.jpg");
        files.add("bgimages/j.jpg");
        files.add("bgimages/h.jpg");
        files.add("bgimages/i.jpg");
        for (String file : files) {
            resourceStore.addResource(CaptchaTypeConstant.SLIDER, new Resource("classpath", file, "default"));
            resourceStore.addResource(CaptchaTypeConstant.ROTATE, new Resource("classpath", file, "default"));
            resourceStore.addResource(CaptchaTypeConstant.CONCAT, new Resource("classpath", file, "default"));
            resourceStore.addResource(CaptchaTypeConstant.WORD_IMAGE_CLICK, new Resource("classpath", "bgimages/bg4.png", "default"));
        }
        return resourceStore;
    }
}
