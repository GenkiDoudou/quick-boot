package io.github.genkidoudou.web.workflow.engine;

import io.github.genkidoudou.web.workflow.constants.WfNodeType;
import io.github.genkidoudou.web.workflow.constants.WorkflowConstants;
import io.github.genkidoudou.web.workflow.dto.GraphEdgeDto;
import io.github.genkidoudou.web.workflow.dto.GraphNodeDto;
import io.github.genkidoudou.web.workflow.dto.WorkflowGraphDto;
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
 * 工作流图结构校验器：校验开始/结束节点唯一性、DAG 无环、分支 handle、可达性等。
 */
@Component
public class WorkflowGraphValidator {

    /**
     * 校验图结构，失败时抛出 {@link IllegalArgumentException} 并附带中文原因。
     *
     * @param graph 图 DSL
     */
    public void validate(WorkflowGraphDto graph) {
        if (graph == null || graph.getNodes() == null || graph.getNodes().isEmpty()) {
            throw new IllegalArgumentException("工作流图不能为空");
        }
        List<GraphNodeDto> nodes = graph.getNodes();
        List<GraphEdgeDto> edges = graph.getEdges() == null ? List.of() : graph.getEdges();

        Map<String, GraphNodeDto> nodeMap = new HashMap<>();
        int startCount = 0;
        int endCount = 0;
        for (GraphNodeDto node : nodes) {
            if (node.getId() == null || node.getId().isBlank()) {
                throw new IllegalArgumentException("节点 ID 不能为空");
            }
            if (node.getType() == null || node.getType().isBlank()) {
                throw new IllegalArgumentException("节点 " + node.getId() + " 缺少 type");
            }
            if (nodeMap.containsKey(node.getId())) {
                throw new IllegalArgumentException("节点 ID 重复: " + node.getId());
            }
            nodeMap.put(node.getId(), node);
            if (WfNodeType.START.equals(node.getType())) {
                startCount++;
            }
            if (WfNodeType.END.equals(node.getType())) {
                endCount++;
            }
        }
        if (startCount != 1) {
            throw new IllegalArgumentException("工作流必须有且仅有一个开始节点");
        }
        if (endCount != 1) {
            throw new IllegalArgumentException("工作流必须有且仅有一个结束节点");
        }

        for (GraphEdgeDto edge : edges) {
            if (edge.getSource() == null || edge.getTarget() == null) {
                throw new IllegalArgumentException("连线 source/target 不能为空");
            }
            if (!nodeMap.containsKey(edge.getSource())) {
                throw new IllegalArgumentException("连线引用了不存在的源节点: " + edge.getSource());
            }
            if (!nodeMap.containsKey(edge.getTarget())) {
                throw new IllegalArgumentException("连线引用了不存在的目标节点: " + edge.getTarget());
            }
        }

        if (hasCycle(nodeMap, edges)) {
            throw new IllegalArgumentException("工作流图存在环，必须为 DAG");
        }

        String startId = findStartId(nodes);
        Set<String> reachable = bfsReachable(startId, edges);
        for (GraphNodeDto node : nodes) {
            if (isLoopInternalNode(node)) {
                continue;
            }
            if (!reachable.contains(node.getId())) {
                throw new IllegalArgumentException("存在从 start 不可达的孤立节点: " + node.getId());
            }
        }

        boolean endReachable = nodes.stream()
            .filter(n -> WfNodeType.END.equals(n.getType()))
            .anyMatch(n -> reachable.contains(n.getId()));
        if (!endReachable) {
            throw new IllegalArgumentException("从 start 无法到达结束节点");
        }

        validateBranchEdges(nodeMap, edges);
        validateLoopNodes(nodeMap);
        validateBatchNodes(nodeMap);
        validateLoopBodyOnlyNodes(nodeMap);
        validateLoopBodyAnchorNodes(nodeMap);
        validateNodeData(nodeMap);
    }

