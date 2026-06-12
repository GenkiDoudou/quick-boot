package io.github.genkidoudou.web.knowledge.support;

import cn.hutool.core.util.StrUtil;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

/**
 * PGVector 向量删除辅助，按 metadata 过滤表达式清理向量。
 */
@Component
public class KnowledgeVectorSupport {

    private final VectorStore vectorStore;

    public KnowledgeVectorSupport(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    /**
     * 按知识库 ID 删除该库下全部向量。
     *
     * @param kbId 知识库 ID
     */
    public void deleteByKbId(Long kbId) {
        if (kbId == null) {
            return;
        }
        vectorStore.delete("kbId == '" + kbId + "'");
    }

    /**
     * 按文档 ID 删除该文档的全部向量（重索引/删文档前调用）。
     *
     * @param docId 文档 ID
     */
    public void deleteByDocId(Long docId) {
        if (docId == null) {
            return;
        }
        vectorStore.delete("docId == '" + docId + "'");
    }

    /**
     * 按向量 Document id 删除单条向量（分块编辑/禁用时使用）。
     *
     * @param vectorId PGVector 中的 id
     */
    public void deleteByVectorId(String vectorId) {
        if (StrUtil.isBlank(vectorId)) {
            return;
        }
        vectorStore.delete(java.util.List.of(vectorId.trim()));
    }
}
