package io.github.genkidoudou.web.knowledge.ingest.preprocess;

import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 入库前文本清洗：按文档快照开关顺序执行空白归一化、URL 与邮箱剔除。
 */
@Component
public class TextPreprocessor {

    private static final Pattern URL_PATTERN = Pattern.compile(
        "(?i)(https?://\\S+|www\\.\\S+)",
        Pattern.MULTILINE);

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}",
        Pattern.MULTILINE);

    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

    /**
     * 按开关对 Spring AI 文档列表逐条清洗文本，保留元数据。
     *
     * @param documents           原始文档
     * @param normalizeWs         是否将连续空白归一化为单空格
     * @param removeUrl           是否删除 URL
     * @param removeEmail         是否删除电子邮箱
     * @return 清洗后的文档列表（跳过清洗后为空的条目）
     */
    public List<Document> preprocess(List<Document> documents,
                                     boolean normalizeWs,
                                     boolean removeUrl,
                                     boolean removeEmail) {
        if (documents == null || documents.isEmpty()) {
            return List.of();
        }
        List<Document> result = new ArrayList<>(documents.size());
        for (Document document : documents) {
            String text = document.getText();
            if (text == null) {
                continue;
            }
            String processed = preprocessText(text, normalizeWs, removeUrl, removeEmail);
            if (processed.isBlank()) {
                continue;
            }
            Map<String, Object> metadata = document.getMetadata();
            result.add(Document.builder()
                .text(processed)
                .metadata(metadata == null ? Map.of() : metadata)
                .build());
        }
        return result;
    }

    /**
     * 对单段文本执行预处理流水线。
     *
     * @param text          原始文本
     * @param normalizeWs   是否归一化空白
     * @param removeUrl     是否删除 URL
     * @param removeEmail   是否删除邮箱
     * @return 清洗后的文本（可能为空串）
     */
    public String preprocessText(String text, boolean normalizeWs, boolean removeUrl, boolean removeEmail) {
        if (text == null) {
            return "";
        }
        String current = text;
        if (normalizeWs) {
            current = WHITESPACE_PATTERN.matcher(current).replaceAll(" ").trim();
        }
        if (removeUrl) {
            current = URL_PATTERN.matcher(current).replaceAll(" ").trim();
        }
        if (removeEmail) {
            current = EMAIL_PATTERN.matcher(current).replaceAll(" ").trim();
        }
        if (normalizeWs && (removeUrl || removeEmail)) {
            current = WHITESPACE_PATTERN.matcher(current).replaceAll(" ").trim();
        }
        return current;
    }
}
