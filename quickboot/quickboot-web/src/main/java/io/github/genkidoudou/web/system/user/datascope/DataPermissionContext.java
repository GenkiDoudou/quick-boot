package io.github.genkidoudou.web.system.user.datascope;

/**
 * 当前线程的 {@link DataPermission}（由 {@link DataPermissionAspect} 在带注解的 Service 方法边界内设置）。
 */
public final class DataPermissionContext {

    private static final ThreadLocal<DataPermission> HOLDER = new ThreadLocal<>();

    private DataPermissionContext() {
    }

    public static void set(DataPermission ann) {
        HOLDER.set(ann);
    }

    public static DataPermission get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
