package io.github.genkidoudou.report.api;

import java.util.List;

/**
 * 积木报表与 quickboot 权限/字典桥接（由 app 实现）。
 */
public interface JimuAuthBridge {

    /**
     * 按 token 解析用户并返回角色标识列表。
     *
     * @param token Sa-Token 或积木请求 token
     * @return 角色 key 列表，无效 token 时为空列表
     */
    List<String> listRoleKeysByToken(String token);

    /**
     * 按 token 解析用户并返回权限字符列表。
     *
     * @param token Sa-Token 或积木请求 token
     * @return 权限列表，无效 token 时为空列表
     */
    List<String> listPermissionsByToken(String token);

    /**
     * 按 token 解析登录用户名。
     *
     * @param token Sa-Token 或积木请求 token
     * @return 用户名，无法解析时返回 {@code null}
     */
    String resolveUsername(String token);

    /**
     * 按 token 解析用户主键。
     *
     * @param token Sa-Token 或积木请求 token
     * @return 用户 ID，无效时返回 {@code -1}
     */
    long resolveUserId(String token);

    /**
     * 按字典类型返回 label/value 列表（供 JimuBI 字典接口）。
     */
    List<JimuDictEntry> listDictByType(String dictType);

    record JimuDictEntry(String value, String text) {
    }
}
