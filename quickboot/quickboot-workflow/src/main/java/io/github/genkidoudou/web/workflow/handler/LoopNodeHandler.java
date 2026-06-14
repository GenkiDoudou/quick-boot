package io.github.genkidoudou.web.workflow.handler;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import io.github.genkidoudou.web.workflow.constants.WfNodeType;
import io.github.genkidoudou.web.workflow.dto.GraphNodeDto;
import io.github.genkidoudou.web.workflow.dto.WorkflowGraphDto;
import io.github.genkidoudou.web.workflow.engine.LoopExecutionScope;
import io.github.genkidoudou.web.workflow.engine.LoopIterationResult;
import io.github.genkidoudou.web.workflow.engine.LoopSubgraphExecutor;
import io.github.genkidoudou.web.workflow.engine.NodeHandler;
import io.github.genkidoudou.web.workflow.engine.NodeResult;
import io.github.genkidoudou.web.workflow.engine.TemplateRenderer;
import io.github.genkidoudou.web.workflow.engine.WorkflowContext;
import io.github.genkidoudou.web.workflow.stream.WorkflowStreamEmitter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 循环节点：数组循环 / 指定次数 / 无限循环，支持中间变量、终止与继续循环。
 */
@Component
public class LoopNodeHandler implements NodeHandler {

    private static final int MIN_COUNT = 1;
    private static final int MAX_COUNT = 1000;
    private static final int DEFAULT_COUNT = 10;
    private static final int INFINITE_MAX_SAFETY = 1000;
    private static final String DEFAULT_ITEM_KEY = "item";
    private static final String DEFAULT_OUTPUT_KEY = "results";

    private final TemplateRenderer templateRenderer;
    private final LoopSubgraphExecutor loopSubgraphExecutor;
    private final WorkflowStreamEmitter streamEmitter;

    public LoopNodeHandler(TemplateRenderer templateRenderer,
                           LoopSubgraphExecutor loopSubgraphExecutor,
                           WorkflowStreamEmitter streamEmitter) {
        this.templateRenderer = templateRenderer;
        this.loopSubgraphExecutor = loopSubgraphExecutor;
        this.streamEmitter = streamEmitter;
    }

    @Override
    public String type() {
        return WfNodeType.LOOP;
    }

    @Override
    @SuppressWarnings("unchecked")
    public NodeResult execute(GraphNodeDto node, WorkflowContext context) {
        WorkflowGraphDto graph = context.getExecutionGraph();
        if (graph == null) {
            return NodeResult.failed("循环节点缺少执行图上下文");
        }

        Map<String, Object> data = node.getData() == null ? Map.of() : node.getData();
        String loopType = data.get("loopType") == null ? "count" : String.valueOf(data.get("loopType"));
        String bodyId = data.get("bodyId") == null ? null : String.valueOf(data.get("bodyId")).trim();
        if (StrUtil.isBlank(bodyId)) {
            return NodeResult.failed("循环节点未配置循环体");
        }

        List<String> intermediateKeys = new ArrayList<>();
        Map<String, String> intermediateTypes = new HashMap<>();
        Map<String, Object> initialIntermediates = new HashMap<>();
        parseIntermediateConfig(data.get("intermediateVariables"), context, intermediateKeys,
            intermediateTypes, initialIntermediates);

        String outputMode = data.get("outputMode") == null ? "results" : String.valueOf(data.get("outputMode"));
        String outputNodeId = data.get("outputNodeId") == null ? null : String.valueOf(data.get("outputNodeId")).trim();
        String outputField = data.get("outputField") == null ? "text" : String.valueOf(data.get("outputField")).trim();
        String outputVariableKey = data.get("outputVariableKey") == null ? null
            : String.valueOf(data.get("outputVariableKey")).trim();
        String outputVariableName = resolveOutputVariableName(data);

        List<Object> results = new ArrayList<>();
        LoopExecutionScope scope = new LoopExecutionScope(node.getId(), bodyId);
        scope.getIntermediateKeys().addAll(intermediateKeys);
        scope.getIntermediateTypes().putAll(intermediateTypes);
        scope.getIntermediateVars().putAll(initialIntermediates);
        LoopExecutionScope previousScope = context.getCurrentLoopScope();
        context.setCurrentLoopScope(scope);

        int executedIterations = 0;
        try {
            if ("infinite".equals(loopType)) {
                scope.setItemVariableKey(DEFAULT_ITEM_KEY);
                executedIterations = runInfiniteLoop(graph, bodyId, context, scope, results, outputNodeId, outputField);
            } else if ("array".equals(loopType)) {
                ArrayIterationPlan plan = resolveArrayIterationPlan(data, context);
                scope.setItemVariableKey(plan.itemVariableKey());
                if (plan.iterations().isEmpty()) {
                    return NodeResult.success(buildOutputs(scope, results, 0, outputMode, outputVariableKey,
                        outputVariableName, data));
                }
                executedIterations = runArrayLoop(graph, bodyId, context, scope, plan, results, outputNodeId, outputField);
            } else {
                scope.setItemVariableKey(DEFAULT_ITEM_KEY);
                List<Object> iterations = resolveCountIterations(data, context);
                executedIterations = runFixedLoop(graph, bodyId, context, scope, iterations, results,
                    outputNodeId, outputField);
            }
        } finally {
            context.setCurrentLoopScope(previousScope);
        }

        return NodeResult.success(buildOutputs(scope, results, executedIterations, outputMode, outputVariableKey,
            outputVariableName, data));
    }

