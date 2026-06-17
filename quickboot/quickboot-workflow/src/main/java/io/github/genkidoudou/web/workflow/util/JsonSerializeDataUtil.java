package io.github.genkidoudou.web.workflow.util;

import cn.hutool.core.util.StrUtil;

import java.util.List;
import java.util.Map;

/**
 * JSON 序列化（json-serialize）节点 data 校验工具。
 */
public final class JsonSerializeDataUtil {

    private JsonSerializeDataUtil() {
    }

    /**
     * 校验节点 data，失败时抛出 {@link IllegalArgumentException}。
     *
     * @param nodeId 节点 ID
     * @param data   节点 data
     */
    public static void validate(String nodeId, Map<String, Object> data) {
        if (!hasValidInput(data)) {
            throw new IllegalArgumentException("JSON 序列化节点 " + nodeId + " 须配置输入参数（参数名与取值）");
        }
    }

    /**
     * 是否已配置有效输入参数。
     *
     * @param data 节点 data
     * @return 有效时 true
     */
    @SuppressWarnings("unchecked")
    public static boolean hasValidInput(Map<String, Object> data) {
        if (data == null || !(data.get("inputVariables") instanceof List<?> list)) {
            return false;
        }
        for (Object item : list) {
            if (!(item instanceof Map<?, ?> row)) {
                continue;
            }
            String key = row.get("key") == null ? "" : String.valueOf(row.get("key")).trim();
            String value = row.get("value") == null ? "" : String.valueOf(row.get("value")).trim();
            if (StrUtil.isNotBlank(key) && StrUtil.isNotBlank(value)) {
                return true;
            }
        }
        return false;
    }
}
