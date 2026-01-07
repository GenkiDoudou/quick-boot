package com.su60.quickboot.data.datascope;


/**
 * 数据权限上下文（ThreadLocal）
 */
public final class DataPermissionContext {

	private static final ThreadLocal<DataPermission> HOLDER = new ThreadLocal<>();

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
