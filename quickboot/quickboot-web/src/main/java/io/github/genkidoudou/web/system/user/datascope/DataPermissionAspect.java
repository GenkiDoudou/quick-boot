package io.github.genkidoudou.web.system.user.datascope;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 参照旧 quick-boot：在带 {@link DataPermission} 的 Service 方法执行期间注入 ThreadLocal。
 */
@Aspect
@Component
@Order(0)
public class DataPermissionAspect {

    /**
     * @param pjp  连接点
     * @param perm 注解实例
     * @return 原方法返回值
     */
    @Around("@annotation(perm)")
    public Object around(ProceedingJoinPoint pjp, DataPermission perm) throws Throwable {
        try {
            DataPermissionContext.set(perm);
            return pjp.proceed();
        } finally {
            DataPermissionContext.clear();
        }
    }
}
