package io.github.genkidoudou.web.system.user.authcache;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;

import java.util.Collections;
import java.util.List;

/**
 * 在 Sa-Token Session 中缓存当前登录用户的角色/权限列表，供 {@code StpInterface} 优先读取。
 * <p>
 * 与 {@link UserAuthCacheService} 的全局版本号配合：菜单变更 bump 版本后，旧 Session 快照自动失效。
 */
public final class UserAuthSessionStore {

    /** Session 键：角色标识列表。 */
    public static final String SESSION_KEY_ROLES = "qbAuthRoles";
    /** Session 键：权限标识列表。 */
    public static final String SESSION_KEY_PERMISSIONS = "qbAuthPermissions";
    /** Session 键：与全局版本对齐的快照版本。 */
    public static final String SESSION_KEY_AUTH_VERSION = "qbAuthVersion";

    private UserAuthSessionStore() {
    }

    /**
     * 写入当前登录会话（须在 {@link StpUtil#login(Object)} 之后调用）。
     *
     * @param roles       角色 key 列表
     * @param permissions 权限标识列表
     * @param authVersion 全局权限版本
     */
    public static void save(List<String> roles, List<String> permissions, long authVersion) {
        SaSession session = StpUtil.getSession();
        session.set(SESSION_KEY_ROLES, copyList(roles));
        session.set(SESSION_KEY_PERMISSIONS, copyList(permissions));
        session.set(SESSION_KEY_AUTH_VERSION, authVersion);
    }

    /**
     * @param authVersion 当前全局版本
     * @return 会话中有效的角色列表；无效或未写入时返回 {@code null}
     */
    @SuppressWarnings("unchecked")
    public static List<String> getRolesIfValid(long authVersion) {
        if (!isSessionValid(authVersion)) {
            return null;
        }
        return (List<String>) StpUtil.getSession().get(SESSION_KEY_ROLES);
    }

    /**
     * @param authVersion 当前全局版本
     * @return 会话中有效的权限列表；无效或未写入时返回 {@code null}
     */
    @SuppressWarnings("unchecked")
    public static List<String> getPermissionsIfValid(long authVersion) {
        if (!isSessionValid(authVersion)) {
            return null;
        }
        return (List<String>) StpUtil.getSession().get(SESSION_KEY_PERMISSIONS);
    }

    /**
     * 清除指定用户会话中的权限快照（角色/菜单变更时调用）。
     *
     * @param userId 用户 id
     */
    public static void clearForLoginId(long userId) {
        try {
            SaSession session = StpUtil.getSessionByLoginId(userId, false);
            if (session == null) {
                return;
            }
            session.delete(SESSION_KEY_ROLES);
            session.delete(SESSION_KEY_PERMISSIONS);
            session.delete(SESSION_KEY_AUTH_VERSION);
        } catch (Exception ignored) {
            // 用户未在线或 token 已失效
        }
    }

    private static boolean isSessionValid(long authVersion) {
        try {
            if (!StpUtil.isLogin()) {
                return false;
            }
            Object v = StpUtil.getSession().get(SESSION_KEY_AUTH_VERSION);
            return v instanceof Number n && n.longValue() == authVersion;
        } catch (Exception ignored) {
            return false;
        }
    }

    private static List<String> copyList(List<String> source) {
        if (source == null || source.isEmpty()) {
            return List.of();
        }
        return List.copyOf(source);
    }
}
