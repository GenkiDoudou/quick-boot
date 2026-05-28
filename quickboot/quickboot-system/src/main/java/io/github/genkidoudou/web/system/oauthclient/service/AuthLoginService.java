package io.github.genkidoudou.web.system.oauthclient.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.common.security.firewall.password.PasswordCodec;
import cn.dev33.satoken.stp.StpUtil;
import io.github.genkidoudou.web.system.user.domain.SysUser;
import io.github.genkidoudou.web.system.user.mapper.SysUserMapper;
import org.springframework.stereotype.Service;

/**
 * 账号密码登录：从 {@code sys_user} 校验账号状态与 {@link PasswordCodec} 密码。
 */
@Service
public class AuthLoginService {

    private final SysUserMapper userMapper;
    private final PasswordCodec passwordCodec;

    public AuthLoginService(SysUserMapper userMapper, PasswordCodec passwordCodec) {
        this.userMapper = userMapper;
        this.passwordCodec = passwordCodec;
    }

    /**
     * 校验登录名与密码，返回可登录用户主键。
     *
     * @param username 登录名（将 trim）
     * @param password 明文密码
     * @return 用户 id
     * @throws WarningException 凭据错误、账号停用或不存在时统一抛出（HTTP 由全局异常映射）
     */
    public long authenticate(String username, String password) {
        if (StrUtil.isBlank(username) || StrUtil.isBlank(password)) {
            throw new WarningException(ErrorCodes.Security.UNAUTHORIZED, "用户名或密码错误");
        }
        String name = username.trim();
        SysUser u = userMapper.selectOne(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getUserName, name)
                .last("LIMIT 1"));
        if (u == null) {
            throw new WarningException(ErrorCodes.Security.UNAUTHORIZED, "用户名或密码错误");
        }
        if (!"0".equals(u.getDelFlag())) {
            throw new WarningException(ErrorCodes.Security.UNAUTHORIZED, "用户名或密码错误");
        }
        if (!"0".equals(u.getStatus())) {
            throw new WarningException(ErrorCodes.Security.FORBIDDEN, "账号已停用");
        }
        String stored = u.getPassword();
        if (StrUtil.isBlank(stored) || !passwordCodec.matches(password, stored)) {
            throw new WarningException(ErrorCodes.Security.UNAUTHORIZED, "用户名或密码错误");
        }
        return u.getUserId();
    }

    /**
     * 校验当前已登录用户的登录密码（敏感操作二次确认，如查看 OAuth client_secret）。
     *
     * @param password 当前用户明文密码
     * @throws WarningException 未登录或密码错误
     */
    public void verifyCurrentUserPassword(String password) {
        if (StrUtil.isBlank(password)) {
            throw new WarningException(ErrorCodes.Security.UNAUTHORIZED, "当前用户密码不正确");
        }
        long userId = StpUtil.getLoginIdAsLong();
        SysUser u = userMapper.selectById(userId);
        if (u == null || !"0".equals(u.getDelFlag())) {
            throw new WarningException(ErrorCodes.Security.UNAUTHORIZED, "当前用户密码不正确");
        }
        if (!"0".equals(u.getStatus())) {
            throw new WarningException(ErrorCodes.Security.FORBIDDEN, "账号已停用");
        }
        String stored = u.getPassword();
        if (StrUtil.isBlank(stored) || !passwordCodec.matches(password, stored)) {
            throw new WarningException(ErrorCodes.Security.UNAUTHORIZED, "当前用户密码不正确");
        }
    }
}
