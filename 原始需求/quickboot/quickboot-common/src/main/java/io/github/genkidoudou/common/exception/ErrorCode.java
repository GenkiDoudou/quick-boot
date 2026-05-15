package io.github.genkidoudou.common.exception;

/**
 * 错误码常量
 * 
 * 错误码规则:
 * - 1xxxx: 通用错误
 * - 2xxxx: 业务错误
 * - 3xxxx: 安全相关错误
 * - 4xxxx: 系统错误
 *
 * @author genkidoudou
 * @since 2026/03/05
 */
public class ErrorCode {

    // ==================== 通用错误 1xxxx ====================
    
    /**
     * 系统内部错误
     */
    public static final Integer INTERNAL_ERROR = 10000;
    
    /**
     * 参数校验失败
     */
    public static final Integer VALIDATION_ERROR = 10001;
    
    /**
     * 资源不存在
     */
    public static final Integer RESOURCE_NOT_FOUND = 10002;
    
    /**
     * 操作失败
     */
    public static final Integer OPERATION_FAILED = 10003;

    // ==================== 业务错误 2xxxx ====================
    
    /**
     * 用户不存在
     */
    public static final Integer USER_NOT_FOUND = 20001;
    
    /**
     * 用户已存在
     */
    public static final Integer USER_ALREADY_EXISTS = 20002;
    
    /**
     * 密码错误
     */
    public static final Integer PASSWORD_INCORRECT = 20003;

    // ==================== 安全相关错误 3xxxx ====================
    
    /**
     * 权限不足
     */
    public static final Integer PERMISSION_DENIED = 30001;
    
    /**
     * 认证失败
     */
    public static final Integer AUTHENTICATION_FAILED = 30002;
    
    /**
     * Token 无效
     */
    public static final Integer TOKEN_INVALID = 30003;
    
    /**
     * Token 过期
     */
    public static final Integer TOKEN_EXPIRED = 30004;

    // ==================== 客户端认证错误 301xx ====================
    
    /**
     * 客户端不存在
     */
    public static final Integer CLIENT_NOT_FOUND = 30101;
    
    /**
     * 客户端已禁用
     */
    public static final Integer CLIENT_DISABLED = 30102;
    
    /**
     * 客户端已过期
     */
    public static final Integer CLIENT_EXPIRED = 30103;
    
    /**
     * 客户端密钥错误
     */
    public static final Integer CLIENT_SECRET_INVALID = 30104;
    
    /**
     * 缺少客户端认证信息
     */
    public static final Integer CLIENT_CREDENTIALS_MISSING = 30105;

    // ==================== 防幂等错误 302xx ====================
    
    /**
     * 重复请求
     */
    public static final Integer IDEMPOTENT_DUPLICATE_REQUEST = 30201;

    // ==================== 请求来源拦截错误 303xx ====================
    
    /**
     * 请求来源不允许
     */
    public static final Integer REFERER_NOT_ALLOWED = 30301;

    // ==================== 请求方式和域名拦截错误 304xx ====================
    
    /**
     * 请求方式不允许
     */
    public static final Integer METHOD_NOT_ALLOWED = 30401;
    
    /**
     * 请求域名不允许
     */
    public static final Integer HOST_NOT_ALLOWED = 30402;

    // ==================== 敏感词过滤错误 305xx ====================
    
    /**
     * 检测到敏感词
     */
    public static final Integer SENSITIVE_WORD_FOUND = 30501;

    // ==================== XSS 脚本注入错误 306xx ====================
    
    /**
     * 检测到 XSS 脚本
     */
    public static final Integer XSS_SCRIPT_DETECTED = 30601;

    // ==================== 验证码错误 307xx ====================

    /**
     * 验证码校验失败
     */
    public static final Integer CAPTCHA_VALIDATION_FAILED = 30701;

    // ==================== 系统错误 4xxxx ====================
    
    /**
     * 数据库错误
     */
    public static final Integer DATABASE_ERROR = 40001;
    
    /**
     * 网络错误
     */
    public static final Integer NETWORK_ERROR = 40002;
    
    /**
     * 文件操作错误
     */
    public static final Integer FILE_ERROR = 40003;
    
    /**
     * 缓存错误
     */
    public static final Integer CACHE_ERROR = 40004;

    private ErrorCode() {
        // 私有构造函数，防止实例化
    }
}
