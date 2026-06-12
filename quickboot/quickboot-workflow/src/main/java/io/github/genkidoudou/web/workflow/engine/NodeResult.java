package io.github.genkidoudou.web.workflow.engine;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 节点执行结果：状态、输出字段、分支 handle（if-else / classifier）。
 */
@Getter
public class NodeResult {

    private final boolean success;

    private final Map<String, Object> outputs;

    /** 分支路由 handle，如 true/false 或 classifier classId。 */
    private final String branchHandle;

    private final String errorMessage;

    private NodeResult(boolean success, Map<String, Object> outputs, String branchHandle, String errorMessage) {
        this.success = success;
        this.outputs = outputs == null ? Map.of() : outputs;
        this.branchHandle = branchHandle;
        this.errorMessage = errorMessage;
    }

    /**
     * 构造成功结果。
     *
     * @param outputs 节点输出
     * @return 成功结果
     */
    public static NodeResult success(Map<String, Object> outputs) {
        return new NodeResult(true, outputs, null, null);
    }

    /**
     * 构造带分支 handle 的成功结果。
     *
     * @param outputs      节点输出
     * @param branchHandle 分支标识
     * @return 成功结果
     */
    public static NodeResult successWithBranch(Map<String, Object> outputs, String branchHandle) {
        return new NodeResult(true, outputs, branchHandle, null);
    }

    /**
     * 构造失败结果。
     *
     * @param errorMessage 失败原因
     * @return 失败结果
     */
    public static NodeResult failed(String errorMessage) {
        return new NodeResult(false, new HashMap<>(), null, errorMessage);
    }
}