    /**
     * 校验各节点 data 业务规则（意图数量、必填项等）。
     */
    private void validateNodeData(Map<String, GraphNodeDto> nodeMap) {
        for (GraphNodeDto node : nodeMap.values()) {
            if (WfNodeType.QUESTION_CLASSIFIER.equals(node.getType())) {
                io.github.genkidoudou.web.workflow.util.QuestionClassifierDataUtil.validate(
                    node.getId(), node.getData());
            }
            if (WfNodeType.JSON_SERIALIZE.equals(node.getType())) {
                io.github.genkidoudou.web.workflow.util.JsonSerializeDataUtil.validate(
                    node.getId(), node.getData());
            }
            if (WfNodeType.JSON_DESERIALIZE.equals(node.getType())) {
                io.github.genkidoudou.web.workflow.util.JsonDeserializeDataUtil.validate(
                    node.getId(), node.getData());
            }
        }
    }

    private void validateLoopBodyAnchorNodes(Map<String, GraphNodeDto> nodeMap) {
        for (GraphNodeDto node : nodeMap.values()) {
            if (!isLoopBodyAnchorType(node.getType())) {
                continue;
            }
            String parentId = node.getParentId();
            if (parentId == null || parentId.isBlank()) {
                throw new IllegalArgumentException("节点 " + node.getId() + " 须置于循环体容器内");
            }
            GraphNodeDto parent = nodeMap.get(parentId);
            if (parent == null || !WfNodeType.LOOP_BODY.equals(parent.getType())) {
                throw new IllegalArgumentException("节点 " + node.getId() + " 须置于循环体容器内");
            }
        }
    }

    private boolean isLoopBodyAnchorType(String type) {
        return WfNodeType.LOOP_BODY_START.equals(type) || WfNodeType.LOOP_BODY_END.equals(type);
    }

    private void validateLoopBodyOnlyNodes(Map<String, GraphNodeDto> nodeMap) {
        for (GraphNodeDto node : nodeMap.values()) {
            if (!isLoopBodyOnlyType(node.getType())) {
                continue;
            }
            String parentId = node.getParentId();
            if (parentId == null || parentId.isBlank()) {
                throw new IllegalArgumentException("节点 " + node.getId() + " 仅能在循环体内使用");
            }
            GraphNodeDto parent = nodeMap.get(parentId);
            if (parent == null || !WfNodeType.LOOP_BODY.equals(parent.getType())) {
                throw new IllegalArgumentException("节点 " + node.getId() + " 须置于循环体容器内");
            }
        }
        for (GraphNodeDto node : nodeMap.values()) {
            if (node.getParentId() == null || node.getParentId().isBlank()) {
                continue;
            }
            if (isLoopBodyOnlyType(node.getType())) {
                continue;
            }
            if (WfNodeType.LOOP.equals(node.getType()) || WfNodeType.LOOP_BODY.equals(node.getType())) {
                continue;
            }
        }
    }

    private boolean isLoopBodyOnlyType(String type) {
        return WfNodeType.BREAK_LOOP.equals(type)
            || WfNodeType.CONTINUE_LOOP.equals(type)
            || WfNodeType.LOOP_SET_VARIABLE.equals(type);
    }

    private boolean isLoopInternalNode(GraphNodeDto node) {
        if (WfNodeType.LOOP_BODY.equals(node.getType()) || WfNodeType.BATCH_BODY.equals(node.getType())) {
            return true;
        }
        return node.getParentId() != null && !node.getParentId().isBlank();
    }

