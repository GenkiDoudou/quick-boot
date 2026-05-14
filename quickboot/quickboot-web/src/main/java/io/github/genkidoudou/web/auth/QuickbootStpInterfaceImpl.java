package io.github.genkidoudou.web.auth;

import cn.dev33.satoken.stp.StpInterface;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Sa-Token 权限数据源占位实现：与 {@link AuthController#getInfo} 中返回的 {@code *:*:*} 对齐，
 * 便于 {@code @SaCheckPermission} 在登录后即可通过校验；后续接入 RBAC 时应改为按用户从库加载。
 */
@Component
public class QuickbootStpInterfaceImpl implements StpInterface {

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return Collections.singletonList("*:*:*");
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return Collections.emptyList();
    }
}
