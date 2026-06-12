package io.github.genkidoudou.web.knowledge.ingest;

import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.web.knowledge.config.KnowledgeProperties;
import io.github.genkidoudou.web.knowledge.constants.KbChunkDelimiter;
import io.github.genkidoudou.web.knowledge.constants.KbSegmentMode;
import io.github.genkidoudou.web.knowledge.domain.KbDocument;
import io.github.genkidoudou.web.knowledge.domain.KbKnowledgeBase;

/**
 * 文档入库分段与预处理快照：优先读 {@link KbDocument} 快照字段，缺省回退知识库与全局默认。
 */
public final class IngestSegmentConfig {

    private final String segmentMode;
    private final int chunkSize;
    private final int chunkOverlap;
    private final String chunkDelimiter;
    private final boolean preprocessNormalizeWs;
    private final boolean preprocessRemoveUrl;
    private final boolean preprocessRemoveEmail;

    private IngestSegmentConfig(String segmentMode,
                                int chunkSize,
                                int chunkOverlap,
                                String chunkDelimiter,
                                boolean preprocessNormalizeWs,
                                boolean preprocessRemoveUrl,
                                boolean preprocessRemoveEmail) {
        this.segmentMode = segmentMode;
        this.chunkSize = chunkSize;
        this.chunkOverlap = chunkOverlap;
        this.chunkDelimiter = chunkDelimiter;
        this.preprocessNormalizeWs = preprocessNormalizeWs;
        this.preprocessRemoveUrl = preprocessRemoveUrl;
        this.preprocessRemoveEmail = preprocessRemoveEmail;
    }

    /**
     * 从文档快照（及知识库默认）解析入库策略；重建索引时仅依赖文档快照，不读取知识库后续变更。
     *
     * @param doc   文档实体（含快照字段）
     * @param kb    所属知识库（仅快照缺省时回退）
     * @param props 模块全局默认
     * @return 解析后的策略
     */
    public static IngestSegmentConfig fromDocumentSnapshot(KbDocument doc,
                                                           KbKnowledgeBase kb,
                                                           KnowledgeProperties props) {
        KnowledgeProperties.Ingest ingestDefaults = props.getIngest();
        String segmentMode = blankToDefault(doc.getSegmentMode(),
            kb != null ? kb.getSegmentMode() : null, KbSegmentMode.AUTO);
        int chunkSize = firstPositive(doc.getChunkSize(), kb != null ? kb.getChunkSize() : null,
            ingestDefaults.getDefaultChunkSize());
        int chunkOverlap = firstPositive(doc.getChunkOverlap(), kb != null ? kb.getChunkOverlap() : null,
            ingestDefaults.getDefaultChunkOverlap());
        String chunkDelimiter = blankToDefault(doc.getChunkDelimiter(),
            kb != null ? kb.getChunkDelimiter() : null, KbChunkDelimiter.DOUBLE_NEWLINE);
        boolean normalizeWs = flagEnabled(doc.getPreprocessNormalizeWs(),
            kb != null ? kb.getPreprocessNormalizeWs() : null, true);
        boolean removeUrl = flagEnabled(doc.getPreprocessRemoveUrl(),
            kb != null ? kb.getPreprocessRemoveUrl() : null, false);
        boolean removeEmail = flagEnabled(doc.getPreprocessRemoveEmail(),
            kb != null ? kb.getPreprocessRemoveEmail() : null, false);
        return new IngestSegmentConfig(segmentMode, chunkSize, chunkOverlap, chunkDelimiter,
            normalizeWs, removeUrl, removeEmail);
    }

    private static int firstPositive(Integer primary, Integer secondary, int fallback) {
        if (primary != null && primary > 0) {
            return primary;
        }
        if (secondary != null && secondary > 0) {
            return secondary;
        }
        return Math.max(1, fallback);
    }

    private static boolean flagEnabled(Integer primary, Integer secondary, boolean defaultValue) {
        if (primary != null) {
            return primary != 0;
        }
        if (secondary != null) {
            return secondary != 0;
        }
        return defaultValue;
    }

    private static String blankToDefault(String primary, String secondary, String fallback) {
        if (StrUtil.isNotBlank(primary)) {
            return primary;
        }
        if (StrUtil.isNotBlank(secondary)) {
            return secondary;
        }
        return fallback;
    }

    public String getSegmentMode() {
        return segmentMode;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public int getChunkOverlap() {
        return chunkOverlap;
    }

    public String getChunkDelimiter() {
        return chunkDelimiter;
    }

    public boolean isPreprocessNormalizeWs() {
        return preprocessNormalizeWs;
    }

    public boolean isPreprocessRemoveUrl() {
        return preprocessRemoveUrl;
    }

    public boolean isPreprocessRemoveEmail() {
        return preprocessRemoveEmail;
    }
}
