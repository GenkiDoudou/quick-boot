package io.github.genkidoudou.web.workflow.handler;

import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.web.knowledge.constants.KbSearchMode;
import io.github.genkidoudou.web.knowledge.dto.ChunkHitVo;
import io.github.genkidoudou.web.knowledge.dto.KnowledgeSearchBo;
import io.github.genkidoudou.web.knowledge.rag.KnowledgeSearchService;
import io.github.genkidoudou.web.workflow.constants.WfNodeType;
import io.github.genkidoudou.web.workflow.dto.GraphNodeDto;
import io.github.genkidoudou.web.workflow.engine.NodeHandler;
import io.github.genkidoudou.web.workflow.engine.NodeResult;
import io.github.genkidoudou.web.workflow.engine.TemplateRenderer;
import io.github.genkidoudou.web.workflow.engine.WorkflowContext;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Knowledge Retrieval 节点：复用 {@link KnowledgeSearchService} 进行语义检索并拼接 contextText。
 */
@Component
public class KnowledgeRetrievalNodeHandler implements NodeHandler {

    private final ObjectProvider<KnowledgeSearchService> searchServiceProvider;
    private final TemplateRenderer templateRenderer;

    public KnowledgeRetrievalNodeHandler(ObjectProvider<KnowledgeSearchService> searchServiceProvider,
                                         TemplateRenderer templateRenderer) {
        this.searchServiceProvider = searchServiceProvider;
        this.templateRenderer = templateRenderer;
    }

    @Override
    public String type() {
        return WfNodeType.KNOWLEDGE_RETRIEVAL;
    }

    @Override
    public NodeResult execute(GraphNodeDto node, WorkflowContext context) {
        KnowledgeSearchService searchService = searchServiceProvider.getIfAvailable();
        if (searchService == null) {
            return NodeResult.failed("知识库模块未启用，无法执行 knowledge-retrieval 节点");
        }
        Map<String, Object> data = node.getData() == null ? Map.of() : node.getData();
        Long kbId = resolveKbId(data, context);
        if (kbId == null) {
            return NodeResult.failed("知识库 ID 不能为空");
        }
        String query = templateRenderer.render(String.valueOf(data.getOrDefault("query", "")), context);
        if (StrUtil.isBlank(query)) {
            return NodeResult.failed("检索 query 不能为空");
        }
        String searchMode = data.get("searchMode") instanceof String mode && StrUtil.isNotBlank(mode)
            ? mode.trim()
            : KbSearchMode.VECTOR;
        KnowledgeSearchBo req = new KnowledgeSearchBo();
        req.setKbId(kbId);
        req.setQuery(query);
        if (data.get("topK") instanceof Number topK) {
            req.setTopK(topK.intValue());
        }
        if (data.get("similarityThreshold") instanceof Number threshold) {
            req.setSimilarityThreshold(threshold.doubleValue());
        }
        req.setSearchMode(searchMode);
        Object saveHistoryFlag = data.get("saveHistory");
        if (saveHistoryFlag instanceof Boolean saveHistory) {
            req.setSaveHistory(saveHistory);
        } else {
            req.setSaveHistory(false);
        }
        try {
            List<ChunkHitVo> hits = searchService.search(req);
            List<Map<String, Object>> chunks = new ArrayList<>();
            List<Map<String, Object>> citations = new ArrayList<>();
            List<Map<String, Object>> outputList = new ArrayList<>();
            StringBuilder contextText = new StringBuilder();
            for (ChunkHitVo hit : hits) {
                Map<String, Object> chunk = new HashMap<>();
                chunk.put("content", hit.getContent());
                chunk.put("score", hit.getScore());
                chunk.put("docId", hit.getDocId());
                chunk.put("chunkId", hit.getChunkId());
                chunks.add(chunk);
                Map<String, Object> citation = new HashMap<>();
                citation.put("docId", hit.getDocId());
                citation.put("chunkId", hit.getChunkId());
                citation.put("fileName", hit.getFileName());
                citation.put("score", hit.getScore());
                citations.add(citation);
                Map<String, Object> outputItem = new HashMap<>();
                outputItem.put("output", hit.getContent());
                outputItem.put("documentId", hit.getDocId() != null ? String.valueOf(hit.getDocId()) : "");
                outputItem.put("chunkId", hit.getChunkId());
                outputItem.put("score", hit.getScore());
                outputItem.put("fileName", hit.getFileName());
                outputList.add(outputItem);
                if (hit.getContent() != null) {
                    contextText.append(hit.getContent()).append("\n\n");
                }
            }
            Map<String, Object> outputs = new HashMap<>();
            outputs.put("outputList", outputList);
            outputs.put("chunks", chunks);
            outputs.put("citations", citations);
            outputs.put("contextText", contextText.toString().trim());
            Map<String, Object> traceInputs = new HashMap<>();
            traceInputs.put("kbId", kbId);
            traceInputs.put("query", query);
            traceInputs.put("searchMode", searchMode);
            traceInputs.put("topK", req.getTopK());
            traceInputs.put("similarityThreshold", req.getSimilarityThreshold());
            return NodeResult.successWithTrace(outputs, traceInputs);
        } catch (Exception ex) {
            return NodeResult.failed("知识检索失败: " + ex.getMessage());
        }
    }

    /**
     * 解析知识库 ID：支持设计器直接选择的数字 ID、模板引用（如 {@code {{sys.kbId}}}）及 sys 注入。
     */
    private Long resolveKbId(Map<String, Object> data, WorkflowContext context) {
        Object kbIdRaw = data.get("kbId");
        if (kbIdRaw == null) {
            return resolveSysKbId(context);
        }
        if (kbIdRaw instanceof Number number) {
            return number.longValue();
        }
        String kbIdStr = String.valueOf(kbIdRaw).trim();
        if (StrUtil.isBlank(kbIdStr)) {
            return resolveSysKbId(context);
        }
        // 设计器下拉选择的知识库为纯数字字符串，不走模板解析
        if (!kbIdStr.contains("{{") && kbIdStr.matches("\\d+")) {
            try {
                return Long.parseLong(kbIdStr);
            } catch (NumberFormatException ignored) {
                // fall through
            }
        }
        Object resolved = templateRenderer.resolveObject(kbIdStr, context);
        Long fromTemplate = toLong(resolved);
        if (fromTemplate != null) {
            return fromTemplate;
        }
        return resolveSysKbId(context);
    }

    private Long resolveSysKbId(WorkflowContext context) {
        return toLong(context.getSysVariables().get("kbId"));
    }

    private Long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String str && StrUtil.isNotBlank(str)) {
            try {
                return Long.parseLong(str.trim());
            } catch (NumberFormatException ignored) {
                // ignore
            }
        }
        return null;
    }
}
