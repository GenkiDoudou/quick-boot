package io.github.genkidoudou.web.workflow.handler;

import io.github.genkidoudou.web.workflow.constants.WfNodeType;
import io.github.genkidoudou.web.workflow.dto.GraphNodeDto;
import io.github.genkidoudou.web.workflow.engine.NodeHandler;
import io.github.genkidoudou.web.workflow.engine.NodeResult;
import io.github.genkidoudou.web.workflow.engine.TemplateRenderer;
import io.github.genkidoudou.web.workflow.engine.WorkflowContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * List Operator 节点：filter / first / last / map-field 列表操作。
 */
@Component
public class ListOperatorNodeHandler implements NodeHandler {

    private final TemplateRenderer templateRenderer;

    public ListOperatorNodeHandler(TemplateRenderer templateRenderer) {
        this.templateRenderer = templateRenderer;
    }

    @Override
    public String type() {
        return WfNodeType.LIST_OPERATOR;
    }

    @Override
    @SuppressWarnings("unchecked")
    public NodeResult execute(GraphNodeDto node, WorkflowContext context) {
        Map<String, Object> data = node.getData() == null ? Map.of() : node.getData();
        String operation = data.get("operation") == null ? "first" : String.valueOf(data.get("operation"));
        Object listObj = templateRenderer.resolveObject(String.valueOf(data.getOrDefault("list", "")), context);
        List<Object> items = toList(listObj);
        Map<String, Object> outputs = new HashMap<>();
        switch (operation) {
            case "filter" -> {
                String field = data.get("filterField") == null ? null : String.valueOf(data.get("filterField"));
                String expected = templateRenderer.render(String.valueOf(data.getOrDefault("filterValue", "")), context);
                List<Object> filtered = new ArrayList<>();
                for (Object item : items) {
                    if (item instanceof Map<?, ?> map && field != null) {
                        Object val = map.get(field);
                        if (expected.equals(String.valueOf(val))) {
                            filtered.add(item);
                        }
                    }
                }
                outputs.put("items", filtered);
                outputs.put("count", filtered.size());
                outputs.put("first", filtered.isEmpty() ? null : filtered.get(0));
            }
            case "last" -> {
                outputs.put("items", items);
                outputs.put("count", items.size());
                outputs.put("first", items.isEmpty() ? null : items.get(items.size() - 1));
            }
            case "map-field" -> {
                String field = data.get("mapField") == null ? null : String.valueOf(data.get("mapField"));
                List<Object> mapped = new ArrayList<>();
                for (Object item : items) {
                    if (item instanceof Map<?, ?> map && field != null) {
                        mapped.add(map.get(field));
                    }
                }
                outputs.put("items", mapped);
                outputs.put("count", mapped.size());
                outputs.put("first", mapped.isEmpty() ? null : mapped.get(0));
            }
            default -> {
                outputs.put("items", items);
                outputs.put("count", items.size());
                outputs.put("first", items.isEmpty() ? null : items.get(0));
            }
        }
        return NodeResult.success(outputs);
    }

    @SuppressWarnings("unchecked")
    private List<Object> toList(Object listObj) {
        if (listObj instanceof List<?> list) {
            return new ArrayList<>(list);
        }
        return new ArrayList<>();
    }
}
