package io.github.genkidoudou.web.knowledge.ingest.chunk;

import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.web.knowledge.constants.KbSegmentMode;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 分段策略门面：AUTO 走 {@link TokenTextSplitter}；CUSTOM 走 {@link DelimiterTokenChunkSplitter}。
 */
@Component
public class ChunkStrategy {

    private final DelimiterTokenChunkSplitter delimiterSplitter;

    public ChunkStrategy(DelimiterTokenChunkSplitter delimiterSplitter) {
        this.delimiterSplitter = delimiterSplitter;
    }

    /**
     * 按分段模式对文档列表分块。
     *
     * @param documents     预处理后的文档
     * @param segmentMode   {@link KbSegmentMode#AUTO} 或 {@link KbSegmentMode#CUSTOM}
     * @param chunkSize     Token 上限
     * @param chunkOverlap  Token 重叠
     * @param chunkDelimiter 自定义分隔符（仅 CUSTOM 生效）
     * @return 分块结果
     */
    public List<Document> chunk(List<Document> documents,
                              String segmentMode,
                              int chunkSize,
                              int chunkOverlap,
                              String chunkDelimiter) {
        if (documents == null || documents.isEmpty()) {
            return List.of();
        }
        if (KbSegmentMode.CUSTOM.equals(segmentMode)) {
            return delimiterSplitter.split(documents, chunkDelimiter, chunkSize, chunkOverlap);
        }
        return autoSplit(documents, chunkSize, chunkOverlap);
    }

    private List<Document> autoSplit(List<Document> documents, int chunkSize, int chunkOverlap) {
        int safeChunkSize = Math.max(1, chunkSize);
        TokenTextSplitter splitter = TokenTextSplitter.builder()
            .withChunkSize(safeChunkSize)
            .withMinChunkSizeChars(Math.min(350, safeChunkSize))
            .withMinChunkLengthToEmbed(5)
            .withMaxNumChunks(10_000)
            .withKeepSeparator(true)
            .build();
        List<Document> chunks = splitter.apply(documents);
        if (chunkOverlap <= 0 || chunks.size() <= 1) {
            return chunks;
        }
        return applyAutoOverlap(chunks, chunkOverlap);
    }

    private List<Document> applyAutoOverlap(List<Document> chunks, int chunkOverlap) {
        String previousTail = "";
        List<Document> result = new java.util.ArrayList<>(chunks.size());
        for (Document chunk : chunks) {
            String text = chunk.getText();
            if (StrUtil.isBlank(text)) {
                continue;
            }
            String merged = previousTail.isEmpty() ? text : previousTail + text;
            result.add(Document.builder()
                .text(merged.trim())
                .metadata(chunk.getMetadata())
                .build());
            previousTail = tailForOverlap(text, chunkOverlap);
        }
        return result;
    }

    private String tailForOverlap(String text, int chunkOverlap) {
        int overlapChars = chunkOverlap * 4;
        if (text.length() <= overlapChars) {
            return text;
        }
        return text.substring(text.length() - overlapChars);
    }
}
