package io.github.genkidoudou.web.workflow.util;

import cn.hutool.core.util.StrUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * JSON 反序列化（json-deserialize）节点 data 校验工具。
 */
public final class JsonDeserializeDataUtil {

    private JsonDeserializeDataUtil() {
    }

    /**
     * 校验节点 data，失败时抛出 {@link IllegalArgumentException}。
     *
     * @param nodeId 节点 ID
     * @param data   节点 data
     */
    public static void validate(String nodeId, Map<String, Object> data) {
        if (!hasValidInput(data)) {
            throw new IllegalArgumentException("JSON 反序列化节点 " + nodeId + " 须配置输入参数（参数名与取值）");
        }
        String duplicateKey = findDuplicateOutputFieldKey(data);
        if (duplicateKey != null) {
            throw new IllegalArgumentException("JSON 反序列化节点 " + nodeId + " 输出字段 key 重复: " + duplicateKey);
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

    /**
     * 查找重复的 outputFields.key。
     *
     * @param data 节点 data
     * @return 重复 key，无重复时 null
     */
    @SuppressWarnings("unchecked")
    public static String findDuplicateOutputFieldKey(Map<String, Object> data) {
        if (data == null || !(data.get("outputFields") instanceof List<?> list)) {
            return null;
        }
        Set<String> seen = new HashSet<>();
        for (Object item : list) {
            if (!(item instanceof Map<?, ?> row)) {
                continue;
            }
            String key = row.get("key") == null ? "" : String.valueOf(row.get("key")).trim();
            if (key.isEmpty()) {
                continue;
            }
            if (!seen.add(key)) {
                return key;
            }
        }
        return null;
    }
}
