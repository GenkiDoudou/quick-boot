package io.github.genkidoudou.web.knowledge.ingest.source;

import org.springframework.ai.document.Document;

import java.util.List;

/**
 * 文档来源适配器：将不同来源解析为 Spring AI {@link Document} 列表。
 */
public interface DocumentSourceAdapter {

    /**
     * 本适配器处理的来源类型常量，与 {@code kb_document.source_type} 一致。
     *
     * @return 来源类型
     */
    String sourceType();

    /**
     * 加载并解析文档正文。
     *
     * @param context 入库上下文
     * @return 解析结果，不可为空列表
     * @throws Exception 解析或 IO 失败
     */
    List<Document> load(DocumentSourceContext context) throws Exception;
}
