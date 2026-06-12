package io.github.genkidoudou.web.workflow.engine;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * 工作流运行时上下文：节点输出、系统变量、运行入参与 start 节点 ID。
 */
@Getter
public class WorkflowContext {

    private final Long runId;

    private final String startNodeId;

    /** Start 节点入参（与 inputs.* 简写互通）。 */
    private final Map<String, Object> runInputs = new HashMap<>();

    /** 系统注入变量：runId、kbId、userId 等。 */
    private final Map<String, Object> sysVariables = new HashMap<>();

    /** 各节点输出：nodeId -> 字段 Map。 */
    private final Map<String, Map<String, Object>> nodeOutputs = new HashMap<>();

    /** 是否启用 SSE 流式输出。 */
    @Setter
    private boolean streamEnabled;

    public WorkflowContext(Long runId, String startNodeId) {
        this.runId = runId;
        this.startNodeId = startNodeId;
    }

    /**
     * 写入节点输出字段。
     *
     * @param nodeId 节点 ID
     * @param key    字段名
     * @param value  字段值
     */
    public void putNodeOutput(String nodeId, String key, Object value) {
        nodeOutputs.computeIfAbsent(nodeId, k -> new HashMap<>()).put(key, value);
    }

    /**
     * 批量写入节点输出。
     *
     * @param nodeId  节点 ID
     * @param outputs 输出 Map
     */
    public void putNodeOutputs(String nodeId, Map<String, Object> outputs) {
        if (outputs == null || outputs.isEmpty()) {
            return;
        }
        nodeOutputs.computeIfAbsent(nodeId, k -> new HashMap<>()).putAll(outputs);
    }

    /**
     * 获取节点完整输出 Map。
     *
     * @param nodeId 节点 ID
     * @return 输出 Map，不存在时返回空 Map
     */
    public Map<String, Object> getNodeOutputMap(String nodeId) {
        return nodeOutputs.getOrDefault(nodeId, Map.of());
    }
}
