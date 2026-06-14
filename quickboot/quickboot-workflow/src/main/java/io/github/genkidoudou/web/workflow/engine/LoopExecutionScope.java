package io.github.genkidoudou.web.workflow.engine;



import lombok.Getter;

import lombok.Setter;



import java.util.ArrayList;

import java.util.HashMap;

import java.util.List;

import java.util.Map;



/**

 * 单次循环迭代的作用域：内置变量 item/index 与中间变量。

 */

@Getter

@Setter

public class LoopExecutionScope {



    private final String loopNodeId;



    private final String bodyId;



    private final List<String> intermediateKeys = new ArrayList<>();



    private final Map<String, String> intermediateTypes = new HashMap<>();



    private final Map<String, Object> intermediateVars = new HashMap<>();



    /** 数组循环时当前元素在 loop 输出上的字段名，默认 item。 */

    private String itemVariableKey = "item";



    /** 每轮迭代的数组参数当前元素（支持多数组）。 */

    private final Map<String, Object> arrayItems = new HashMap<>();



    private Object currentItem;



    private int currentIndex;



    /** 本轮是否请求终止整个循环。 */

    private boolean breakRequested;



    /** 本轮是否请求跳过剩余节点进入下一轮。 */

    private boolean continueRequested;



    /** 当前轮次体内节点执行摘要（调试 Trace）。 */

    private final List<Map<String, Object>> currentIterationSteps = new ArrayList<>();



    /** 全部轮次 Trace 汇总。 */

    private final List<Map<String, Object>> iterationTraces = new ArrayList<>();



    public LoopExecutionScope(String loopNodeId, String bodyId) {

        this.loopNodeId = loopNodeId;

        this.bodyId = bodyId;

    }



    /**

     * 新一轮迭代开始：清空控制标记与本轮步骤缓存。

     *

     * @param index 轮次索引

     * @param item  主数组当前元素

     */

    public void beginIteration(int index, Object item) {

        currentIndex = index;

        currentItem = item;

        arrayItems.clear();

        if (itemVariableKey != null && !itemVariableKey.isBlank()) {

            arrayItems.put(itemVariableKey, item);

        }

        currentIterationSteps.clear();

        clearControlFlags();

    }



    /**

     * 记录本轮体内单节点执行摘要。

     *

     * @param nodeId   节点 ID

     * @param nodeType 节点类型

     * @param outputs  节点输出

     * @param durationMs 耗时

     */

    public void recordBodyStep(String nodeId, String nodeType, Map<String, Object> outputs, long durationMs) {

        Map<String, Object> step = new HashMap<>();

        step.put("nodeId", nodeId);

        step.put("nodeType", nodeType);

        step.put("outputs", outputs == null ? Map.of() : new HashMap<>(outputs));

        step.put("durationMs", durationMs);

        currentIterationSteps.add(step);

    }



    /**

     * 结束当前轮次，写入 iterationTraces。

     */

    public void finishIteration() {

        Map<String, Object> trace = new HashMap<>();

        trace.put("index", currentIndex);

        trace.put("item", currentItem);

        trace.put("arrayItems", new HashMap<>(arrayItems));

        trace.put("intermediateVars", new HashMap<>(intermediateVars));

        trace.put("steps", new ArrayList<>(currentIterationSteps));

        iterationTraces.add(trace);

    }



    /**

     * 将当前迭代变量同步到上下文，供模板 {@code {{loopId.field}}} 引用。

     *

     * @param context 运行时上下文

     */

    public void syncToContext(WorkflowContext context) {

        Map<String, Object> loopOut = new HashMap<>();

        loopOut.put("index", currentIndex);

        loopOut.putAll(arrayItems);

        if (itemVariableKey != null && !itemVariableKey.isBlank() && !arrayItems.containsKey(itemVariableKey)) {

            loopOut.put(itemVariableKey, currentItem);

        }

        loopOut.put("item", currentItem);

        loopOut.putAll(intermediateVars);

        context.putNodeOutputs(loopNodeId, loopOut);

    }



    /** 新一轮迭代开始前重置控制标记。 */

    public void clearControlFlags() {

        breakRequested = false;

        continueRequested = false;

    }



    /**

     * 读取本轮迭代结束方式。

     *

     * @return 迭代结果

     */

    public LoopIterationResult iterationOutcome() {

        if (breakRequested) {

            return LoopIterationResult.BREAK;

        }

        if (continueRequested) {

            return LoopIterationResult.CONTINUE;

        }

        return LoopIterationResult.NORMAL;

    }

}


