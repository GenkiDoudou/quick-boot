package io.github.genkidoudou.common.firewall.idempotent;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * 默认幂等键生成器
 *
 * @author QuickBoot
 * @since 2026/03/02
 */
public class DefaultIdempotentKeyGenerator implements IdempotentKeyGenerator {

    private final IdempotentProperties properties;

    public DefaultIdempotentKeyGenerator(IdempotentProperties properties) {
        this.properties = properties;
    }

    @Override
    public String generateKey(ProceedingJoinPoint joinPoint, Idempotent idempotent) {
        KeyGenerateStrategy strategy = idempotent.strategy();
        
        switch (strategy) {
            case URL:
                return generateUrlKey(joinPoint, idempotent);
            case URL_USER:
                return generateUrlUserKey(joinPoint, idempotent);
            case TOKEN:
                return generateTokenKey(joinPoint, idempotent);
            case DEFAULT:
            default:
                return generateDefaultKey(joinPoint, idempotent);
        }
    }

    /**
     * 默认策略：方法签名 + 参数 hashCode
     */
    private String generateDefaultKey(ProceedingJoinPoint joinPoint, Idempotent idempotent) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        
        String prefix = getPrefix(idempotent, method);
        String argsHash = getArgsHash(joinPoint.getArgs());
        
        return prefix + ":" + method.getDeclaringClass().getName() + "." + method.getName() + ":" + argsHash;
    }

    /**
     * URL 策略：请求路径 + 参数 hashCode
     */
    private String generateUrlKey(ProceedingJoinPoint joinPoint, Idempotent idempotent) {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return generateDefaultKey(joinPoint, idempotent);
        }
        
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        
        String prefix = getPrefix(idempotent, method);
        String uri = request.getRequestURI();
        String argsHash = getArgsHash(joinPoint.getArgs());
        
        return prefix + ":url:" + uri + ":" + argsHash;
    }

    /**
     * URL + 用户策略：请求路径 + 用户标识 + 参数 hashCode
     */
    private String generateUrlUserKey(ProceedingJoinPoint joinPoint, Idempotent idempotent) {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return generateDefaultKey(joinPoint, idempotent);
        }
        
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        
        String prefix = getPrefix(idempotent, method);
        String uri = request.getRequestURI();
        String userId = getUserId(request);
        String argsHash = getArgsHash(joinPoint.getArgs());
        
        return prefix + ":url:user:" + uri + ":" + userId + ":" + argsHash;
    }

    /**
     * Token 策略：从请求头获取幂等 Token
     */
    private String generateTokenKey(ProceedingJoinPoint joinPoint, Idempotent idempotent) {
        HttpServletRequest request = getRequest();
        if (request == null) {
            throw new IdempotentException("无法获取 HttpServletRequest");
        }
        
        String token = request.getHeader(properties.getTokenHeader());
        if (!StringUtils.hasText(token)) {
            throw new IdempotentException("缺少幂等 Token，请在请求头中添加 " + properties.getTokenHeader());
        }
        
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String prefix = getPrefix(idempotent, method);
        
        return prefix + ":token:" + token;
    }

    /**
     * 获取前缀
     */
    private String getPrefix(Idempotent idempotent, Method method) {
        if (StringUtils.hasText(idempotent.prefix())) {
            return properties.getKeyPrefix() + ":" + idempotent.prefix();
        }
        return properties.getKeyPrefix();
    }

    /**
     * 获取参数 hash
     */
    private String getArgsHash(Object[] args) {
        if (args == null || args.length == 0) {
            return "noargs";
        }
        
        String argsStr = Arrays.toString(args);
        return DigestUtils.md5DigestAsHex(argsStr.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 获取用户 ID
     */
    private String getUserId(HttpServletRequest request) {
        // 优先从请求头获取
        String userId = request.getHeader("X-User-Id");
        if (StringUtils.hasText(userId)) {
            return userId;
        }
        
        // 从 session 获取（如果有）
        Object userIdObj = request.getSession().getAttribute("userId");
        if (userIdObj != null) {
            return userIdObj.toString();
        }
        
        // 使用 IP 作为备选
        return getClientIp(request);
    }

    /**
     * 获取客户端 IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * 获取当前请求
     */
    private HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
}
