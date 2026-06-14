package io.github.genkidoudou.web.workflow.engine;

import io.github.genkidoudou.web.workflow.dto.WorkflowGraphDto;
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

    /** 当前执行的完整图 DSL（循环体子图调度需要）。 */
    @Setter
    private WorkflowGraphDto executionGraph;

    /** 当前循环迭代作用域；非循环体执行时为 null。 */
    @Setter
    private LoopExecutionScope currentLoopScope;

    /** 当前批处理项作用域；非批处理体执行时为 null。 */
    @Setter
    private BatchExecutionScope currentBatchScope;

    /** 全局步骤顺序号（主图 + 子图共用）。 */
    private int stepOrder;

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

    /**
     * 获取并递增全局步骤顺序号。
     *
     * @return 新顺序号（从 1 开始）
     */
    public int nextStepOrder() {
        return ++stepOrder;
    }

    /**
     * 复制当前上下文供批处理并行迭代使用（共享上游输出，隔离本轮写入）。
     *
     * @return 可独立写入的上下文副本
     */
    public WorkflowContext forkSnapshot() {
        WorkflowContext fork = new WorkflowContext(runId, startNodeId);
        fork.getRunInputs().putAll(runInputs);
        fork.getSysVariables().putAll(sysVariables);
        for (Map.Entry<String, Map<String, Object>> entry : nodeOutputs.entrySet()) {
            fork.putNodeOutputs(entry.getKey(), new HashMap<>(entry.getValue()));
        }
        fork.setExecutionGraph(executionGraph);
        fork.setStreamEnabled(false);
        return fork;
    }
}
