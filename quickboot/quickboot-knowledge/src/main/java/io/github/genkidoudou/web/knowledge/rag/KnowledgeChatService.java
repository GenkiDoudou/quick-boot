package io.github.genkidoudou.web.knowledge.rag;

import io.github.genkidoudou.web.knowledge.dto.KnowledgeChatBo;
import io.github.genkidoudou.web.knowledge.dto.KnowledgeChatVo;
import org.springframework.stereotype.Service;

/**
 * RAG 问答门面，供 Controller 调用。
 */
@Service
public class KnowledgeChatService {

    private final RagService ragService;

    public KnowledgeChatService(RagService ragService) {
        this.ragService = ragService;
    }

    /**
     * 执行 RAG 问答。
     *
     * @param req 问答请求
     * @return 回答与引用
     */
    public KnowledgeChatVo chat(KnowledgeChatBo req) {
        return ragService.ask(req);
    }
}
