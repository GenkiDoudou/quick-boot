package io.github.genkidoudou.common.exception;

import io.github.genkidoudou.common.api.HttpCodes;

/**
 * 异常体系业务码常量。
 * <p>
 * 分段规则：1xxxx（通用）/ 2xxxx（业务）/ 3xxxx（安全）/ 4xxxx（系统）。
 * 其中部分安全码直接复用既有 {@link HttpCodes} 以避免冲突与双份定义。
 */
public final class ErrorCodes {

    private ErrorCodes() {
    }

    /** 通用错误码。 */
    public static final class Common {
        public static final int INVALID_PARAM = 10001;
        public static final int REQUEST_BODY_INVALID = 10002;

        private Common() {
        }
    }

    /** 业务错误码。 */
    public static final class Biz {
        public static final int STATE_NOT_ALLOWED = 20001;
        public static final int IDEMPOTENT_REPEAT = HttpCodes.IDEMPOTENT_REPEAT;

        private Biz() {
        }
    }

    /** 安全错误码。 */
    public static final class Security {
        public static final int UNAUTHORIZED = HttpCodes.UNAUTHORIZED;
        public static final int FORBIDDEN = HttpCodes.FORBIDDEN;
        public static final int RATE_LIMITED = 30001;
        public static final int HOST_NOT_ALLOWED = HttpCodes.HOST_NOT_ALLOWED;
        public static final int SENSITIVE_WORD = HttpCodes.SENSITIVE_WORD;
        public static final int SQL_INJECTION_DETECTED = HttpCodes.SQL_INJECTION_DETECTED;
        public static final int XSS_SCRIPT_DETECTED = HttpCodes.XSS_SCRIPT_DETECTED;

        private Security() {
        }
    }

    /** 系统错误码。 */
    public static final class System {
        public static final int INTERNAL_ERROR = 40000;
        public static final int DEPENDENCY_UNAVAILABLE = 40001;

        private System() {
        }
    }
}
