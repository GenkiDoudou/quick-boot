package io.github.genkidoudou.common.api;

/**
 * 与 HTTP 语义对齐的<strong>业务响应码</strong>常量，用于 {@link R} 的 {@code code} 字段。
 * <p>
 * 约定：对外 API 的 HTTP 状态码保持 200，由客户端依据本处整型码判断业务成败（见项目 AGENTS / OpenSpec {@code common-response-paging}）。
 */
public final class HttpCodes {

    /** 成功，与 {@link R#isSuccess()} 判定一致。 */
    public static final int OK = 200;

    public static final int BAD_REQUEST = 400;
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;
    public static final int NOT_FOUND = 404;
    public static final int INTERNAL_ERROR = 500;
    public static final int SERVICE_UNAVAILABLE = 503;

    /**
     * 幂等：窗口内重复请求（与 {@link io.github.genkidoudou.common.security.firewall.idempotent.IdempotentException} 一致）。
     */
    public static final int IDEMPOTENT_REPEAT = 30201;

    /**
     * 防火墙：请求方式不允许（Method 白名单拦截）。
     */
    public static final int METHOD_NOT_ALLOWED = 30401;

    /**
     * 防火墙：Host 不允许（Host 白名单拦截）。
     */
    public static final int HOST_NOT_ALLOWED = 30402;

    /**
     * 敏感词命中（与 {@link io.github.genkidoudou.common.security.firewall.sensitiveword.SensitiveWordException} 一致）。
     */
    public static final int SENSITIVE_WORD = 30501;

    /**
     * 防火墙：请求参数或 JSON 中命中 SQL 注入启发式关键字（子串级检测，与 {@code qc.security.firewall.sql-injection} 一致）。
     */
    public static final int SQL_INJECTION_DETECTED = 30601;

    /**
     * 防火墙：请求参数或 JSON / multipart 文本字段中命中 XSS 启发式规则（与 {@code qc.security.firewall.xss} 一致）。
     */
    public static final int XSS_SCRIPT_DETECTED = 30701;

    private HttpCodes() {
    }
}
