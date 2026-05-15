package io.github.genkidoudou.common.firewall.idempotent;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.Order;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * 幂等切面
 *
 * @author QuickBoot
 * @since 2026/03/02
 */
@Aspect
@Order(1)
public class IdempotentAspect implements ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(IdempotentAspect.class);

    private final IdempotentProperties properties;
    private final IdempotentStorage storage;
    private final IdempotentKeyGenerator defaultKeyGenerator;
    private final PathMatcher pathMatcher;
    private ApplicationContext applicationContext;

    public IdempotentAspect(IdempotentProperties properties,
                           IdempotentStorage storage,
                           IdempotentKeyGenerator defaultKeyGenerator,
                           PathMatcher pathMatcher) {
        this.properties = properties;
        this.storage = storage;
        this.defaultKeyGenerator = defaultKeyGenerator;
        this.pathMatcher = pathMatcher;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 拦截 @Idempotent 注解
     */
    @Around("@annotation(idempotent)")
    public Object around(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
        return handleIdempotent(joinPoint, idempotent);
    }

    /**
     * 自动拦截配置的请求方式
     */
    @Around("execution(* *(..)) && (@annotation(org.springframework.web.bind.annotation.RequestMapping) " +
            "|| @annotation(org.springframework.web.bind.annotation.GetMapping) " +
            "|| @annotation(org.springframework.web.bind.annotation.PostMapping) " +
            "|| @annotation(org.springframework.web.bind.annotation.PutMapping) " +
            "|| @annotation(org.springframework.web.bind.annotation.DeleteMapping) " +
            "|| @annotation(org.springframework.web.bind.annotation.PatchMapping))")
    public Object autoIntercept(ProceedingJoinPoint joinPoint) throws Throwable {
        // 如果未配置拦截方式，直接执行
        if (properties.getInterceptMethods() == null || properties.getInterceptMethods().isEmpty()) {
            return joinPoint.proceed();
        }

        // 检查方法是否已有 @Idempotent 注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        if (method.isAnnotationPresent(Idempotent.class)) {
            // 已有注解，由 @Around("@annotation(idempotent)") 处理
            return joinPoint.proceed();
        }

        // 检查是否在排除列表中
        if (isExcluded()) {
            return joinPoint.proceed();
        }

        // 检查当前请求方式是否需要拦截
        if (!isMethodIntercepted()) {
            return joinPoint.proceed();
        }

        // 创建默认的幂等注解
        Idempotent defaultIdempotent = createDefaultIdempotent();
        return handleIdempotent(joinPoint, defaultIdempotent);
    }

    /**
     * 处理幂等逻辑
     */
    private Object handleIdempotent(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
        // 生成幂等键
        String key = generateKey(joinPoint, idempotent);
        
        // 获取过期时间
        long expireTime = idempotent.expireTime() > 0 ? idempotent.expireTime() : properties.getExpireTime();
        TimeUnit timeUnit = idempotent.timeUnit();

        log.debug("幂等检查: key={}, expireTime={}{}", key, expireTime, timeUnit);

        // 尝试设置键
        boolean success = storage.setIfAbsent(key, "1", expireTime, timeUnit);
        if (!success) {
            // 键已存在，说明是重复请求
            log.warn("检测到重复请求: key={}", key);
            throw IdempotentException.duplicateRequest();
        }

        try {
            // 执行目标方法
            Object result = joinPoint.proceed();
            
            // 如果配置了执行后删除，则删除键
            if (idempotent.deleteAfterExecution()) {
                storage.delete(key);
                log.debug("幂等键已删除: key={}", key);
            }
            
            return result;
        } catch (Throwable e) {
            // 发生异常时删除键，允许重试
            storage.delete(key);
            log.debug("执行异常，幂等键已删除: key={}", key);
            throw e;
        }
    }

    /**
     * 生成幂等键
     */
    private String generateKey(ProceedingJoinPoint joinPoint, Idempotent idempotent) {
        IdempotentKeyGenerator keyGenerator = defaultKeyGenerator;
        
        // 如果使用自定义策略，获取自定义键生成器
        if (idempotent.strategy() == KeyGenerateStrategy.CUSTOM) {
            if (!StringUtils.hasText(idempotent.keyGenerator())) {
                throw IdempotentException.duplicateRequest();
            }
            keyGenerator = applicationContext.getBean(idempotent.keyGenerator(), IdempotentKeyGenerator.class);
        }
        
        return keyGenerator.generateKey(joinPoint, idempotent);
    }

    /**
     * 检查当前请求是否在排除列表中
     */
    private boolean isExcluded() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return false;
        }

        String uri = request.getRequestURI();
        for (String pattern : properties.getExcludeUrls()) {
            if (pathMatcher.match(pattern, uri)) {
                log.debug("请求 {} 在排除列表中，跳过幂等检查", uri);
                return true;
            }
        }
        return false;
    }

    /**
     * 检查当前请求方式是否需要拦截
     */
    private boolean isMethodIntercepted() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return false;
        }

        String method = request.getMethod();
        boolean intercepted = properties.getInterceptMethods().stream()
                .anyMatch(m -> m.equalsIgnoreCase(method));
        
        if (!intercepted) {
            log.debug("请求方式 {} 不在拦截列表中，跳过幂等检查", method);
        }
        
        return intercepted;
    }

    /**
     * 创建默认的幂等注解
     */
    private Idempotent createDefaultIdempotent() {
        return new Idempotent() {
            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return Idempotent.class;
            }

            @Override
            public String prefix() {
                return "";
            }

            @Override
            public KeyGenerateStrategy strategy() {
                return KeyGenerateStrategy.DEFAULT;
            }

            @Override
            public String keyGenerator() {
                return "";
            }

            @Override
            public long expireTime() {
                return -1;
            }

            @Override
            public TimeUnit timeUnit() {
                return TimeUnit.SECONDS;
            }

            @Override
            public String message() {
                return properties.getDefaultMessage();
            }

            @Override
            public boolean deleteAfterExecution() {
                return false;
            }
        };
    }

    /**
     * 获取当前请求
     */
    private HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
}
