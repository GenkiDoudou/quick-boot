package io.github.genkidoudou.web.workflow.handler;

import io.github.genkidoudou.web.workflow.constants.WfNodeType;
import io.github.genkidoudou.web.workflow.dto.GraphNodeDto;
import io.github.genkidoudou.web.workflow.engine.NodeHandler;
import io.github.genkidoudou.web.workflow.engine.NodeResult;
import io.github.genkidoudou.web.workflow.engine.TemplateRenderer;
import io.github.genkidoudou.web.workflow.engine.WorkflowContext;
import io.github.genkidoudou.web.workflow.support.WorkflowHttpClient;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HTTP Request 节点：带 SSRF 防护的外部 HTTP 调用。
 */
@Component
public class HttpRequestNodeHandler implements NodeHandler {

    private final TemplateRenderer templateRenderer;
    private final WorkflowHttpClient httpClient;

    public HttpRequestNodeHandler(TemplateRenderer templateRenderer, WorkflowHttpClient httpClient) {
        this.templateRenderer = templateRenderer;
        this.httpClient = httpClient;
    }

    @Override
    public String type() {
        return WfNodeType.HTTP_REQUEST;
    }

    @Override
    @SuppressWarnings("unchecked")
    public NodeResult execute(GraphNodeDto node, WorkflowContext context) {
        Map<String, Object> data = node.getData() == null ? Map.of() : node.getData();
        String method = data.get("method") == null ? "GET" : String.valueOf(data.get("method"));
        String url = templateRenderer.render(String.valueOf(data.getOrDefault("url", "")), context);
        String body = data.get("body") == null ? null
            : templateRenderer.render(String.valueOf(data.get("body")), context);
        Map<String, String> headers = new HashMap<>();
        if (data.get("headers") instanceof List<?> headerList) {
            for (Object item : headerList) {
                if (item instanceof Map<?, ?> h) {
                    Object k = h.get("key");
                    Object v = h.get("value");
                    if (k != null && v != null) {
                        headers.put(String.valueOf(k),
                            templateRenderer.render(String.valueOf(v), context));
                    }
                }
            }
        }
        try {
            Map<String, Object> response = httpClient.execute(method, url, headers, body);
            return NodeResult.success(response);
        } catch (Exception ex) {
            return NodeResult.failed(ex.getMessage());
        }
    }
}
