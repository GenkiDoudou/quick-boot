package io.github.genkidoudou.web.system.user.datascope;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据权限：在 Service 方法执行期间通过 AOP 写入 {@link DataPermissionContext}，
 * 由 {@link DataPermissionInnerInterceptor} 对匹配的 SELECT 追加条件（参照旧 quick-boot）。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataPermission {

    /**
     * 参与控制的物理表名，支持通配（如 {@code order_*}）。
     */
    String[] tables();

    /**
     * 部门字段名。
     */
    String deptField() default "dept_id";

    /**
     * 用户字段名（「仅本人」等场景）；{@code sys_user} 应使用 {@code user_id}。
     */
    String userField() default "user_id";

    /**
     * 为 true 时跳过注入。
     */
    boolean ignore() default false;
}
