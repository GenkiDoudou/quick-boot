package io.github.genkidoudou.web.workflow.handler;

import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.web.workflow.constants.WfNodeType;
import io.github.genkidoudou.web.workflow.dto.GraphNodeDto;
import io.github.genkidoudou.web.workflow.dto.WorkflowGraphDto;
import io.github.genkidoudou.web.workflow.engine.BatchExecutionScope;
import io.github.genkidoudou.web.workflow.engine.LoopSubgraphExecutor;
import io.github.genkidoudou.web.workflow.engine.NodeHandler;
import io.github.genkidoudou.web.workflow.engine.NodeResult;
import io.github.genkidoudou.web.workflow.engine.TemplateRenderer;
import io.github.genkidoudou.web.workflow.engine.WorkflowContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 批处理节点：按数组元素分批并行执行批处理体，并汇总输出为数组。
 */
@Component
public class BatchNodeHandler implements NodeHandler {

    private static final int DEFAULT_PARALLEL = 10;
    private static final int MAX_PARALLEL = 10;
    private static final int MIN_PARALLEL = 1;
    private static final int DEFAULT_MAX_RUNS = 100;
    private static final int MAX_MAX_RUNS = 200;
    private static final int MIN_MAX_RUNS = 1;

    private final TemplateRenderer templateRenderer;
    private final LoopSubgraphExecutor loopSubgraphExecutor;

    public BatchNodeHandler(TemplateRenderer templateRenderer, LoopSubgraphExecutor loopSubgraphExecutor) {
        this.templateRenderer = templateRenderer;
        this.loopSubgraphExecutor = loopSubgraphExecutor;
    }

    @Override
    public String type() {
        return WfNodeType.BATCH;
    }

    @Override
    @SuppressWarnings("unchecked")
    public NodeResult execute(GraphNodeDto node, WorkflowContext context) {
        WorkflowGraphDto graph = context.getExecutionGraph();
        if (graph == null) {
            return NodeResult.failed("批处理节点缺少执行图上下文");
        }

        Map<String, Object> data = node.getData() == null ? Map.of() : node.getData();
        String bodyId = data.get("bodyId") == null ? null : String.valueOf(data.get("bodyId")).trim();
        if (StrUtil.isBlank(bodyId)) {
            return NodeResult.failed("批处理节点未配置批处理体");
        }

        List<Map<String, Object>> inputParams = parseInputParameters(data.get("inputParameters"));
        if (inputParams.isEmpty()) {
            return NodeResult.failed("批处理节点至少需要一个输入参数");
        }

        Map<String, List<Object>> inputArrays = resolveInputArrays(inputParams, context);
        int arrayLength = shortestArrayLength(inputArrays);
        if (arrayLength <= 0) {
            return NodeResult.success(buildEmptyOutputs(data));
        }

        int maxRuns = resolveMaxRuns(data);
        int runCount = Math.min(arrayLength, maxRuns);
        int parallelLimit = resolveParallelLimit(data, context);

        List<Map<String, Object>> outputParams = parseOutputParameters(data.get("outputParameters"));
        Map<String, List<Object>> aggregated = new LinkedHashMap<>();
        for (Map<String, Object> outputParam : outputParams) {
            String key = String.valueOf(outputParam.get("key")).trim();
            if (StrUtil.isNotBlank(key)) {
                aggregated.put(key, new ArrayList<>());
            }
        }

        for (int chunkStart = 0; chunkStart < runCount; chunkStart += parallelLimit) {
            int chunkEnd = Math.min(chunkStart + parallelLimit, runCount);
            int chunkSize = chunkEnd - chunkStart;
            ExecutorService executor = Executors.newFixedThreadPool(Math.max(1, chunkSize));
            try {
                List<CompletableFuture<Map<String, Object>>> futures = new ArrayList<>(chunkSize);
                for (int index = chunkStart; index < chunkEnd; index++) {
                    final int idx = index;
                    futures.add(CompletableFuture.supplyAsync(
                        () -> runSingleIteration(graph, node.getId(), bodyId, idx, inputArrays, outputParams, context),
                        executor));
                }
                for (CompletableFuture<Map<String, Object>> future : futures) {
                    Map<String, Object> itemOutputs = future.join();
                    for (Map.Entry<String, List<Object>> entry : aggregated.entrySet()) {
                        entry.getValue().add(itemOutputs.get(entry.getKey()));
                    }
                }
            } finally {
                executor.shutdown();
            }
        }

        Map<String, Object> outputs = new HashMap<>();
        outputs.putAll(aggregated);
        outputs.put("count", runCount);
        outputs.put("index", runCount > 0 ? runCount - 1 : -1);
        return NodeResult.success(outputs);
    }

