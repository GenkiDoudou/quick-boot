package io.github.genkidoudou.web.knowledge.mcp.support;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import io.github.genkidoudou.common.security.firewall.password.PasswordCodec;
import io.github.genkidoudou.web.knowledge.constants.McpEnvValueType;
import io.github.genkidoudou.web.knowledge.dto.McpHeaderItemBo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP 远程 HTTP 请求头的 JSON 编解码与密钥标记处理。
 * <p>
 * 存储格式为 JSON 数组：[{@code {"name":"Authorization","valueType":"SECRET","value":"{sm4:...}"}}]。
 */
public final class McpHeaderSupport {

    private static final String FIELD_NAME = "name";
    private static final String FIELD_VALUE = "value";
    private static final String FIELD_VALUE_TYPE = "valueType";

    private McpHeaderSupport() {
    }

    /**
     * 将请求头列表序列化为入库 JSON。
     *
     * @param codec   编解码器
     * @param headers 请求头项
     * @return JSON 字符串；空列表返回 null
     */
    public static String encodeHeaders(PasswordCodec codec, List<McpHeaderItemBo> headers) {
        if (headers == null || headers.isEmpty()) {
            return null;
        }
        JSONArray array = new JSONArray();
        for (McpHeaderItemBo item : headers) {
            if (item == null || StrUtil.isBlank(item.getName())) {
                continue;
            }
            JSONObject obj = new JSONObject();
            obj.set(FIELD_NAME, item.getName().trim());
            String valueType = normalizeValueType(item.getValueType());
            obj.set(FIELD_VALUE_TYPE, valueType);
            obj.set(FIELD_VALUE, encodeValue(codec, valueType, item.getValue()));
            array.add(obj);
        }
        return array.isEmpty() ? null : array.toString();
    }

    /**
     * 从库中 JSON 解析请求头列表。
     *
     * @param headersJson 库中 JSON
     * @param reveal      是否展示密钥明文
     * @return 请求头列表
     */
    public static List<McpHeaderItemBo> decodeHeaders(String headersJson, boolean reveal) {
        if (StrUtil.isBlank(headersJson)) {
            return new ArrayList<>();
        }
        JSONArray array = JSONUtil.parseArray(headersJson);
        List<McpHeaderItemBo> result = new ArrayList<>(array.size());
        for (Object element : array) {
            JSONObject obj = JSONUtil.parseObj(element);
            McpHeaderItemBo item = new McpHeaderItemBo();
            item.setName(obj.getStr(FIELD_NAME));
            String valueType = normalizeValueType(obj.getStr(FIELD_VALUE_TYPE));
            item.setValueType(valueType);
            String stored = obj.getStr(FIELD_VALUE);
            item.setValue(McpSecretSupport.maskForDisplay(valueType, stored, reveal));
            result.add(item);
        }
        return result;
    }

    /**
     * 解析运行时可用的请求头 Map（值已解密/取环境变量）。
     *
     * @param codec       编解码器
     * @param headersJson 库中 JSON
     * @return 请求头键值
     */
    public static Map<String, String> resolveHeaders(PasswordCodec codec, String headersJson) {
        Map<String, String> resolved = new LinkedHashMap<>();
        if (StrUtil.isBlank(headersJson)) {
            return resolved;
        }
        JSONArray array = JSONUtil.parseArray(headersJson);
        for (Object element : array) {
            JSONObject obj = JSONUtil.parseObj(element);
            String name = obj.getStr(FIELD_NAME);
            if (StrUtil.isBlank(name)) {
                continue;
            }
            String valueType = normalizeValueType(obj.getStr(FIELD_VALUE_TYPE));
            String plain = McpSecretSupport.resolvePlainValue(codec, valueType, obj.getStr(FIELD_VALUE));
            if (plain == null) {
                throw new IllegalStateException("环境变量未设置: " + obj.getStr(FIELD_VALUE));
            }
            resolved.put(name.trim(), plain);
        }
        return resolved;
    }

    /**
     * 更新入库时合并请求头：SECRET 留空则保留原值。
     *
     * @param codec        编解码器
     * @param oldHeadersJson 原 JSON
     * @param submitted    提交的请求头
     * @return 新 JSON
     */
    public static String mergeHeadersForUpdate(PasswordCodec codec, String oldHeadersJson, List<McpHeaderItemBo> submitted) {
        if (submitted == null) {
            return oldHeadersJson;
        }
        Map<String, JSONObject> oldMap = new LinkedHashMap<>();
        if (StrUtil.isNotBlank(oldHeadersJson)) {
            JSONArray oldArray = JSONUtil.parseArray(oldHeadersJson);
            for (Object element : oldArray) {
                JSONObject obj = JSONUtil.parseObj(element);
                String name = obj.getStr(FIELD_NAME);
                if (StrUtil.isNotBlank(name)) {
                    oldMap.put(name.trim(), obj);
                }
            }
        }
        for (McpHeaderItemBo item : submitted) {
            if (item == null || StrUtil.isBlank(item.getName())) {
                continue;
            }
            String name = item.getName().trim();
            String valueType = normalizeValueType(item.getValueType());
            JSONObject obj = new JSONObject();
            obj.set(FIELD_NAME, name);
            obj.set(FIELD_VALUE_TYPE, valueType);
            if (McpSecretSupport.isKeepExistingSecret(valueType, item.getValue()) && oldMap.containsKey(name)) {
                obj.set(FIELD_VALUE, oldMap.get(name).getStr(FIELD_VALUE));
            } else {
                obj.set(FIELD_VALUE, encodeValue(codec, valueType, item.getValue()));
            }
            oldMap.put(name, obj);
        }
        if (oldMap.isEmpty()) {
            return null;
        }
        JSONArray array = new JSONArray();
        oldMap.values().forEach(array::add);
        return array.toString();
    }

    private static String encodeValue(PasswordCodec codec, String valueType, String value) {
        if (McpEnvValueType.SECRET.equals(valueType)) {
            return McpSecretSupport.encodeForStorage(codec, value);
        }
        return StrUtil.nullToEmpty(value);
    }

    private static String normalizeValueType(String valueType) {
        if (StrUtil.isBlank(valueType)) {
            return McpEnvValueType.PLAIN;
        }
        return valueType.trim().toUpperCase();
    }
}
