package io.github.genkidoudou.web.knowledge.ingest.source;

import io.github.genkidoudou.web.knowledge.constants.KbDocSourceType;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 文件上传来源：从 {@code sys_file} 经 Tika 解析正文。
 */
@Component
public class FileDocumentSourceAdapter implements DocumentSourceAdapter {

    private final TikaDocumentLoader tikaDocumentLoader;

    public FileDocumentSourceAdapter(TikaDocumentLoader tikaDocumentLoader) {
        this.tikaDocumentLoader = tikaDocumentLoader;
    }

    @Override
    public String sourceType() {
        return KbDocSourceType.FILE;
    }

    @Override
    public List<Document> load(DocumentSourceContext context) {
        return tikaDocumentLoader.load(context.getFile());
    }
}
