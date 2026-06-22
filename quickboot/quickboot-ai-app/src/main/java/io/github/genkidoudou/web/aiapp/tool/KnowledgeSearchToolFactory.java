package io.github.genkidoudou.web.aiapp.tool;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import io.github.genkidoudou.web.knowledge.config.KnowledgeProperties;
import io.github.genkidoudou.web.knowledge.constants.KbSearchMode;
import io.github.genkidoudou.web.knowledge.dto.ChunkHitVo;
import io.github.genkidoudou.web.knowledge.dto.KnowledgeSearchBo;
import io.github.genkidoudou.web.knowledge.rag.KnowledgeSearchService;
import io.github.genkidoudou.web.aiapp.constants.AiAppConstants;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 知识库检索 Tool 工厂：注册固定名称 {@code search_knowledge}。
 */
@Component
public class KnowledgeSearchToolFactory {

    private static final String INPUT_SCHEMA = """
        {
          "type": "object",
          "properties": {
            "query": { "type": "string", "description": "检索问题或关键词" }
          },
          "required": ["query"]
        }
        """;

    private final KnowledgeSearchService searchService;
    private final KnowledgeProperties properties;

    public KnowledgeSearchToolFactory(KnowledgeSearchService searchService,
                                      KnowledgeProperties properties) {
        this.searchService = searchService;
        this.properties = properties;
    }

    /**
     * 为指定知识库 ID 列表创建 search_knowledge ToolCallback。
     *
     * @param kbIds 知识库 ID 列表
     * @return ToolCallback 数组；无 kbIds 时返回空数组
     */
    public ToolCallback[] create(List<Long> kbIds) {
        if (kbIds == null || kbIds.isEmpty()) {
            return new ToolCallback[0];
        }
        ToolDefinition definition = ToolDefinition.builder()
            .name(AiAppConstants.TOOL_SEARCH_KNOWLEDGE)
            .description("在绑定的知识库中检索与用户问题相关的文档片段")
            .inputSchema(INPUT_SCHEMA)
            .build();
        ToolCallback callback = new ToolCallback() {
            @Override
            public ToolDefinition getToolDefinition() {
                return definition;
            }

            @Override
            public String call(String toolInput) {
                String query = parseQuery(toolInput);
                if (StrUtil.isBlank(query)) {
                    return "检索词不能为空";
                }
                return searchFormattedContext(kbIds, query);
            }
        };
        return new ToolCallback[]{callback};
    }

    /**
     * 判断绑定知识库中是否存在与用户问题足够相关的检索命中（过滤混合检索弱匹配回落结果）。
     *
     * @param kbIds 知识库 ID 列表
     * @param query 检索词
     * @return 有相关命中为 true
     */
    public boolean hasRelevantHits(List<Long> kbIds, String query) {
        return !collectRelevantHits(kbIds, query).isEmpty();
    }

    /**
     * 在绑定知识库中检索并格式化为供 LLM 参考的上下文（非直接展示给终端用户）。
     *
     * @param kbIds 知识库 ID 列表
     * @param query 检索词
     * @return 格式化上下文；无命中时返回提示文案
     */
    public String searchFormattedContext(List<Long> kbIds, String query) {
        if (kbIds == null || kbIds.isEmpty() || StrUtil.isBlank(query)) {
            return "未在知识库中找到相关内容";
        }
        List<String> blocks = new ArrayList<>();
        for (ChunkHitVo hit : collectRelevantHits(kbIds, query)) {
            if (StrUtil.isBlank(hit.getContent())) {
                continue;
            }
            String source = StrUtil.blankToDefault(hit.getFileName(), "未知文档");
            if (hit.getPageNumber() != null) {
                source = source + " 第" + hit.getPageNumber() + "页";
            }
            blocks.add("【来源：" + source + "】\n" + hit.getContent().trim());
        }
        if (blocks.isEmpty()) {
            return "未在知识库中找到相关内容";
        }
        return "（以下为知识库检索片段，请据此用自然语言组织回答，勿原样粘贴全文）\n\n"
            + String.join("\n\n", blocks);
    }

    private List<ChunkHitVo> collectRelevantHits(List<Long> kbIds, String query) {
        List<ChunkHitVo> relevant = new ArrayList<>();
        if (kbIds == null || kbIds.isEmpty() || StrUtil.isBlank(query)) {
            return relevant;
        }
        double threshold = properties.getRag().getSimilarityThreshold();
        for (Long kbId : kbIds) {
            if (kbId == null) {
                continue;
            }
            KnowledgeSearchBo bo = new KnowledgeSearchBo();
            bo.setKbId(kbId);
            bo.setQuery(query);
            bo.setSearchMode(KbSearchMode.HYBRID);
            bo.setSaveHistory(false);
            List<ChunkHitVo> hits = searchService.search(bo);
            for (ChunkHitVo hit : hits) {
                if (isRelevantHit(hit, threshold)) {
                    relevant.add(hit);
                }
            }
        }
        return relevant;
    }

    /**
     * 与 {@link io.github.genkidoudou.web.knowledge.rag.KnowledgeSearchService} 混合检索过滤规则对齐。
     */
    private boolean isRelevantHit(ChunkHitVo hit, double threshold) {
        double vector = hit.getVectorScore() != null ? hit.getVectorScore() : 0.0;
        if (vector <= 0 && hit.getScore() != null && KbSearchMode.VECTOR.equals(hit.getSearchMode())) {
            vector = hit.getScore();
        }
        double keyword = hit.getKeywordScore() != null ? hit.getKeywordScore() : 0.0;
        if (vector >= threshold) {
            return true;
        }
        return keyword >= 0.5;
    }

    private String parseQuery(String toolInput) {
        if (StrUtil.isBlank(toolInput)) {
            return "";
        }
        try {
            Map<?, ?> map = JSONUtil.toBean(toolInput, Map.class);
            Object query = map.get("query");
            return query == null ? toolInput : String.valueOf(query);
        } catch (Exception ex) {
            return toolInput;
        }
    }
}
