package io.github.genkidoudou.web.knowledge.ingest.source;

import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.web.knowledge.constants.KbDocSourceType;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 手动录入来源：正文已归档为 {@code sys_file}（通常为 .md），优先 Tika 解析；无文件时无法入库。
 */
@Component
public class ManualDocumentSourceAdapter implements DocumentSourceAdapter {

    private final TikaDocumentLoader tikaDocumentLoader;

    public ManualDocumentSourceAdapter(TikaDocumentLoader tikaDocumentLoader) {
        this.tikaDocumentLoader = tikaDocumentLoader;
    }

    @Override
    public String sourceType() {
        return KbDocSourceType.MANUAL;
    }

    @Override
    public List<Document> load(DocumentSourceContext context) {
        if (context.getFile() != null && StrUtil.isNotBlank(context.getFile().getRelativePath())) {
            return tikaDocumentLoader.load(context.getFile());
        }
        throw new IllegalStateException("手动录入文档缺少归档文件，无法解析正文");
    }
}
