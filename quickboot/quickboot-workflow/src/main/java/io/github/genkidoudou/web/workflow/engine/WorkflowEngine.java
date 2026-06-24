package io.github.genkidoudou.web.workflow.engine;

import cn.hutool.json.JSONUtil;
import io.github.genkidoudou.web.workflow.constants.WfNodeType;
import io.github.genkidoudou.web.workflow.constants.WfRunStatus;
import io.github.genkidoudou.web.workflow.constants.WorkflowConstants;
import io.github.genkidoudou.web.workflow.domain.WfRun;
import io.github.genkidoudou.web.workflow.dto.GraphEdgeDto;
import io.github.genkidoudou.web.workflow.dto.GraphNodeDto;
import io.github.genkidoudou.web.workflow.dto.WorkflowGraphDto;
import io.github.genkidoudou.web.workflow.mapper.WfRunMapper;
import io.github.genkidoudou.web.workflow.stream.WorkflowStreamEmitter;
import io.github.genkidoudou.web.workflow.support.WorkflowTraceSanitizer;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * 工作流执行引擎：拓扑排序 + 分支路由 + Trace 落库 + SSE 事件桥接。
 */
@Component
public class WorkflowEngine {

    private final NodeHandlerRegistry handlerRegistry;
    private final WorkflowGraphValidator graphValidator;
    private final WfRunMapper runMapper;
    private final WorkflowStreamEmitter streamEmitter;
    private final WorkflowTraceSanitizer traceSanitizer;
    private final WorkflowStepRecorder stepRecorder;

    public WorkflowEngine(NodeHandlerRegistry handlerRegistry,
                          WorkflowGraphValidator graphValidator,
                          WfRunMapper runMapper,
                          WorkflowStreamEmitter streamEmitter,
                          WorkflowTraceSanitizer traceSanitizer,
                          WorkflowStepRecorder stepRecorder) {
        this.handlerRegistry = handlerRegistry;
        this.graphValidator = graphValidator;
        this.runMapper = runMapper;
        this.streamEmitter = streamEmitter;
        this.traceSanitizer = traceSanitizer;
        this.stepRecorder = stepRecorder;
    }