    private int runArrayLoop(WorkflowGraphDto graph, String bodyId, WorkflowContext context, LoopExecutionScope scope,
                             ArrayIterationPlan plan, List<Object> results, String outputNodeId, String outputField) {
        List<Object> iterations = plan.iterations();
        for (int i = 0; i < iterations.size(); i++) {
            Object item = iterations.get(i);
            scope.beginIteration(i, item);
            for (Map.Entry<String, List<Object>> entry : plan.arrayValues().entrySet()) {
                List<Object> list = entry.getValue();
                if (i < list.size()) {
                    scope.getArrayItems().put(entry.getKey(), list.get(i));
                }
            }
            scope.syncToContext(context);
            emitLoopIterationStart(context, scope);

            LoopIterationResult outcome = loopSubgraphExecutor.executeIteration(graph, bodyId, context);
            scope.finishIteration();
            emitLoopIterationEnd(context, scope, results, outputNodeId, outputField);

            if (outcome == LoopIterationResult.BREAK) {
                return i + 1;
            }
            if (outcome == LoopIterationResult.CONTINUE) {
                continue;
            }
            collectIterationResult(context, results, outputNodeId, outputField);
        }
        return iterations.size();
    }

    private int runFixedLoop(WorkflowGraphDto graph, String bodyId, WorkflowContext context, LoopExecutionScope scope,
                             List<Object> iterations, List<Object> results, String outputNodeId, String outputField) {
        for (int i = 0; i < iterations.size(); i++) {
            scope.beginIteration(i, iterations.get(i));
            scope.syncToContext(context);
            emitLoopIterationStart(context, scope);

            LoopIterationResult outcome = loopSubgraphExecutor.executeIteration(graph, bodyId, context);
            scope.finishIteration();
            emitLoopIterationEnd(context, scope, results, outputNodeId, outputField);

            if (outcome == LoopIterationResult.BREAK) {
                return i + 1;
            }
            if (outcome == LoopIterationResult.CONTINUE) {
                continue;
            }
            collectIterationResult(context, results, outputNodeId, outputField);
        }
        return iterations.size();
    }

    private int runInfiniteLoop(WorkflowGraphDto graph, String bodyId, WorkflowContext context, LoopExecutionScope scope,
                                List<Object> results, String outputNodeId, String outputField) {
        for (int i = 0; i < INFINITE_MAX_SAFETY; i++) {
            scope.beginIteration(i, i);
            scope.syncToContext(context);
            emitLoopIterationStart(context, scope);

            LoopIterationResult outcome = loopSubgraphExecutor.executeIteration(graph, bodyId, context);
            scope.finishIteration();
            emitLoopIterationEnd(context, scope, results, outputNodeId, outputField);

            if (outcome == LoopIterationResult.BREAK) {
                return i + 1;
            }
            if (outcome == LoopIterationResult.CONTINUE) {
                continue;
            }
            collectIterationResult(context, results, outputNodeId, outputField);
        }
        return INFINITE_MAX_SAFETY;
    }

