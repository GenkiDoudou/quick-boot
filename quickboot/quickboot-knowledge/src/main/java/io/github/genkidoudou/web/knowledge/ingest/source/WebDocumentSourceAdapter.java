package io.github.genkidoudou.web.knowledge.ingest.source;

import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.web.knowledge.constants.KbDocSourceType;
import io.github.genkidoudou.web.knowledge.ingest.web.WebContentFetcher;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 网页来源：优先读取已归档文件；若无归档则按 {@code sourceUrl} 实时抓取。
 */
@Component
public class WebDocumentSourceAdapter implements DocumentSourceAdapter {

    private final TikaDocumentLoader tikaDocumentLoader;
    private final WebContentFetcher webContentFetcher;

    public WebDocumentSourceAdapter(TikaDocumentLoader tikaDocumentLoader,
                                    WebContentFetcher webContentFetcher) {
        this.tikaDocumentLoader = tikaDocumentLoader;
        this.webContentFetcher = webContentFetcher;
    }

    @Override
    public String sourceType() {
        return KbDocSourceType.WEB;
    }

    @Override
    public List<Document> load(DocumentSourceContext context) {
        if (context.getFile() != null && StrUtil.isNotBlank(context.getFile().getRelativePath())) {
            return tikaDocumentLoader.load(context.getFile());
        }
        String url = context.getDocument().getSourceUrl();
        if (StrUtil.isBlank(url)) {
            throw new IllegalStateException("网页来源缺少 URL 与归档文件");
        }
        String content = webContentFetcher.fetch(url);
        if (StrUtil.isBlank(content)) {
            throw new IllegalStateException("网页抓取正文为空");
        }
        return List.of(new Document(content, Map.of("sourceUrl", url)));
    }
}
