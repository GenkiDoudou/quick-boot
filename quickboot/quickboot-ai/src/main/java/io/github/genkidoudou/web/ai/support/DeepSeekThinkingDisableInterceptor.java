package io.github.genkidoudou.web.ai.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * DeepSeek V4 默认开启思考模式，但 Spring AI 1.0 尚不支持 {@code reasoning_content} 多轮回传，
 * 会导致模型在正文中输出 {@code <function_calls>} 伪 XML 而非 API {@code tool_calls}。
 * <p>
 * 本拦截器仅在请求体包含非空 {@code tools} 时注入 {@code thinking.type=disabled}，
 * 不影响普通无工具对话的思考模式。
 */
public class DeepSeekThinkingDisableInterceptor implements ClientHttpRequestInterceptor {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
        throws IOException {
        if (body == null || body.length == 0 || !shouldDisableThinking(body)) {
            return execution.execute(request, body);
        }
        ObjectNode root = (ObjectNode) MAPPER.readTree(body);
        ObjectNode thinking = MAPPER.createObjectNode();
        thinking.put("type", "disabled");
        root.set("thinking", thinking);
        return execution.execute(request, MAPPER.writeValueAsBytes(root));
    }

    private boolean shouldDisableThinking(byte[] body) {
        try {
            JsonNode root = MAPPER.readTree(body);
            JsonNode tools = root.get("tools");
            return tools != null && tools.isArray() && !tools.isEmpty();
        } catch (IOException ex) {
            return false;
        }
    }
}
