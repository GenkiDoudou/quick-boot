package io.github.genkidoudou.common.firewall.client.exception;

import io.github.genkidoudou.common.exception.ErrorCode;
import io.github.genkidoudou.common.exception.WarningException;

/**
 * 客户端认证异常
 *
 * @author luyanan
 * @since 2026-03-04
 */
public class ClientAuthException extends WarningException {
    public ClientAuthException(Integer code) {
        super(code);
    }

    public ClientAuthException(Integer code, String msg) {
        super(code, msg);
    }

    public ClientAuthException(Integer code, String msg, Object[] args) {
        super(code, msg, args);
    }

    public ClientAuthException(Integer code, String msg, Throwable cause) {
        super(code, msg, cause);
    }

    public ClientAuthException(Integer code, String msg, Object[] args, Throwable cause) {
        super(code, msg, args, cause);
    }

    /**
     * 客户端不存在
     *
     * @param clientId 客户端ID
     * @return 客户端认证异常
     * @since 2026/03/05
     */
    public static ClientAuthException clientNotFound(String clientId) {
        return new ClientAuthException(ErrorCode.CLIENT_NOT_FOUND, "客户端不存在", new Object[]{clientId});
    }

    /**
     * 客户端已禁用
     *
     * @param clientId 客户端ID
     * @return 客户端认证异常
     * @since 2026/03/05
     */
    public static ClientAuthException clientDisabled(String clientId) {
        return new ClientAuthException(ErrorCode.CLIENT_DISABLED, "客户端已禁用", new Object[]{clientId});
    }

    /**
     * 客户端已过期
     *
     * @param clientId 客户端ID
     * @return 客户端认证异常
     * @since 2026/03/05
     */
    public static ClientAuthException clientExpired(String clientId) {
        return new ClientAuthException(ErrorCode.CLIENT_EXPIRED, "客户端已过期", new Object[]{clientId});
    }

    /**
     * 客户端密钥错误
     *
     * @param clientId 客户端ID
     * @return 客户端认证异常
     * @since 2026/03/05
     */
    public static ClientAuthException clientSecretInvalid(String clientId) {
        return new ClientAuthException(ErrorCode.CLIENT_SECRET_INVALID, "客户端密钥错误", new Object[]{clientId});
    }

    /**
     * 缺少客户端认证信息
     *
     * @return 客户端认证异常
     * @since 2026/03/05
     */
    public static ClientAuthException credentialsMissing() {
        return new ClientAuthException(ErrorCode.CLIENT_CREDENTIALS_MISSING, "缺少客户端认证信息");
    }
}
