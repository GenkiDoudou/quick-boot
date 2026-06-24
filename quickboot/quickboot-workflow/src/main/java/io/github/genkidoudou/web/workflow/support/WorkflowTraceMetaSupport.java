package io.github.genkidoudou.web.workflow.support;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 步骤 Trace 元数据（{@code inputs._meta}）构建辅助。
 */
public final class WorkflowTraceMetaSupport {

    private WorkflowTraceMetaSupport() {
    }

    /**
     * 将元数据写入 Trace 入参的 {@code _meta} 字段。
     *
     * @param traceInputs Trace 入参（可变）
     * @param nodeKind    节点种类标识，如 llm / classifier
     * @param tokenUsage  Token 用量，可为 null
     * @param extras      其它展示字段
     */
    public static void enrichTraceInputs(Map<String, Object> traceInputs, String nodeKind,
                                         Map<String, Object> tokenUsage, Map<String, Object> extras) {
        if (traceInputs == null) {
            return;
        }
        Map<String, Object> meta = new LinkedHashMap<>();
        if (nodeKind != null && !nodeKind.isBlank()) {
            meta.put("nodeKind", nodeKind);
        }
        if (tokenUsage != null && !tokenUsage.isEmpty()) {
            meta.put("tokenUsage", tokenUsage);
        }
        if (extras != null && !extras.isEmpty()) {
            meta.putAll(extras);
        }
        if (!meta.isEmpty()) {
            traceInputs.put("_meta", meta);
        }
    }
}