    /**
     * 执行已创建的 run 实例。
     *
     * @param runId     运行 ID
     * @param graphJson 图 DSL JSON
     * @param context   运行时上下文（已填充 inputs 与 sys 变量）
     * @return 最终 answer 节点输出；失败时 outputs 为空
     */
    public Map<String, Object> execute(Long runId, String graphJson, WorkflowContext context) {
        WorkflowGraphDto graph = JSONUtil.toBean(graphJson, WorkflowGraphDto.class);
        graphValidator.validate(graph);

        Map<String, GraphNodeDto> nodeMap = indexNodes(graph.getNodes());
        List<GraphEdgeDto> edges = graph.getEdges() == null ? List.of() : graph.getEdges();
        String startId = findStartId(graph.getNodes());
        context.setExecutionGraph(graph);
        context.getSysVariables().putIfAbsent("runId", runId);

        WfRun run = runMapper.selectById(runId);
        if (run != null) {
            run.setStatus(WfRunStatus.RUNNING);
            run.setStartTime(LocalDateTime.now());
            runMapper.updateById(run);
        }

        long runStart = System.currentTimeMillis();
        Set<String> executed = new HashSet<>();
        /** 已激活的边：仅沿这些边调度后继，未激活分支不会阻塞合并节点（如变量聚合）。 */
        Set<String> activatedEdges = new HashSet<>();
        Queue<String> queue = new ArrayDeque<>();
        queue.add(startId);

        Map<String, Object> finalOutputs = new HashMap<>();
        String errorMsg = null;
        int rescheduleCount = 0;
        int maxReschedules = Math.max(nodeMap.size() * (nodeMap.size() + 1), edges.size() + 1);

        try {
            while (!queue.isEmpty()) {
                String nodeId = queue.poll();
                if (executed.contains(nodeId)) {
                    continue;
                }
                if (!canExecute(nodeId, edges, executed, activatedEdges)) {
                    queue.add(nodeId);
                    rescheduleCount++;
                    if (rescheduleCount > maxReschedules) {
                        errorMsg = "工作流调度异常：节点依赖无法满足，请检查分支合并与连线";
                        markRunFailed(run, runStart, errorMsg);
                        streamEmitter.emitError(runId, errorMsg, nodeId);
                        return Map.of();
                    }
                    continue;
                }
                rescheduleCount = 0;

                GraphNodeDto node = nodeMap.get(nodeId);
                if (node == null) {
                    continue;
                }
                if (shouldSkipMainSchedule(node)) {
                    executed.add(nodeId);
                    continue;
                }
                int orderNo = context.nextStepOrder();
                long stepStart = System.currentTimeMillis();
                streamEmitter.emitStepStart(runId, nodeId, node.getType(), orderNo);

                NodeHandler handler = handlerRegistry.require(node.getType());
                NodeResult result;
                try {
                    result = handler.execute(node, context);
                } catch (Exception ex) {
                    result = NodeResult.failed(ex.getMessage());
                }

                long stepDuration = System.currentTimeMillis() - stepStart;
                if (result.isSuccess()) {
                    context.putNodeOutputs(nodeId, result.getOutputs());
                    Map<String, Object> traceInputs = result.getTraceInputs();
                    Map<String, Object> safeInputs = traceInputs == null ? Map.of() : traceInputs;
                    stepRecorder.insert(context, orderNo, node, "SUCCESS", safeInputs, result.getOutputs(), null, stepDuration);
                    streamEmitter.emitStepEnd(runId, nodeId, node.getType(), "SUCCESS", stepDuration,
                        traceSanitizer.sanitizeMap(safeInputs), traceSanitizer.sanitizeMap(result.getOutputs()), null);
                    executed.add(nodeId);

                    if (WfNodeType.ANSWER.equals(node.getType()) || WfNodeType.END.equals(node.getType())) {
                        finalOutputs.putAll(result.getOutputs());
                    }

                    enqueueSuccessors(node, nodeId, edges, result.getBranchHandle(), queue, activatedEdges);
                } else {
                    errorMsg = result.getErrorMessage();
                    stepRecorder.insert(context, orderNo, node, "FAILED", Map.of(), Map.of(), errorMsg, stepDuration);
                    streamEmitter.emitStepEnd(runId, nodeId, node.getType(), "FAILED", stepDuration,
                        Map.of(), Map.of(), errorMsg);
                    streamEmitter.emitError(runId, errorMsg, nodeId);
                    markRunFailed(run, runStart, errorMsg);
                    return Map.of();
                }
            }
            markRunSuccess(run, runStart, finalOutputs, context.isStreamEnabled());
            if (context.isStreamEnabled()) {
                streamEmitter.emitDone(runId, WfRunStatus.SUCCESS, finalOutputs, System.currentTimeMillis() - runStart);
            }
            return finalOutputs;
        } catch (Exception ex) {
            errorMsg = ex.getMessage();
            markRunFailed(run, runStart, errorMsg);
            streamEmitter.emitError(runId, errorMsg, null);
            throw ex;
        }
    }

    private void enqueueSuccessors(GraphNodeDto node, String nodeId, List<GraphEdgeDto> edges,
                                   String branchHandle, Queue<String> queue, Set<String> activatedEdges) {
        boolean isBranchNode = WfNodeType.IF_ELSE.equals(node.getType())
            || WfNodeType.QUESTION_CLASSIFIER.equals(node.getType());
        for (GraphEdgeDto edge : edges) {
            if (!nodeId.equals(edge.getSource())) {
                continue;
            }
            if (isBranchNode && branchHandle != null
                && !matchesBranchHandle(edge.getSourceHandle(), branchHandle, node.getType())) {
                // 未命中分支的边不激活，合并节点不会等待该分支上的前驱
                continue;
            }
            activatedEdges.add(edgeKey(edge));
            queue.add(edge.getTarget());
        }
    }

