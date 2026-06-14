package io.github.genkidoudou.web.knowledge.rag;

import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.web.knowledge.config.KnowledgeProperties;
import io.github.genkidoudou.web.knowledge.constants.KbSearchMode;
import io.github.genkidoudou.web.knowledge.dto.CitationVo;
import io.github.genkidoudou.web.knowledge.dto.ChunkHitVo;
import io.github.genkidoudou.web.knowledge.dto.KnowledgeChatBo;
import io.github.genkidoudou.web.knowledge.dto.KnowledgeChatVo;
import io.github.genkidoudou.web.knowledge.dto.KnowledgeSearchBo;
import io.github.genkidoudou.web.knowledge.mcp.runtime.McpToolCallbackProvider;
import io.github.genkidoudou.web.knowledge.mcp.runtime.McpToolUsageTracker;
import io.github.genkidoudou.web.knowledge.mcp.runtime.McpTrackingToolCallbacks;
import io.github.genkidoudou.web.knowledge.service.KbKnowledgeBaseMcpService;
import io.github.genkidoudou.web.knowledge.support.KnowledgeAiGuard;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * RAG 问答服务：检索片段 + 本地 LLM 生成带引用的回答。
 */
@Service
public class RagService {

    private static final String SYSTEM_PROMPT = """
        你是企业知识库问答助手。请优先依据提供的检索上下文回答用户问题。
        若上下文不足以回答，请明确说明「未在知识库中找到相关内容」，不要编造。
        回答应简洁准确，并在适当时提及信息来源文档名称。
        """;

    private static final String MCP_TOOL_HINT = """
        
        若已提供外部 MCP 工具且与问题相关，可调用工具获取实时数据；不得捏造引用或工具结果。
        """;

    private final KnowledgeProperties properties;
    private final KnowledgeAiGuard aiGuard;
    private final KnowledgeSearchService searchService;
    private final KbKnowledgeBaseMcpService mcpBindingService;
    private final ObjectProvider<McpToolCallbackProvider> mcpToolCallbackProvider;
    private final ObjectProvider<McpToolUsageTracker> mcpToolUsageTracker;

    public RagService(KnowledgeProperties properties,
                      KnowledgeAiGuard aiGuard,
                      KnowledgeSearchService searchService,
                      KbKnowledgeBaseMcpService mcpBindingService,
                      ObjectProvider<McpToolCallbackProvider> mcpToolCallbackProvider,
                      ObjectProvider<McpToolUsageTracker> mcpToolUsageTracker) {
        this.properties = properties;
        this.aiGuard = aiGuard;
        this.searchService = searchService;
        this.mcpBindingService = mcpBindingService;
        this.mcpToolCallbackProvider = mcpToolCallbackProvider;
        this.mcpToolUsageTracker = mcpToolUsageTracker;
    }

    /**
     * 基于 {@link KnowledgeSearchService} 向量检索 + 本地 LLM 生成回答，与工作流知识库检索节点共用同一检索链路。
     *
     * @param req 问答请求
     * @return 回答与引用列表
     */
    public KnowledgeChatVo ask(KnowledgeChatBo req) {
        aiGuard.requireRagModels(req.getKbId());

        int topK = req.getTopK() != null ? req.getTopK() : properties.getRag().getTopK();
        double threshold = req.getSimilarityThreshold() != null
            ? req.getSimilarityThreshold()
            : properties.getRag().getSimilarityThreshold();

        KnowledgeSearchBo searchBo = new KnowledgeSearchBo();
        searchBo.setKbId(req.getKbId());
        searchBo.setQuery(req.getQuestion());
        searchBo.setTopK(topK);
        searchBo.setSimilarityThreshold(threshold);
        searchBo.setSearchMode(KbSearchMode.VECTOR);
        searchBo.setSaveHistory(false);
        List<ChunkHitVo> hits = searchService.search(searchBo);

        String context = hits.stream()
            .map(ChunkHitVo::getContent)
            .filter(StrUtil::isNotBlank)
            .collect(Collectors.joining("\n\n"));

        boolean useMcpTools = req.getUseMcpTools() == null || req.getUseMcpTools();
        McpToolUsageTracker tracker = mcpToolUsageTracker.getIfAvailable();
        if (tracker != null) {
            tracker.clear();
        }

        String systemPrompt = SYSTEM_PROMPT;
        ChatClient.Builder clientBuilder = ChatClient.builder(aiGuard.requireChatModelInstance(req.getKbId()));

        if (useMcpTools && properties.getMcp().isEnabled()) {
            McpToolCallbackProvider toolProvider = mcpToolCallbackProvider.getIfAvailable();
            if (toolProvider != null && tracker != null) {
                List<Long> mcpIds = mcpBindingService.listEnabledMcpIdsByKbId(req.getKbId());
                ToolCallback[] callbacks = McpTrackingToolCallbacks.wrap(
                    toolProvider.getToolCallbacks(mcpIds), tracker);
                if (callbacks.length > 0) {
                    systemPrompt = SYSTEM_PROMPT + MCP_TOOL_HINT;
                    clientBuilder.defaultToolCallbacks(callbacks);
                }
            }
        }

        ChatClient chatClient = clientBuilder.defaultSystem(systemPrompt).build();

        String userPrompt = StrUtil.isNotBlank(context)
            ? "参考以下检索上下文回答用户问题。若上下文不足以回答，请明确说明「未在知识库中找到相关内容」。\n\n"
            + "【检索上下文】\n" + context + "\n\n【用户问题】\n" + req.getQuestion()
            : req.getQuestion();

        String answer = chatClient.prompt()
            .user(userPrompt)
            .call()
            .content();

        KnowledgeChatVo vo = new KnowledgeChatVo();
        vo.setAnswer(StrUtil.blankToDefault(answer, "未生成有效回答"));
        vo.setCitations(hits.stream().map(this::toCitation).collect(Collectors.toList()));
        if (tracker != null) {
            vo.setMcpToolsUsed(tracker.drain());
        }
        return vo;
    }

    private CitationVo toCitation(ChunkHitVo hit) {
        CitationVo citation = new CitationVo();
        citation.setDocId(hit.getDocId());
        citation.setChunkId(hit.getChunkId());
        citation.setFileName(hit.getFileName());
        citation.setContentPreview(truncate(hit.getContent()));
        citation.setScore(hit.getScore());
        citation.setPageNumber(hit.getPageNumber());
        return citation;
    }

    private static String truncate(String text) {
        if (text == null) {
            return "";
        }
        String normalized = text.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= 500) {
            return normalized;
        }
        return normalized.substring(0, 500);
    }
}
