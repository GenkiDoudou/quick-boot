package io.github.genkidoudou.common.security.firewall.idempotent;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.UrlPathHelper;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * MVC 拦截：按 {@link Idempotent} 与 {@link IdempotentProperties#getInterceptMethods()} 决定是否对「非空幂等头」占位。
 */
public class IdempotentInterceptor implements HandlerInterceptor {

    /** 获得占位的完整存储 key，供 afterCompletion 清理。 */
    public static final String REQ_ATTR_SLOT_KEY = IdempotentInterceptor.class.getName() + ".slotKey";

    /** 是否为 {@link Idempotent#deleteAfterExecution()} 模式。 */
    public static final String REQ_ATTR_DELETE_AFTER = IdempotentInterceptor.class.getName() + ".deleteAfter";

    /** 本次请求是否由本拦截器成功占位。 */
    public static final String REQ_ATTR_SLOT_HELD = IdempotentInterceptor.class.getName() + ".slotHeld";

    private final IdempotentProperties properties;
    private final IdempotentStore store;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final UrlPathHelper urlPathHelper = new UrlPathHelper();

    public IdempotentInterceptor(IdempotentProperties properties, IdempotentStore store) {
        this.properties = properties;
        this.store = store;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) {
        String path = urlPathHelper.getPathWithinApplication(request);
        if (matchesExclude(path)) {
            return true;
        }

        String rawToken = request.getHeader(properties.getTokenHeader());
        if (!StringUtils.hasText(rawToken)) {
            return true;
        }
        String token = rawToken.trim();

        if (!(handler instanceof HandlerMethod hm)) {
            return true;
        }

        Idempotent ann = hm.getMethodAnnotation(Idempotent.class);
        boolean byAnnotation = ann != null;
        boolean byHttpMethod = matchesInterceptHttpMethod(request.getMethod());
        if (!byAnnotation && !byHttpMethod) {
            return true;
        }

        Duration ttl = resolveTtl(ann);
        String slotKey = buildSlotKey(ann, token);

        if (!store.setIfAbsent(slotKey, ttl)) {
            throw new IdempotentException(properties.getDefaultMessage());
        }

        request.setAttribute(REQ_ATTR_SLOT_KEY, slotKey);
        request.setAttribute(REQ_ATTR_DELETE_AFTER, ann != null && ann.deleteAfterExecution());
        request.setAttribute(REQ_ATTR_SLOT_HELD, Boolean.TRUE);
        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
                                @NonNull HttpServletResponse response,
                                @NonNull Object handler,
                                @Nullable Exception ex) {
        if (!Boolean.TRUE.equals(request.getAttribute(REQ_ATTR_SLOT_HELD))) {
            return;
        }
        String key = (String) request.getAttribute(REQ_ATTR_SLOT_KEY);
        if (!StringUtils.hasText(key)) {
            return;
        }
        boolean deleteAfter = Boolean.TRUE.equals(request.getAttribute(REQ_ATTR_DELETE_AFTER));
        if (ex != null || deleteAfter) {
            store.delete(key);
        }
    }

    private boolean matchesExclude(String path) {
        if (properties.getExcludeUrls() == null) {
            return false;
        }
        for (String pattern : properties.getExcludeUrls()) {
            if (StringUtils.hasText(pattern) && pathMatcher.match(pattern.trim(), path)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesInterceptHttpMethod(String requestMethod) {
        if (properties.getInterceptMethods() == null || properties.getInterceptMethods().isEmpty()) {
            return false;
        }
        if (!StringUtils.hasText(requestMethod)) {
            return false;
        }
        String m = requestMethod.trim().toUpperCase();
        for (String configured : properties.getInterceptMethods()) {
            if (configured != null && m.equals(configured.trim().toUpperCase())) {
                return true;
            }
        }
        return false;
    }

    private Duration resolveTtl(@Nullable Idempotent ann) {
        long amount;
        TimeUnit unit;
        if (ann != null && ann.expireTime() >= 0) {
            amount = ann.expireTime();
            unit = ann.timeUnit();
        } else {
            amount = properties.getExpireTime();
            unit = properties.getExpireTimeUnit();
        }
        return Duration.ofMillis(Math.max(1, unit.toMillis(amount)));
    }

    private String buildSlotKey(@Nullable Idempotent ann, String token) {
        String prefix = properties.getKeyPrefix() == null ? "" : properties.getKeyPrefix();
        String seg = (ann != null && StringUtils.hasText(ann.prefix())) ? ann.prefix() : "";
        return prefix + seg + token;
    }
}