    /** 唯一标识一条有向边（含 sourceHandle，区分 if-else 多出口）。 */
    private String edgeKey(GraphEdgeDto edge) {
        String handle = edge.getSourceHandle();
        return edge.getSource() + "->" + edge.getTarget() + "#" + (handle == null ? "" : handle);
    }

    /**
     * 判断边的 sourceHandle 是否匹配本次命中的分支。
     * if-else 无 handle 的旧连线视为「如果」(true) 出口。
     */
    private boolean matchesBranchHandle(String edgeHandle, String branchHandle, String nodeType) {
        if (edgeHandle == null || edgeHandle.isBlank()) {
            if (WfNodeType.IF_ELSE.equals(nodeType)) {
                return WorkflowConstants.HANDLE_TRUE.equals(branchHandle);
            }
            return false;
        }
        return edgeHandle.equals(branchHandle);
    }

    @SuppressWarnings("unused")
    private void markSkippedSubtree(String nodeId, List<GraphEdgeDto> edges, Set<String> skipped, Set<String> allNodeIds) {
        Queue<String> q = new ArrayDeque<>();
        q.add(nodeId);
        while (!q.isEmpty()) {
            String current = q.poll();
            if (!skipped.add(current)) {
                continue;
            }
            for (GraphEdgeDto edge : edges) {
                if (current.equals(edge.getSource()) && allNodeIds.contains(edge.getTarget())) {
                    q.add(edge.getTarget());
                }
            }
        }
    }

    private boolean canExecute(String nodeId, List<GraphEdgeDto> edges, Set<String> executed,
                               Set<String> activatedEdges) {
        for (GraphEdgeDto edge : edges) {
            if (!nodeId.equals(edge.getTarget())) {
                continue;
            }
            if (!activatedEdges.contains(edgeKey(edge))) {
                continue;
            }
            if (!executed.contains(edge.getSource())) {
                return false;
            }
        }
        return true;
    }

    private boolean shouldSkipMainSchedule(GraphNodeDto node) {
        if (WfNodeType.LOOP_BODY.equals(node.getType()) || WfNodeType.BATCH_BODY.equals(node.getType())) {
            return true;
        }
        return node.getParentId() != null && !node.getParentId().isBlank();
    }

    private void markRunSuccess(WfRun run, long runStart, Map<String, Object> outputs, boolean streamEnabled) {
        if (run == null) {
            return;
        }
        run.setStatus(WfRunStatus.SUCCESS);
        run.setOutputsJson(JSONUtil.toJsonStr(outputs));
        run.setDurationMs(System.currentTimeMillis() - runStart);
        run.setEndTime(LocalDateTime.now());
        runMapper.updateById(run);
    }

    private void markRunFailed(WfRun run, long runStart, String errorMsg) {
        if (run == null) {
            return;
        }
        run.setStatus(WfRunStatus.FAILED);
        run.setErrorMsg(errorMsg);
        run.setDurationMs(System.currentTimeMillis() - runStart);
        run.setEndTime(LocalDateTime.now());
        runMapper.updateById(run);
    }

    private Map<String, GraphNodeDto> indexNodes(List<GraphNodeDto> nodes) {
        Map<String, GraphNodeDto> map = new HashMap<>();
        for (GraphNodeDto node : nodes) {
            map.put(node.getId(), node);
        }
        return map;
    }

    private String findStartId(List<GraphNodeDto> nodes) {
        for (GraphNodeDto node : nodes) {
            if (WfNodeType.START.equals(node.getType())) {
                return node.getId();
            }
        }
        throw new IllegalArgumentException("缺少 start 节点");
    }
}
