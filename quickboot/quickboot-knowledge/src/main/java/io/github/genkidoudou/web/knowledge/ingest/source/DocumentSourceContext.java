package io.github.genkidoudou.web.knowledge.ingest.source;

import io.github.genkidoudou.web.knowledge.domain.KbDocument;
import io.github.genkidoudou.web.knowledge.domain.KbKnowledgeBase;
import io.github.genkidoudou.web.system.file.domain.SysFile;

/**
 * 文档来源适配上下文：聚合入库所需的文档、知识库与文件元数据。
 */
public class DocumentSourceContext {

    private final KbDocument document;
    private final KbKnowledgeBase knowledgeBase;
    private final SysFile file;

    /**
     * @param document      待入库文档
     * @param knowledgeBase 所属知识库
     * @param file          关联 {@code sys_file}，部分来源可能为空
     */
    public DocumentSourceContext(KbDocument document, KbKnowledgeBase knowledgeBase, SysFile file) {
        this.document = document;
        this.knowledgeBase = knowledgeBase;
        this.file = file;
    }

    public KbDocument getDocument() {
        return document;
    }

    public KbKnowledgeBase getKnowledgeBase() {
        return knowledgeBase;
    }

    public SysFile getFile() {
        return file;
    }
}
