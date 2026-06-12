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
 * 工作流图结构校验器：校验 start/answer 唯一性、DAG 无环、分支 handle、可达性等。
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
        int answerCount = 0;
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
            if (WfNodeType.ANSWER.equals(node.getType())) {
                answerCount++;
            }
        }
        if (startCount != 1) {
            throw new IllegalArgumentException("工作流必须有且仅有一个 start 节点");
        }
        if (answerCount < 1) {
            throw new IllegalArgumentException("工作流至少需要一个 answer 节点");
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
            if (!reachable.contains(node.getId())) {
                throw new IllegalArgumentException("存在从 start 不可达的孤立节点: " + node.getId());
            }
        }

        boolean answerReachable = nodes.stream()
            .filter(n -> WfNodeType.ANSWER.equals(n.getType()))
            .anyMatch(n -> reachable.contains(n.getId()));
        if (!answerReachable) {
            throw new IllegalArgumentException("从 start 无法到达任何 answer 节点");
        }

        validateBranchEdges(nodeMap, edges);
    }

    private void validateBranchEdges(Map<String, GraphNodeDto> nodeMap, List<GraphEdgeDto> edges) {
        for (GraphNodeDto node : nodeMap.values()) {
            if (WfNodeType.IF_ELSE.equals(node.getType())) {
                validateIfElseEdges(node.getId(), edges);
            }
            if (WfNodeType.QUESTION_CLASSIFIER.equals(node.getType())) {
                validateClassifierEdges(node.getId(), edges);
            }
        }
    }

    private void validateIfElseEdges(String nodeId, List<GraphEdgeDto> edges) {
        boolean hasTrue = false;
        boolean hasFalse = false;
        for (GraphEdgeDto edge : edges) {
            if (!nodeId.equals(edge.getSource())) {
                continue;
            }
            if (WorkflowConstants.HANDLE_TRUE.equals(edge.getSourceHandle())) {
                hasTrue = true;
            }
            if (WorkflowConstants.HANDLE_FALSE.equals(edge.getSourceHandle())) {
                hasFalse = true;
            }
        }
        if (!hasTrue || !hasFalse) {
            throw new IllegalArgumentException("if-else 节点 " + nodeId + " 必须同时有 true/false 出口边");
        }
    }

    private void validateClassifierEdges(String nodeId, List<GraphEdgeDto> edges) {
        boolean hasHandle = false;
        for (GraphEdgeDto edge : edges) {
            if (nodeId.equals(edge.getSource()) && edge.getSourceHandle() != null && !edge.getSourceHandle().isBlank()) {
                hasHandle = true;
                break;
            }
        }
        if (!hasHandle) {
            throw new IllegalArgumentException("question-classifier 节点 " + nodeId + " 的出口边须带 sourceHandle");
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
        Map<String, List<String>> adj = buildAdjacency(edges);
        Set<String> visiting = new HashSet<>();
        Set<String> visited = new HashSet<>();
        for (String nodeId : nodeMap.keySet()) {
            if (dfsCycle(nodeId, adj, visiting, visited)) {
                return true;
            }
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