    @SuppressWarnings("unchecked")
    private void validateBatchNodes(Map<String, GraphNodeDto> nodeMap) {
        for (GraphNodeDto node : nodeMap.values()) {
            if (!WfNodeType.BATCH.equals(node.getType())) {
                continue;
            }
            Map<String, Object> data = node.getData() == null ? Map.of() : node.getData();
            String bodyId = data.get("bodyId") == null ? null : String.valueOf(data.get("bodyId")).trim();
            if (bodyId == null || bodyId.isBlank()) {
                throw new IllegalArgumentException("批处理节点 " + node.getId() + " 未配置批处理体 bodyId");
            }
            GraphNodeDto bodyNode = nodeMap.get(bodyId);
            if (bodyNode == null || !WfNodeType.BATCH_BODY.equals(bodyNode.getType())) {
                throw new IllegalArgumentException("批处理节点 " + node.getId() + " 的批处理体不存在或类型错误: " + bodyId);
            }

            Object inputParams = data.get("inputParameters");
            if (!(inputParams instanceof List<?> list) || list.isEmpty()) {
                throw new IllegalArgumentException("批处理节点 " + node.getId() + " 至少需要一个输入参数");
            }

            if (inputParams instanceof List<?> outputList) {
                for (Object item : outputList) {
                    if (!(item instanceof Map<?, ?> row)) {
                        continue;
                    }
                    Object key = row.get("key");
                    Object source = row.get("source");
                    if (key == null || String.valueOf(key).isBlank()) {
                        throw new IllegalArgumentException("批处理节点 " + node.getId() + " 的输入参数缺少 key");
                    }
                    if (source == null || String.valueOf(source).isBlank()) {
                        throw new IllegalArgumentException("批处理节点 " + node.getId() + " 的输入参数须引用数组来源");
                    }
                }
            }

            if (data.get("outputParameters") instanceof List<?> outList) {
                for (Object item : outList) {
                    if (!(item instanceof Map<?, ?> row)) {
                        continue;
                    }
                    String nodeId = row.get("nodeId") == null ? "" : String.valueOf(row.get("nodeId")).trim();
                    if (nodeId.isBlank()) {
                        continue;
                    }
                    GraphNodeDto outNode = nodeMap.get(nodeId);
                    if (outNode == null || !bodyId.equals(outNode.getParentId())) {
                        throw new IllegalArgumentException("批处理节点 " + node.getId() + " 的输出须引用批处理体内节点: " + nodeId);
                    }
                }
            }

            for (GraphNodeDto child : nodeMap.values()) {
                if (!bodyId.equals(child.getParentId())) {
                    continue;
                }
                if (isForbiddenInBatchBody(child.getType())) {
                    throw new IllegalArgumentException("批处理体内禁止节点类型: " + child.getType() + " (" + child.getId() + ")");
                }
            }
        }
    }

    private boolean isForbiddenInBatchBody(String type) {
        return WfNodeType.LOOP.equals(type)
            || WfNodeType.LOOP_BODY.equals(type)
            || WfNodeType.LOOP_BODY_START.equals(type)
            || WfNodeType.LOOP_BODY_END.equals(type)
            || WfNodeType.BATCH.equals(type)
            || WfNodeType.BATCH_BODY.equals(type)
            || isLoopBodyOnlyType(type);
    }

    @SuppressWarnings("unchecked")
    private void validateLoopNodes(Map<String, GraphNodeDto> nodeMap) {
        for (GraphNodeDto node : nodeMap.values()) {
            if (!WfNodeType.LOOP.equals(node.getType())) {
                continue;
            }
            Map<String, Object> data = node.getData() == null ? Map.of() : node.getData();
            String bodyId = data.get("bodyId") == null ? null : String.valueOf(data.get("bodyId")).trim();
            if (bodyId == null || bodyId.isBlank()) {
                throw new IllegalArgumentException("循环节点 " + node.getId() + " 未配置循环体 bodyId");
            }
            GraphNodeDto bodyNode = nodeMap.get(bodyId);
            if (bodyNode == null || !WfNodeType.LOOP_BODY.equals(bodyNode.getType())) {
                throw new IllegalArgumentException("循环节点 " + node.getId() + " 的循环体不存在或类型错误: " + bodyId);
            }

            String outputNodeId = data.get("outputNodeId") == null ? null : String.valueOf(data.get("outputNodeId")).trim();
            if (outputNodeId != null && !outputNodeId.isBlank()) {
                GraphNodeDto outputNode = nodeMap.get(outputNodeId);
                if (outputNode == null || !bodyId.equals(outputNode.getParentId())) {
                    throw new IllegalArgumentException("循环节点 " + node.getId() + " 的输出节点须在循环体内: " + outputNodeId);
                }
            }

            for (GraphNodeDto child : nodeMap.values()) {
                if (!bodyId.equals(child.getParentId())) {
                    continue;
                }
                if (WfNodeType.LOOP.equals(child.getType()) || WfNodeType.LOOP_BODY.equals(child.getType())
                    || WfNodeType.BATCH.equals(child.getType()) || WfNodeType.BATCH_BODY.equals(child.getType())) {
                    throw new IllegalArgumentException("循环体内禁止嵌套循环或批处理: " + child.getId());
                }
            }
        }
    }

