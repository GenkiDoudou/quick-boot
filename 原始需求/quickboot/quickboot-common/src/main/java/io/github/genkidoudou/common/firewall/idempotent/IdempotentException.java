package io.github.genkidoudou.common.firewall.idempotent;

import io.github.genkidoudou.common.exception.ErrorCode;
import io.github.genkidoudou.common.exception.WarningException;

/**
 * 幂等异常
 *
 * @author QuickBoot
 * @since 2026/03/02
 */
public class IdempotentException extends WarningException {

    public IdempotentException(String msg) {
        super(ErrorCode.IDEMPOTENT_DUPLICATE_REQUEST, msg);
    }

    public IdempotentException(Integer code, String msg) {
        super(code, msg);
    }

    public IdempotentException(Integer code, String msg, Throwable cause) {
        super(code, msg, cause);
    }

    /**
     * 重复请求
     *
     * @return 幂等异常
     * @since 2026/03/05
     */
    public static IdempotentException duplicateRequest() {
        return new IdempotentException(ErrorCode.IDEMPOTENT_DUPLICATE_REQUEST, "重复请求,请稍后再试");
    }
}