    private void emitLoopIterationStart(WorkflowContext context, LoopExecutionScope scope) {
        if (context.getRunId() == null || !context.isStreamEnabled()) {
            return;
        }
        streamEmitter.emitLoopIteration(context.getRunId(), scope.getLoopNodeId(), scope.getCurrentIndex(), "start",
            Map.of("item", scope.getCurrentItem(), "index", scope.getCurrentIndex()));
    }

    private void emitLoopIterationEnd(WorkflowContext context, LoopExecutionScope scope, List<Object> results,
                                      String outputNodeId, String outputField) {
        if (context.getRunId() == null || !context.isStreamEnabled()) {
            return;
        }
        Object roundResult = null;
        if (StrUtil.isNotBlank(outputNodeId)) {
            roundResult = context.getNodeOutputMap(outputNodeId).get(outputField);
        }
        streamEmitter.emitLoopIteration(context.getRunId(), scope.getLoopNodeId(), scope.getCurrentIndex(), "end",
            Map.of("item", scope.getCurrentItem(), "index", scope.getCurrentIndex(), "roundResult", roundResult == null ? "" : roundResult));
    }

    private void collectIterationResult(WorkflowContext context, List<Object> results,
                                        String outputNodeId, String outputField) {
        if (StrUtil.isBlank(outputNodeId)) {
            return;
        }
        Map<String, Object> nodeOut = context.getNodeOutputMap(outputNodeId);
        if (nodeOut == null || nodeOut.isEmpty()) {
            return;
        }
        if (StrUtil.isNotBlank(outputField) && nodeOut.containsKey(outputField)) {
            Object val = nodeOut.get(outputField);
            if (val != null && !(val instanceof String str && str.isEmpty())) {
                results.add(val);
                return;
            }
        }
        Map<String, Object> round = extractCollectibleOutputs(nodeOut);
        if (!round.isEmpty()) {
            results.add(round);
        }
    }

