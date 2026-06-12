package io.github.genkidoudou.web.workflow.handler;

import io.github.genkidoudou.web.workflow.constants.WfNodeType;
import io.github.genkidoudou.web.workflow.dto.GraphNodeDto;
import io.github.genkidoudou.web.workflow.engine.NodeHandler;
import io.github.genkidoudou.web.workflow.engine.NodeResult;
import io.github.genkidoudou.web.workflow.engine.TemplateRenderer;
import io.github.genkidoudou.web.workflow.engine.WorkflowContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Template Transform 节点：模板字符串拼接，输出 result 字段。
 */
@Component
public class TemplateTransformNodeHandler implements NodeHandler {

    private final TemplateRenderer templateRenderer;

    public TemplateTransformNodeHandler(TemplateRenderer templateRenderer) {
        this.templateRenderer = templateRenderer;
    }

    @Override
    public String type() {
        return WfNodeType.TEMPLATE_TRANSFORM;
    }

    @Override
    public NodeResult execute(GraphNodeDto node, WorkflowContext context) {
        Map<String, Object> data = node.getData() == null ? Map.of() : node.getData();
        String template = data.get("template") == null ? "" : String.valueOf(data.get("template"));
        String result = templateRenderer.render(template, context);
        Map<String, Object> outputs = new HashMap<>();
        outputs.put("result", result);
        return NodeResult.success(outputs);
    }
}
