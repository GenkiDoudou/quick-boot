package io.github.genkidoudou.web.aiapp.tool;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
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
import java.util.stream.Collectors;

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

    public KnowledgeSearchToolFactory(KnowledgeSearchService searchService) {
        this.searchService = searchService;
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
                List<String> chunks = new ArrayList<>();
                for (Long kbId : kbIds) {
                    if (kbId == null) {
                        continue;
                    }
                    KnowledgeSearchBo bo = new KnowledgeSearchBo();
                    bo.setKbId(kbId);
                    bo.setQuery(query);
                    bo.setSearchMode(KbSearchMode.VECTOR);
                    bo.setSaveHistory(false);
                    List<ChunkHitVo> hits = searchService.search(bo);
                    for (ChunkHitVo hit : hits) {
                        if (StrUtil.isNotBlank(hit.getContent())) {
                            chunks.add(hit.getContent());
                        }
                    }
                }
                if (chunks.isEmpty()) {
                    return "未在知识库中找到相关内容";
                }
                return chunks.stream().collect(Collectors.joining("\n\n"));
            }
        };
        return new ToolCallback[]{callback};
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
