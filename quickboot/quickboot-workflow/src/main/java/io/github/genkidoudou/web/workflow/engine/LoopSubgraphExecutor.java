package io.github.genkidoudou.web.workflow.engine;

import io.github.genkidoudou.web.workflow.constants.WfNodeType;
import io.github.genkidoudou.web.workflow.constants.WorkflowConstants;
import io.github.genkidoudou.web.workflow.dto.GraphEdgeDto;
import io.github.genkidoudou.web.workflow.dto.GraphNodeDto;
import io.github.genkidoudou.web.workflow.dto.WorkflowGraphDto;
import io.github.genkidoudou.web.workflow.stream.WorkflowStreamEmitter;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * 循环体子图执行器：在单次迭代内按 DAG 串行执行循环体节点。
 * <p>
 * 支持容器左右 handle：body-entry（容器→首节点）、body-exit（末节点→容器）；
 * 历史 loop-body-start/end 锚点节点不参与调度。
 */
@Component
public class LoopSubgraphExecutor {

    private static final int MAX_RESCHEDULES_FACTOR = 16;

    private final NodeHandlerRegistry handlerRegistry;

    private final WorkflowStepRecorder stepRecorder;

    private final WorkflowStreamEmitter streamEmitter;

    public LoopSubgraphExecutor(@Lazy NodeHandlerRegistry handlerRegistry,
                                WorkflowStepRecorder stepRecorder,
                                WorkflowStreamEmitter streamEmitter) {
        this.handlerRegistry = handlerRegistry;
        this.stepRecorder = stepRecorder;
        this.streamEmitter = streamEmitter;
    }

    /**
     * 执行循环体子图一次迭代。
     *
     * @param graph   完整工作流图
     * @param bodyId  循环体容器节点 ID
     * @param context 运行时上下文（须已设置 {@link LoopExecutionScope}）
     * @return 本轮迭代结束方式
     */
    public LoopIterationResult executeIteration(WorkflowGraphDto graph, String bodyId, WorkflowContext context) {
        LoopExecutionScope scope = context.getCurrentLoopScope();
        if (scope != null) {
            scope.clearControlFlags();
        }
        List<GraphNodeDto> allNodes = graph.getNodes() == null ? List.of() : graph.getNodes();
        List<GraphEdgeDto> allEdges = graph.getEdges() == null ? List.of() : graph.getEdges();

        Map<String, GraphNodeDto> bodyNodeMap = new HashMap<>();
        for (GraphNodeDto node : allNodes) {
            if (!bodyId.equals(node.getParentId())) {
                continue;
            }
            if (isNonExecutableLoopBodyNode(node.getType())) {
                continue;
            }
            bodyNodeMap.put(node.getId(), node);
        }
        if (bodyNodeMap.isEmpty()) {
            return LoopIterationResult.NORMAL;
        }

        Set<String> bodyIds = bodyNodeMap.keySet();
        List<GraphEdgeDto> bodyEdges = new ArrayList<>();
        for (GraphEdgeDto edge : allEdges) {
            String src = edge.getSource();
            String tgt = edge.getTarget();
            if (bodyIds.contains(src) && bodyIds.contains(tgt)) {
                bodyEdges.add(edge);
            } else if (bodyId.equals(src) && bodyIds.contains(tgt)) {
                bodyEdges.add(edge);
            }
        }

        Set<String> executed = new HashSet<>();
        Set<String> activatedEdges = new HashSet<>();
        for (GraphEdgeDto edge : allEdges) {
            if (bodyId.equals(edge.getSource()) && bodyIds.contains(edge.getTarget())) {
                activatedEdges.add(edgeKey(edge));
            }
        }

        Queue<String> queue = new ArrayDeque<>();
        for (String nodeId : bodyIds) {
            if (!hasInternalPredecessor(nodeId, bodyIds, bodyId, bodyEdges)) {
                queue.add(nodeId);
            }
        }
        if (queue.isEmpty()) {
            queue.addAll(bodyIds);
        }

        int maxReschedules = Math.max(bodyIds.size() * MAX_RESCHEDULES_FACTOR, bodyEdges.size() + 1);
        int rescheduleCount = 0;

        while (!queue.isEmpty()) {
            String nodeId = queue.poll();
            if (executed.contains(nodeId)) {
                continue;
            }
            if (!canExecute(nodeId, bodyId, bodyEdges, executed, activatedEdges)) {
                queue.add(nodeId);
                rescheduleCount++;
                if (rescheduleCount > maxReschedules) {
                    throw new IllegalStateException("循环体调度异常：节点依赖无法满足，请检查循环体内连线");
                }
                continue;
            }
            rescheduleCount = 0;

            GraphNodeDto node = bodyNodeMap.get(nodeId);
            if (node == null) {
                continue;
            }
            long stepStart = System.currentTimeMillis();
            int orderNo = 0;
            if (context.getRunId() != null) {
                orderNo = context.nextStepOrder();
                streamEmitter.emitStepStart(context.getRunId(), nodeId, node.getType(), orderNo);
            }
            NodeHandler handler = handlerRegistry.require(node.getType());
            NodeResult result = handler.execute(node, context);
            long stepDuration = System.currentTimeMillis() - stepStart;
            if (!result.isSuccess()) {
                if (context.getRunId() != null) {
                    stepRecorder.insert(context, orderNo, node, "FAILED", Map.of(), Map.of(),
                        result.getErrorMessage(), stepDuration);
                    streamEmitter.emitStepEnd(context.getRunId(), nodeId, node.getType(), "FAILED", stepDuration,
                        Map.of(), Map.of(), result.getErrorMessage());
                }
                throw new IllegalStateException(
                    "循环体节点 " + nodeId + " 执行失败: " + result.getErrorMessage());
            }
            context.putNodeOutputs(nodeId, result.getOutputs());
            if (scope != null) {
                scope.recordBodyStep(nodeId, node.getType(), result.getOutputs(), stepDuration);
            }
            if (context.getRunId() != null) {
                Map<String, Object> traceInputs = result.getTraceInputs();
                Map<String, Object> safeInputs = traceInputs == null ? Map.of() : traceInputs;
                stepRecorder.insert(context, orderNo, node, "SUCCESS", safeInputs, result.getOutputs(), null, stepDuration);
                streamEmitter.emitStepEnd(context.getRunId(), nodeId, node.getType(), "SUCCESS", stepDuration,
                    safeInputs, result.getOutputs(), null);
            }
            executed.add(nodeId);

            LoopIterationResult control = scope == null ? LoopIterationResult.NORMAL : scope.iterationOutcome();
            if (control != LoopIterationResult.NORMAL) {
                return control;
            }

            enqueueSuccessors(node, nodeId, bodyId, bodyEdges, result.getBranchHandle(), queue, activatedEdges);
        }
        return LoopIterationResult.NORMAL;
    }

