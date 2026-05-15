package io.github.genkidoudou.satoken;

/**
 * 自定义权限加载接口实现类
 *
 * @author luyanan
 * @since 2026/3/13
 */

import cn.dev33.satoken.stp.StpInterface;

import java.util.List;

public class StpInterfaceImpl implements StpInterface {
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return List.of();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return List.of();
    }
}
