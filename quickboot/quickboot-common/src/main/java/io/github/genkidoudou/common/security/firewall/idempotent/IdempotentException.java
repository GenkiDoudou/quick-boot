package io.github.genkidoudou.common.security.firewall.idempotent;

import io.github.genkidoudou.common.api.HttpCodes;

/**
 * 幂等窗口内重复请求时抛出；业务码固定 {@link HttpCodes#IDEMPOTENT_REPEAT}（30201）。
 */
public class IdempotentException extends RuntimeException {

    private final int code;

    public IdempotentException(String message) {
        super(message);
        this.code = HttpCodes.IDEMPOTENT_REPEAT;
    }

    public IdempotentException(String message, Throwable cause) {
        super(message, cause);
        this.code = HttpCodes.IDEMPOTENT_REPEAT;
    }

    /** @return 恒为 {@link HttpCodes#IDEMPOTENT_REPEAT} */
    public int getCode() {
        return code;
    }
}
