package io.github.genkidoudou.web.workflow.engine;

import cn.hutool.json.JSONUtil;
import io.github.genkidoudou.web.workflow.domain.WfRunStep;
import io.github.genkidoudou.web.workflow.dto.GraphNodeDto;
import io.github.genkidoudou.web.workflow.mapper.WfRunStepMapper;
import io.github.genkidoudou.web.workflow.support.WorkflowTraceSanitizer;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 运行步骤 Trace 落库：主图与循环/批处理子图共用，支持循环轮次元数据。
 */
@Component
public class WorkflowStepRecorder {

    private final WfRunStepMapper runStepMapper;
    private final WorkflowTraceSanitizer traceSanitizer;

    public WorkflowStepRecorder(WfRunStepMapper runStepMapper, WorkflowTraceSanitizer traceSanitizer) {
        this.runStepMapper = runStepMapper;
        this.traceSanitizer = traceSanitizer;
    }

    /**
     * 记录一步执行 Trace（自动递增顺序号）。
     */
    public int record(WorkflowContext context, GraphNodeDto node, String status,
                      Map<String, Object> inputs, Map<String, Object> outputs,
                      String errorMsg, long durationMs) {
        if (context == null || context.getRunId() == null || node == null) {
            return 0;
        }
        return insert(context, context.nextStepOrder(), node, status, inputs, outputs, errorMsg, durationMs);
    }

    /**
     * 以指定顺序号记录步骤（与 SSE step_start 对齐时使用）。
     */
    public int insert(WorkflowContext context, int orderNo, GraphNodeDto node, String status,
                      Map<String, Object> inputs, Map<String, Object> outputs,
                      String errorMsg, long durationMs) {
        if (context == null || context.getRunId() == null || node == null) {
            return 0;
        }
        WfRunStep step = new WfRunStep();
        step.setRunId(context.getRunId());
        step.setNodeId(node.getId());
        step.setNodeType(node.getType());
        step.setStatus(status);
        step.setInputsJson(JSONUtil.toJsonStr(traceSanitizer.sanitizeMap(enrichLoopMeta(inputs, context))));
        step.setOutputsJson(JSONUtil.toJsonStr(traceSanitizer.sanitizeMap(outputs == null ? Map.of() : outputs)));
        step.setErrorMsg(errorMsg);
        step.setDurationMs(durationMs);
        step.setOrderNo(orderNo);
        step.setCreateTime(LocalDateTime.now());
        runStepMapper.insert(step);
        return orderNo;
    }

    private Map<String, Object> enrichLoopMeta(Map<String, Object> inputs, WorkflowContext context) {
        Map<String, Object> merged = new HashMap<>(inputs == null ? Map.of() : inputs);
        LoopExecutionScope loopScope = context.getCurrentLoopScope();
        if (loopScope != null) {
            Map<String, Object> loopMeta = new HashMap<>();
            loopMeta.put("loopNodeId", loopScope.getLoopNodeId());
            loopMeta.put("iteration", loopScope.getCurrentIndex());
            merged.put("_loop", loopMeta);
        }
        BatchExecutionScope batchScope = context.getCurrentBatchScope();
        if (batchScope != null) {
            Map<String, Object> batchMeta = new HashMap<>();
            batchMeta.put("batchNodeId", batchScope.getBatchNodeId());
            batchMeta.put("iteration", batchScope.getCurrentIndex());
            merged.put("_batch", batchMeta);
        }
        return merged;
    }
}
