package io.github.genkidoudou.common.security.firewall.sensitiveword;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import java.io.IOException;

/**
 * 将 {@code application/json} body 解析为树，对字符串节点按策略替换或校验。
 */
final class SensitiveWordJsonBodyProcessor {

    private final ObjectMapper objectMapper;

    SensitiveWordJsonBodyProcessor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    byte[] transform(byte[] rawBody, SensitiveWordEngine engine, SensitiveWordFirewallStrategy strategy)
            throws IOException {
        JsonNode root = objectMapper.readTree(rawBody);
        JsonNode out = transformNode(root, engine, strategy);
        return objectMapper.writeValueAsBytes(out);
    }

    private JsonNode transformNode(JsonNode node, SensitiveWordEngine engine, SensitiveWordFirewallStrategy strategy) {
        if (node == null || node.isNull()) {
            return node;
        }
        if (node.isTextual()) {
            String t = node.asText();
            if (strategy == SensitiveWordFirewallStrategy.REPLACE) {
                return TextNode.valueOf(engine.replace(t));
            }
            engine.assertNotContains(t);
            return node;
        }
        if (node.isObject()) {
            ObjectNode src = (ObjectNode) node;
            ObjectNode copy = objectMapper.createObjectNode();
            src.fields().forEachRemaining(e ->
                    copy.set(e.getKey(), transformNode(e.getValue(), engine, strategy)));
            return copy;
        }
        if (node.isArray()) {
            ArrayNode src = (ArrayNode) node;
            ArrayNode copy = objectMapper.createArrayNode();
            for (JsonNode item : src) {
                copy.add(transformNode(item, engine, strategy));
            }
            return copy;
        }
        return node;
    }
}
