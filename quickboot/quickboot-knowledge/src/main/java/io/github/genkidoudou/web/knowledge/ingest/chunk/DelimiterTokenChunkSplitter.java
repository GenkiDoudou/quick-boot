package io.github.genkidoudou.web.knowledge.ingest.chunk;

import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.web.knowledge.constants.KbChunkDelimiter;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 自定义分段：先按分隔符切段落，再合并/切分至 Token 上限，并对块间施加 Token 重叠。
 * 超长段落使用 {@link TokenTextSplitter} 二次切分。
 */
@Component
public class DelimiterTokenChunkSplitter {

    private static final Pattern DOUBLE_NEWLINE_PATTERN = Pattern.compile("\\n\\s*\\n+");

    /**
     * 按自定义分隔符与 Token 参数分块。
     *
     * @param documents    待分块文档
     * @param delimiter    {@link KbChunkDelimiter#SINGLE_NEWLINE} 或 {@link KbChunkDelimiter#DOUBLE_NEWLINE}
     * @param chunkSize    单块 Token 上限
     * @param chunkOverlap 块间重叠 Token 数
     * @return 分块结果
     */
    public List<Document> split(List<Document> documents,
                                String delimiter,
                                int chunkSize,
                                int chunkOverlap) {
        if (documents == null || documents.isEmpty()) {
            return List.of();
        }
        int safeChunkSize = Math.max(1, chunkSize);
        int safeOverlap = Math.max(0, Math.min(chunkOverlap, safeChunkSize - 1));
        String effectiveDelimiter = StrUtil.blankToDefault(delimiter, KbChunkDelimiter.DOUBLE_NEWLINE);

        List<Document> result = new ArrayList<>();
        for (Document source : documents) {
            String text = source.getText();
            if (StrUtil.isBlank(text)) {
                continue;
            }
            Map<String, Object> metadata = source.getMetadata();
            List<String> mergedBlocks = mergeParagraphs(splitByDelimiter(text, effectiveDelimiter), safeChunkSize);
            List<String> overlapped = applyOverlap(mergedBlocks, safeOverlap);
            for (String block : overlapped) {
                if (StrUtil.isBlank(block)) {
                    continue;
                }
                result.add(Document.builder()
                    .text(block)
                    .metadata(metadata == null ? Map.of() : metadata)
                    .build());
            }
        }
        return result;
    }

    private List<String> splitByDelimiter(String text, String delimiter) {
        String[] parts;
        if (KbChunkDelimiter.SINGLE_NEWLINE.equals(delimiter)) {
            parts = text.split("\\n", -1);
        } else {
            parts = DOUBLE_NEWLINE_PATTERN.split(text, -1);
        }
        List<String> segments = new ArrayList<>(parts.length);
        for (String part : parts) {
            String trimmed = part == null ? "" : part.trim();
            if (!trimmed.isEmpty()) {
                segments.add(trimmed);
            }
        }
        return segments;
    }

    private List<String> mergeParagraphs(List<String> paragraphs, int chunkSize) {
        List<String> blocks = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int currentTokens = 0;

        for (String paragraph : paragraphs) {
            int paragraphTokens = estimateTokens(paragraph);
            if (paragraphTokens > chunkSize) {
                flushBlock(blocks, current);
                currentTokens = 0;
                blocks.addAll(splitOversized(paragraph, chunkSize));
                continue;
            }
            if (currentTokens > 0 && currentTokens + paragraphTokens > chunkSize) {
                flushBlock(blocks, current);
                current.append(paragraph);
                currentTokens = paragraphTokens;
            } else {
                if (current.length() > 0) {
                    current.append("\n\n");
                }
                current.append(paragraph);
                currentTokens += paragraphTokens;
            }
        }
        flushBlock(blocks, current);
        return blocks;
    }

    private void flushBlock(List<String> blocks, StringBuilder current) {
        if (current.length() == 0) {
            return;
        }
        blocks.add(current.toString().trim());
        current.setLength(0);
    }

    private List<String> splitOversized(String paragraph, int chunkSize) {
        TokenTextSplitter splitter = TokenTextSplitter.builder()
            .withChunkSize(chunkSize)
            .withMinChunkSizeChars(Math.min(350, chunkSize))
            .withMinChunkLengthToEmbed(5)
            .withMaxNumChunks(10_000)
            .withKeepSeparator(true)
            .build();
        List<Document> splits = splitter.apply(List.of(new Document(paragraph)));
        List<String> texts = new ArrayList<>(splits.size());
        for (Document split : splits) {
            if (StrUtil.isNotBlank(split.getText())) {
                texts.add(split.getText().trim());
            }
        }
        return texts;
    }

    private List<String> applyOverlap(List<String> blocks, int chunkOverlap) {
        if (blocks.isEmpty() || chunkOverlap <= 0) {
            return blocks;
        }
        List<String> result = new ArrayList<>(blocks.size());
        String previousTail = "";
        for (String block : blocks) {
            String merged = previousTail.isEmpty() ? block : previousTail + block;
            result.add(merged.trim());
            previousTail = tailForOverlap(block, chunkOverlap);
        }
        return result;
    }

    private String tailForOverlap(String text, int chunkOverlap) {
        if (StrUtil.isBlank(text) || chunkOverlap <= 0) {
            return "";
        }
        int overlapChars = chunkOverlap * 4;
        if (text.length() <= overlapChars) {
            return text;
        }
        return text.substring(text.length() - overlapChars);
    }

    /**
     * 以字符数 / 4 估算 Token 数，与 Spring AI 默认估算策略对齐。
     *
     * @param text 文本
     * @return 估算 Token 数（至少 1）
     */
    static int estimateTokens(String text) {
        if (StrUtil.isBlank(text)) {
            return 0;
        }
        return Math.max(1, text.length() / 4);
    }
}
