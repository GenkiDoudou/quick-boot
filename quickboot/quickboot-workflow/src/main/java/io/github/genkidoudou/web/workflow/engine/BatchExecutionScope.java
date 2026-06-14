package io.github.genkidoudou.web.workflow.engine;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * 单次批处理项的作用域：当前索引与各输入参数的元素值。
 */
@Getter
@Setter
public class BatchExecutionScope {

    private final String batchNodeId;

    private final String bodyId;

    private final int currentIndex;

    private final Map<String, Object> inputItems = new HashMap<>();

    public BatchExecutionScope(String batchNodeId, String bodyId, int currentIndex) {
        this.batchNodeId = batchNodeId;
        this.bodyId = bodyId;
        this.currentIndex = currentIndex;
    }

    /**
     * 将当前批处理项变量同步到上下文，供模板 {@code {{batchId.paramKey}}} 引用。
     *
     * @param context 运行时上下文
     */
    public void syncToContext(WorkflowContext context) {
        Map<String, Object> batchOut = new HashMap<>();
        batchOut.put("index", currentIndex);
        batchOut.putAll(inputItems);
        context.putNodeOutputs(batchNodeId, batchOut);
    }
}
