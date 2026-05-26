package io.github.genkidoudou.report.bridge;

import java.util.List;

/**
 * 积木报表与 quickboot 权限/字典桥接（由 web 模块实现）。
 */
public interface JimuAuthBridge {

    List<String> listRoleKeysByToken(String token);

    List<String> listPermissionsByToken(String token);

    String resolveUsername(String token);

    long resolveUserId(String token);

    /**
     * 按字典类型返回 label/value 列表（供 JimuBI 字典桥接）。
     */
    List<JimuDictEntry> listDictByType(String dictType);

    record JimuDictEntry(String value, String text) {
    }
}