    /** 提取 answer/业务节点上用户定义的输出变量（排除 text/citations 等模板字段）。 */
    private Map<String, Object> extractCollectibleOutputs(Map<String, Object> nodeOut) {
        Map<String, Object> round = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : nodeOut.entrySet()) {
            String key = entry.getKey();
            if ("text".equals(key) || "citations".equals(key)) {
                continue;
            }
            round.put(key, entry.getValue());
        }
        return round;
    }

    private Map<String, Object> buildOutputs(LoopExecutionScope scope, List<Object> results, int executedIterations,
                                             String outputMode, String outputVariableKey, String outputVariableName,
                                             Map<String, Object> data) {
        Map<String, Object> outputs = new HashMap<>();
        outputs.put(outputVariableName, results);
        outputs.put("count", executedIterations);
        outputs.put("item", scope.getCurrentItem());
        outputs.put("index", executedIterations > 0 ? scope.getCurrentIndex() : -1);
        outputs.put("iterationTraces", new ArrayList<>(scope.getIterationTraces()));
        outputs.putAll(scope.getIntermediateVars());
        if ("variable".equals(outputMode) && StrUtil.isNotBlank(outputVariableKey)) {
            Object varValue = scope.getIntermediateVars().get(outputVariableKey);
            outputs.put("outputVariable", varValue);
            outputs.put(outputVariableKey, varValue);
        }
        return outputs;
    }

    private String resolveOutputVariableName(Map<String, Object> data) {
        Object name = data.get("outputVariableName");
        if (name != null && StrUtil.isNotBlank(String.valueOf(name))) {
            return String.valueOf(name).trim();
        }
        return DEFAULT_OUTPUT_KEY;
    }

    @SuppressWarnings("unchecked")
    private ArrayIterationPlan resolveArrayIterationPlan(Map<String, Object> data, WorkflowContext context) {
        List<Map<String, Object>> params = parseArrayParameters(data);
        if (params.isEmpty()) {
            String arraySource = data.get("arraySource") == null ? "" : String.valueOf(data.get("arraySource"));
            if (StrUtil.isNotBlank(arraySource)) {
                params = List.of(Map.of("key", DEFAULT_ITEM_KEY, "source", arraySource));
            }
        }
        Map<String, List<Object>> arrayValues = new LinkedHashMap<>();
        String itemKey = DEFAULT_ITEM_KEY;
        for (int i = 0; i < params.size(); i++) {
            Map<String, Object> param = params.get(i);
            String key = param.get("key") == null ? "" : String.valueOf(param.get("key")).trim();
            String source = param.get("source") == null ? "" : String.valueOf(param.get("source")).trim();
            if (StrUtil.isBlank(key) || StrUtil.isBlank(source)) {
                continue;
            }
            if (i == 0) {
                itemKey = key;
            }
            Object resolved = templateRenderer.resolveObject(source, context);
            arrayValues.put(key, toList(resolved));
        }
        int length = shortestArrayLength(arrayValues);
        List<Object> iterations = new ArrayList<>();
        List<Object> primary = arrayValues.get(itemKey);
        if (primary != null && length > 0) {
            for (int i = 0; i < length; i++) {
                iterations.add(i < primary.size() ? primary.get(i) : null);
            }
        }
        return new ArrayIterationPlan(itemKey, arrayValues, iterations);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseArrayParameters(Map<String, Object> data) {
        Object raw = data.get("arrayParameters");
        if (!(raw instanceof List<?> list)) {
            return List.of();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map<?, ?> map) {
                result.add((Map<String, Object>) map);
            }
        }
        return result;
    }

    private List<Object> toList(Object value) {
        if (value instanceof List<?> list) {
            return new ArrayList<>(list);
        }
        if (value instanceof String str && StrUtil.isNotBlank(str) && JSONUtil.isTypeJSONArray(str.trim())) {
            return new ArrayList<>(JSONUtil.parseArray(str.trim()));
        }
        return new ArrayList<>();
    }

    private int shortestArrayLength(Map<String, List<Object>> arrays) {
        if (arrays.isEmpty()) {
            return 0;
        }
        int min = Integer.MAX_VALUE;
        for (List<Object> list : arrays.values()) {
            min = Math.min(min, list.size());
        }
        return min == Integer.MAX_VALUE ? 0 : min;
    }

    @SuppressWarnings("unchecked")
    private List<Object> resolveCountIterations(Map<String, Object> data, WorkflowContext context) {
        int count = resolveCount(data, context);
        List<Object> iterations = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            iterations.add(i);
        }
        return iterations;
    }

    private int resolveCount(Map<String, Object> data, WorkflowContext context) {
        String countSource = data.get("countSource") == null ? null : String.valueOf(data.get("countSource")).trim();
        if (StrUtil.isNotBlank(countSource)) {
            Object resolved = templateRenderer.resolveObject(countSource, context);
            if (resolved instanceof Number num) {
                return clampCount(num.intValue());
            }
            if (resolved != null) {
                try {
                    return clampCount(Integer.parseInt(String.valueOf(resolved).trim()));
                } catch (NumberFormatException ignored) {
                    // fall through
                }
            }
        }
        int count = DEFAULT_COUNT;
        Object countObj = data.get("count");
        if (countObj instanceof Number num) {
            count = num.intValue();
        } else if (countObj != null && StrUtil.isNotBlank(String.valueOf(countObj))) {
            try {
                count = Integer.parseInt(String.valueOf(countObj).trim());
            } catch (NumberFormatException ignored) {
                count = DEFAULT_COUNT;
            }
        }
        return clampCount(count);
    }

    private int clampCount(int count) {
        return Math.max(MIN_COUNT, Math.min(MAX_COUNT, count));
    }

    @SuppressWarnings("unchecked")
    private void parseIntermediateConfig(Object raw, WorkflowContext context,
                                         List<String> keys, Map<String, String> types, Map<String, Object> initials) {
        if (!(raw instanceof List<?> list)) {
            return;
        }
        for (Object item : list) {
            if (!(item instanceof Map<?, ?> map)) {
                continue;
            }
            Object key = map.get("key");
            if (key == null || StrUtil.isBlank(String.valueOf(key))) {
                continue;
            }
            String keyStr = String.valueOf(key).trim();
            keys.add(keyStr);
            Object typeObj = map.get("type");
            types.put(keyStr, typeObj == null ? "any" : String.valueOf(typeObj).trim());
            Object initialValue = map.get("initialValue");
            String rendered = initialValue == null ? ""
                : templateRenderer.render(String.valueOf(initialValue), context);
            initials.put(keyStr, rendered);
        }
    }

    private record ArrayIterationPlan(String itemVariableKey, Map<String, List<Object>> arrayValues,
                                      List<Object> iterations) {
    }
}