    private void validateBranchEdges(Map<String, GraphNodeDto> nodeMap, List<GraphEdgeDto> edges) {
        for (GraphNodeDto node : nodeMap.values()) {
            if (WfNodeType.IF_ELSE.equals(node.getType())) {
                validateIfElseEdges(node, edges);
            }
            if (WfNodeType.QUESTION_CLASSIFIER.equals(node.getType())) {
                validateClassifierEdges(node.getId(), edges, node);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void validateIfElseEdges(GraphNodeDto node, List<GraphEdgeDto> edges) {
        String nodeId = node.getId();
        Set<String> branchHandles = collectIfElseBranchHandles(node.getData());
        boolean hasFalse = false;
        boolean hasBranchEdge = false;
        for (GraphEdgeDto edge : edges) {
            if (!nodeId.equals(edge.getSource())) {
                continue;
            }
            String handle = edge.getSourceHandle();
            if (WorkflowConstants.HANDLE_FALSE.equals(handle)) {
                hasFalse = true;
            }
            if (handle != null && branchHandles.contains(handle)) {
                hasBranchEdge = true;
            }
        }
        if (!hasFalse) {
            throw new IllegalArgumentException("条件分支节点 " + nodeId + " 必须连接「否则」出口");
        }
        if (!hasBranchEdge) {
            throw new IllegalArgumentException("条件分支节点 " + nodeId + " 至少需要连接一个条件分支出口");
        }
    }

    @SuppressWarnings("unchecked")
    private Set<String> collectIfElseBranchHandles(Map<String, Object> data) {
        Set<String> handles = new HashSet<>();
        if (data == null) {
            handles.add(WorkflowConstants.HANDLE_TRUE);
            return handles;
        }
        if (data.get("branches") instanceof List<?> branchList && !branchList.isEmpty()) {
            for (Object item : branchList) {
                if (!(item instanceof Map<?, ?> branch)) {
                    continue;
                }
                Object id = branch.get("id");
                if (id != null && !String.valueOf(id).isBlank()) {
                    handles.add(String.valueOf(id).trim());
                }
            }
        }
        if (handles.isEmpty()) {
            handles.add(WorkflowConstants.HANDLE_TRUE);
        }
        return handles;
    }

    @SuppressWarnings("unchecked")
    private void validateClassifierEdges(String nodeId, List<GraphEdgeDto> edges, GraphNodeDto node) {
        Map<String, Object> rawData = node.getData() == null ? Map.of() : node.getData();
        Map<String, Object> data = io.github.genkidoudou.web.workflow.util.QuestionClassifierDataUtil.normalize(rawData);
        int intentCount = io.github.genkidoudou.web.workflow.util.QuestionClassifierDataUtil.intentCount(data);

        boolean hasIntentEdge = false;
        boolean hasFallback = false;
        List<Map<String, Object>> legacyClasses = rawData.get("classes") instanceof List<?> list
            ? (List<Map<String, Object>>) list : null;

        for (GraphEdgeDto edge : edges) {
            if (!nodeId.equals(edge.getSource())) {
                continue;
            }
            String handle = edge.getSourceHandle();
            if (handle == null || handle.isBlank()) {
                throw new IllegalArgumentException("意图识别节点 " + nodeId + " 的出口边须带 sourceHandle");
            }
            if (io.github.genkidoudou.web.workflow.util.QuestionClassifierDataUtil.FALLBACK_HANDLE.equals(handle)) {
                hasFallback = true;
                continue;
            }
            try {
                int n = Integer.parseInt(handle.trim());
                if (n >= 1 && n <= intentCount) {
                    hasIntentEdge = true;
                } else if (n != 0) {
                    throw new IllegalArgumentException("意图识别节点 " + nodeId + " 的 sourceHandle 无效: " + handle);
                }
            } catch (NumberFormatException ex) {
                String mapped = io.github.genkidoudou.web.workflow.util.QuestionClassifierDataUtil
                    .mapLegacyHandle(handle, legacyClasses);
                if (mapped == null) {
                    throw new IllegalArgumentException("意图识别节点 " + nodeId
                        + " 的 sourceHandle 无法映射为数字 handle: " + handle
                        + "，请重新连线（1.." + intentCount + " 或 0 兜底）");
                }
                if (io.github.genkidoudou.web.workflow.util.QuestionClassifierDataUtil.FALLBACK_HANDLE.equals(mapped)) {
                    hasFallback = true;
                } else {
                    hasIntentEdge = true;
                }
            }
        }
        if (!hasIntentEdge) {
            throw new IllegalArgumentException("意图识别节点 " + nodeId + " 至少需要连接一条意图分支（sourceHandle 1.."
                + intentCount + "）");
        }
        if (!hasFallback) {
            throw new IllegalArgumentException("意图识别节点 " + nodeId + " 必须连接「其他」兜底出口（sourceHandle=0）");
        }
    }

    private String findStartId(List<GraphNodeDto> nodes) {
        for (GraphNodeDto node : nodes) {
            if (WfNodeType.START.equals(node.getType())) {
                return node.getId();
            }
        }
        throw new IllegalArgumentException("缺少 start 节点");
    }

    private Set<String> bfsReachable(String startId, List<GraphEdgeDto> edges) {
        Map<String, List<String>> adj = buildAdjacency(edges);
        Set<String> visited = new HashSet<>();
        Queue<String> queue = new ArrayDeque<>();
        queue.add(startId);
        visited.add(startId);
        while (!queue.isEmpty()) {
            String current = queue.poll();
            for (String next : adj.getOrDefault(current, List.of())) {
                if (visited.add(next)) {
                    queue.add(next);
                }
            }
        }
        return visited;
    }

    private boolean hasCycle(Map<String, GraphNodeDto> nodeMap, List<GraphEdgeDto> edges) {
        Map<String, List<String>> adj = buildAdjacencyForCycleCheck(nodeMap, edges);
        Set<String> visiting = new HashSet<>();
        Set<String> visited = new HashSet<>();
        for (String nodeId : nodeMap.keySet()) {
            if (dfsCycle(nodeId, adj, visiting, visited)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 构建环检测用的邻接表：排除循环/批处理容器的结构边（非主 DAG 语义）。
     * <p>
     * 循环体「容器→首节点」「末节点→容器」在画布上形成闭合，但语义上属于子图边界，不应判为主图成环。
     */
    private Map<String, List<String>> buildAdjacencyForCycleCheck(
        Map<String, GraphNodeDto> nodeMap, List<GraphEdgeDto> edges) {
        Map<String, List<String>> adj = new HashMap<>();
        for (GraphEdgeDto edge : edges) {
            if (isContainerStructuralEdge(edge, nodeMap)) {
                continue;
            }
            adj.computeIfAbsent(edge.getSource(), k -> new ArrayList<>()).add(edge.getTarget());
        }
        return adj;
    }

    private boolean isContainerStructuralEdge(GraphEdgeDto edge, Map<String, GraphNodeDto> nodeMap) {
        GraphNodeDto source = nodeMap.get(edge.getSource());
        GraphNodeDto target = nodeMap.get(edge.getTarget());
        if (source == null || target == null) {
            return false;
        }
        String sourceType = source.getType();
        String targetType = target.getType();

        if (WfNodeType.LOOP.equals(sourceType) && WfNodeType.LOOP_BODY.equals(targetType)) {
            return true;
        }
        if (WfNodeType.BATCH.equals(sourceType) && WfNodeType.BATCH_BODY.equals(targetType)) {
            return true;
        }
        if (WfNodeType.LOOP_BODY.equals(sourceType) && edge.getSource().equals(target.getParentId())) {
            return true;
        }
        if (WfNodeType.LOOP_BODY.equals(targetType) && edge.getTarget().equals(source.getParentId())) {
            return true;
        }
        if (WfNodeType.BATCH_BODY.equals(sourceType) && edge.getSource().equals(target.getParentId())) {
            return true;
        }
        if (WfNodeType.BATCH_BODY.equals(targetType) && edge.getTarget().equals(source.getParentId())) {
            return true;
        }
        return false;
    }

    private boolean dfsCycle(String nodeId, Map<String, List<String>> adj, Set<String> visiting, Set<String> visited) {
        if (visited.contains(nodeId)) {
            return false;
        }
        if (!visiting.add(nodeId)) {
            return true;
        }
        for (String next : adj.getOrDefault(nodeId, List.of())) {
            if (dfsCycle(next, adj, visiting, visited)) {
                return true;
            }
        }
        visiting.remove(nodeId);
        visited.add(nodeId);
        return false;
    }

    private Map<String, List<String>> buildAdjacency(List<GraphEdgeDto> edges) {
        Map<String, List<String>> adj = new HashMap<>();
        for (GraphEdgeDto edge : edges) {
            adj.computeIfAbsent(edge.getSource(), k -> new ArrayList<>()).add(edge.getTarget());
        }
        return adj;
    }
}
