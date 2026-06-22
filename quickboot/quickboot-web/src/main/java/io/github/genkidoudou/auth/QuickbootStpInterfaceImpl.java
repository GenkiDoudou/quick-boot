package io.github.genkidoudou.auth;

import cn.dev33.satoken.stp.StpInterface;
import io.github.genkidoudou.web.system.menu.service.MenuService;
import io.github.genkidoudou.web.system.user.authcache.UserAuthCacheService;
import io.github.genkidoudou.web.system.user.authcache.UserAuthSessionStore;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Sa-Token 权限数据源：优先读 Session 快照，未命中再走 Spring Cache + DB（与 {@code /getInfo} 一致）。
 */
@Component
public class QuickbootStpInterfaceImpl implements StpInterface {

    private final MenuService menuService;
    private final UserAuthCacheService userAuthCacheService;

    public QuickbootStpInterfaceImpl(MenuService menuService, UserAuthCacheService userAuthCacheService) {
        this.menuService = menuService;
        this.userAuthCacheService = userAuthCacheService;
    }

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        long uid = parseUserId(loginId);
        long version = userAuthCacheService.currentGlobalVersion();
        List<String> cached = UserAuthSessionStore.getPermissionsIfValid(version);
        if (cached != null) {
            return cached;
        }
        List<String> permissions = menuService.listPermissionsByUserId(uid);
        List<String> roles = menuService.listRoleKeysByUserId(uid);
        UserAuthSessionStore.save(roles, permissions, version);
        return permissions;
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        long uid = parseUserId(loginId);
        long version = userAuthCacheService.currentGlobalVersion();
        List<String> cached = UserAuthSessionStore.getRolesIfValid(version);
        if (cached != null) {
            return cached;
        }
        List<String> roles = menuService.listRoleKeysByUserId(uid);
        List<String> permissions = menuService.listPermissionsByUserId(uid);
        UserAuthSessionStore.save(roles, permissions, version);
        return roles;
    }

    private static long parseUserId(Object loginId) {
        if (loginId instanceof Number n) {
            return n.longValue();
        }
        return Long.parseLong(loginId.toString());
    }
}
