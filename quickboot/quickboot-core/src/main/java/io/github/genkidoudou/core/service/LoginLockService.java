package io.github.genkidoudou.core.service;


/**
 * 登录失败锁定：基于 {@link org.springframework.cache.CacheManager}，不直连 Redis API。
 */
public interface LoginLockService {

    /**
     * 规范化用户名（与 {@link AuthLoginService#authenticate} 使用的登录名规则一致）。
     *
     * @param userName 原始登录名
     * @return trim 后非空串；若入参为空则返回空串
     */
    String normalizeUserName(String userName);

    /**
     * 若账号处于锁定期内则抛出业务异常。
     *
     * @param userName 规范化后的登录名
     */
    void assertNotLocked(String userName);

    /**
     * 记录一次密码类失败（仅应在未授权/凭据错误场景调用，与全局 {@code UNAUTHORIZED} 语义一致）。
     *
     * @param userName 规范化后的登录名
     */
    void recordFailure(String userName);

    /**
     * 登录成功后清除该用户失败状态。
     *
     * @param userName 规范化后的登录名
     */
    void onLoginSuccess(String userName);

    /**
     * 管理员解锁：清除缓存条目（幂等）。
     *
     * @param userName 规范化后的登录名
     */
    void clearForUserName(String userName);
}
