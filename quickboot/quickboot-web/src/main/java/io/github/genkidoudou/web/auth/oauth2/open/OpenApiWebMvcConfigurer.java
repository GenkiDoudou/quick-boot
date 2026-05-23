package io.github.genkidoudou.web.auth.oauth2.open;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 开放 API 路径注册 OAuth2 鉴权拦截器。
 */
@Configuration
@RequiredArgsConstructor
public class OpenApiWebMvcConfigurer implements WebMvcConfigurer {

    private final OpenApiOAuth2Interceptor openApiOAuth2Interceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(openApiOAuth2Interceptor)
                .addPathPatterns("/open-api/**");
    }
}
