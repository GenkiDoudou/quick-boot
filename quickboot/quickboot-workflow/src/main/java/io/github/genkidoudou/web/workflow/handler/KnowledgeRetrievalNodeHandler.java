package io.github.genkidoudou.web.workflow.handler;

import cn.hutool.core.util.StrUtil;
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
        KnowledgeSearchBo req = new KnowledgeSearchBo();
        req.setKbId(kbId);
        req.setQuery(query);
        if (data.get("topK") instanceof Number topK) {
            req.setTopK(topK.intValue());
        }
        if (data.get("similarityThreshold") instanceof Number threshold) {
            req.setSimilarityThreshold(threshold.doubleValue());
        }
        try {
            List<ChunkHitVo> hits = searchService.search(req);
            List<Map<String, Object>> chunks = new ArrayList<>();
            List<Map<String, Object>> citations = new ArrayList<>();
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
                if (hit.getContent() != null) {
                    contextText.append(hit.getContent()).append("\n\n");
                }
            }
            Map<String, Object> outputs = new HashMap<>();
            outputs.put("chunks", chunks);
            outputs.put("citations", citations);
            outputs.put("contextText", contextText.toString().trim());
            return NodeResult.success(outputs);
        } catch (Exception ex) {
            return NodeResult.failed("知识检索失败: " + ex.getMessage());
        }
    }

    private Long resolveKbId(Map<String, Object> data, WorkflowContext context) {
        Object kbIdTemplate = data.get("kbId");
        if (kbIdTemplate != null) {
            Object resolved = templateRenderer.resolveObject(String.valueOf(kbIdTemplate), context);
            if (resolved instanceof Number number) {
                return number.longValue();
            }
            if (resolved instanceof String str && StrUtil.isNotBlank(str)) {
                try {
                    return Long.parseLong(str.trim());
                } catch (NumberFormatException ignored) {
                    // fall through
                }
            }
        }
        Object sysKbId = context.getSysVariables().get("kbId");
        if (sysKbId instanceof Number number) {
            return number.longValue();
        }
        return null;
    }
}