    private boolean isNonExecutableLoopBodyNode(String type) {
        return WfNodeType.LOOP_BODY.equals(type)
            || WfNodeType.BATCH_BODY.equals(type)
            || WfNodeType.LOOP_BODY_START.equals(type)
            || WfNodeType.LOOP_BODY_END.equals(type);
    }

    private boolean hasInternalPredecessor(String nodeId, Set<String> bodyIds, String bodyId,
                                           List<GraphEdgeDto> bodyEdges) {
        for (GraphEdgeDto edge : bodyEdges) {
            if (!nodeId.equals(edge.getTarget())) {
                continue;
            }
            String src = edge.getSource();
            if (bodyIds.contains(src)) {
                return true;
            }
            if (bodyId.equals(src)) {
                return true;
            }
        }
        return false;
    }

    private void enqueueSuccessors(GraphNodeDto node, String nodeId, String bodyId, List<GraphEdgeDto> edges,
                                   String branchHandle, Queue<String> queue, Set<String> activatedEdges) {
        boolean isBranchNode = WfNodeType.IF_ELSE.equals(node.getType())
            || WfNodeType.QUESTION_CLASSIFIER.equals(node.getType());
        for (GraphEdgeDto edge : edges) {
            if (!nodeId.equals(edge.getSource())) {
                continue;
            }
            if (bodyId.equals(edge.getTarget())) {
                continue;
            }
            if (isBranchNode && branchHandle != null
                && !matchesBranchHandle(edge.getSourceHandle(), branchHandle, node.getType())) {
                continue;
            }
            activatedEdges.add(edgeKey(edge));
            queue.add(edge.getTarget());
        }
    }

    private boolean matchesBranchHandle(String edgeHandle, String branchHandle, String nodeType) {
        if (edgeHandle == null || edgeHandle.isBlank()) {
            if (WfNodeType.IF_ELSE.equals(nodeType)) {
                return WorkflowConstants.HANDLE_TRUE.equals(branchHandle);
            }
            return false;
        }
        return edgeHandle.equals(branchHandle);
    }

    private String edgeKey(GraphEdgeDto edge) {
        String handle = edge.getSourceHandle();
        return edge.getSource() + "->" + edge.getTarget() + "#" + (handle == null ? "" : handle);
    }

    private boolean canExecute(String nodeId, String bodyId, List<GraphEdgeDto> edges, Set<String> executed,
                               Set<String> activatedEdges) {
        for (GraphEdgeDto edge : edges) {
            if (!nodeId.equals(edge.getTarget())) {
                continue;
            }
            if (!activatedEdges.contains(edgeKey(edge))) {
                continue;
            }
            if (bodyId.equals(edge.getSource())) {
                continue;
            }
            if (!executed.contains(edge.getSource())) {
                return false;
            }
        }
        return true;
    }
}