    private Map<String, Object> runSingleIteration(WorkflowGraphDto graph, String batchNodeId, String bodyId,
                                                     int index, Map<String, List<Object>> inputArrays,
                                                     List<Map<String, Object>> outputParams,
                                                     WorkflowContext parentContext) {
        WorkflowContext iterContext = parentContext.forkSnapshot();
        BatchExecutionScope scope = new BatchExecutionScope(batchNodeId, bodyId, index);
        for (Map.Entry<String, List<Object>> entry : inputArrays.entrySet()) {
            List<Object> list = entry.getValue();
            if (index < list.size()) {
                scope.getInputItems().put(entry.getKey(), list.get(index));
            }
        }
        iterContext.setCurrentBatchScope(scope);
        scope.syncToContext(iterContext);
        loopSubgraphExecutor.executeIteration(graph, bodyId, iterContext);

        Map<String, Object> itemResult = new HashMap<>();
        for (Map<String, Object> outputParam : outputParams) {
            String key = outputParam.get("key") == null ? "" : String.valueOf(outputParam.get("key")).trim();
            String nodeId = outputParam.get("nodeId") == null ? "" : String.valueOf(outputParam.get("nodeId")).trim();
            String field = outputParam.get("field") == null ? "output" : String.valueOf(outputParam.get("field")).trim();
            if (StrUtil.isBlank(key) || StrUtil.isBlank(nodeId)) {
                continue;
            }
            itemResult.put(key, iterContext.getNodeOutputMap(nodeId).get(field));
        }
        return itemResult;
    }

    @SuppressWarnings("unchecked")
    private Map<String, List<Object>> resolveInputArrays(List<Map<String, Object>> inputParams, WorkflowContext context) {
        Map<String, List<Object>> arrays = new LinkedHashMap<>();
        for (Map<String, Object> param : inputParams) {
            String key = param.get("key") == null ? "" : String.valueOf(param.get("key")).trim();
            String source = param.get("source") == null ? "" : String.valueOf(param.get("source")).trim();
            if (StrUtil.isBlank(key) || StrUtil.isBlank(source)) {
                continue;
            }
            Object resolved = templateRenderer.resolveObject(source, context);
            arrays.put(key, toList(resolved));
        }
        return arrays;
    }

    @SuppressWarnings("unchecked")
    private List<Object> toList(Object value) {
        if (value instanceof List<?> list) {
            return new ArrayList<>(list);
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

    private int resolveParallelLimit(Map<String, Object> data, WorkflowContext context) {
        String source = data.get("parallelLimitSource") == null ? null
            : String.valueOf(data.get("parallelLimitSource")).trim();
        if (StrUtil.isNotBlank(source)) {
            Object resolved = templateRenderer.resolveObject(source, context);
            if (resolved instanceof Number num) {
                return clampParallel(num.intValue());
            }
            if (resolved != null) {
                try {
                    return clampParallel(Integer.parseInt(String.valueOf(resolved).trim()));
                } catch (NumberFormatException ignored) {
                    // fall through
                }
            }
        }
        int parallel = DEFAULT_PARALLEL;
        Object parallelObj = data.get("parallelLimit");
        if (parallelObj instanceof Number num) {
            parallel = num.intValue();
        } else if (parallelObj != null && StrUtil.isNotBlank(String.valueOf(parallelObj))) {
            try {
                parallel = Integer.parseInt(String.valueOf(parallelObj).trim());
            } catch (NumberFormatException ignored) {
                parallel = DEFAULT_PARALLEL;
            }
        }
        return clampParallel(parallel);
    }

    private int clampParallel(int value) {
        return Math.max(MIN_PARALLEL, Math.min(MAX_PARALLEL, value));
    }

    private int resolveMaxRuns(Map<String, Object> data) {
        int maxRuns = DEFAULT_MAX_RUNS;
        Object maxObj = data.get("maxRuns");
        if (maxObj instanceof Number num) {
            maxRuns = num.intValue();
        } else if (maxObj != null && StrUtil.isNotBlank(String.valueOf(maxObj))) {
            try {
                maxRuns = Integer.parseInt(String.valueOf(maxObj).trim());
            } catch (NumberFormatException ignored) {
                maxRuns = DEFAULT_MAX_RUNS;
            }
        }
        return Math.max(MIN_MAX_RUNS, Math.min(MAX_MAX_RUNS, maxRuns));
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseInputParameters(Object raw) {
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

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseOutputParameters(Object raw) {
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

    @SuppressWarnings("unchecked")
    private Map<String, Object> buildEmptyOutputs(Map<String, Object> data) {
        Map<String, Object> outputs = new HashMap<>();
        List<Map<String, Object>> outputParams = parseOutputParameters(data.get("outputParameters"));
        for (Map<String, Object> param : outputParams) {
            String key = param.get("key") == null ? "" : String.valueOf(param.get("key")).trim();
            if (StrUtil.isNotBlank(key)) {
                outputs.put(key, List.of());
            }
        }
        outputs.put("count", 0);
        outputs.put("index", -1);
        return outputs;
    }
}
