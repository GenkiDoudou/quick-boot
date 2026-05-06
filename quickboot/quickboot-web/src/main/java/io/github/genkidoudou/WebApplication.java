package io.github.genkidoudou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Web 模块启动入口。
 * <p>
 * {@link EnableCaching}：启用 Spring Cache，以便 {@code quickboot-common} 中按 {@code spring.cache.type}
 * 注册的 {@link org.springframework.cache.CacheManager}（Caffeine / Redis）生效。
 */
@SpringBootApplication
@EnableCaching
public class WebApplication {

    /**
     * @param args 命令行参数（可传入 {@code --spring.profiles.active=prod} 等）
     */
    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
    }
}
