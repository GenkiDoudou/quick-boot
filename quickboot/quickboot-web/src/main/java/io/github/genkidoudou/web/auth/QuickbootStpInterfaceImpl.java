package io.github.genkidoudou.web.auth;

import cn.dev33.satoken.stp.StpInterface;
import io.github.genkidoudou.web.system.menu.service.MenuService;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Sa-Token 权限数据源：按登录用户从库加载角色标识与菜单权限串，与 {@code /getInfo} 一致。
 */
@Component
public class QuickbootStpInterfaceImpl implements StpInterface {

    private final MenuService menuService;

    public QuickbootStpInterfaceImpl(MenuService menuService) {
        this.menuService = menuService;
    }

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        long uid = parseUserId(loginId);
        return menuService.listPermissionsByUserId(uid);
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        long uid = parseUserId(loginId);
        return menuService.listRoleKeysByUserId(uid);
    }

    private static long parseUserId(Object loginId) {
        if (loginId instanceof Number n) {
            return n.longValue();
        }
        return Long.parseLong(loginId.toString());
    }
}
