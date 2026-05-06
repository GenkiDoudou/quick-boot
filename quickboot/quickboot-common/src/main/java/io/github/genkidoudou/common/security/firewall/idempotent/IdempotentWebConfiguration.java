package io.github.genkidoudou.common.security.firewall.idempotent;

import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 注册 {@link IdempotentInterceptor}；顺序略早于部分默认拦截链节点。
 */
@RequiredArgsConstructor
public class IdempotentWebConfiguration implements WebMvcConfigurer {

    private final IdempotentInterceptor idempotentInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(idempotentInterceptor).order(Ordered.LOWEST_PRECEDENCE - 50);
    }
}
