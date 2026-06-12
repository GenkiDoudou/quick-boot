package io.github.genkidoudou.web.knowledge.support;

import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.web.knowledge.config.KnowledgeProperties;
import io.github.genkidoudou.web.knowledge.constants.KbChunkDelimiter;
import io.github.genkidoudou.web.knowledge.constants.KbSegmentMode;
import io.github.genkidoudou.web.knowledge.domain.KbDocument;
import io.github.genkidoudou.web.knowledge.domain.KbKnowledgeBase;
import io.github.genkidoudou.web.knowledge.dto.SegmentConfigBo;
import io.github.genkidoudou.web.knowledge.ingest.IngestSegmentConfig;
import org.springframework.stereotype.Component;

/**
 * 合并知识库默认与单次入库覆盖配置，写入 {@link KbDocument} 快照字段。
 * <p>
 * 入库流水线通过 {@link IngestSegmentConfig#fromDocumentSnapshot} 读取快照，重建索引时不随知识库后续变更而变。
 */
@Component
public class SegmentConfigResolver {

    private final KnowledgeProperties properties;

    public SegmentConfigResolver(KnowledgeProperties properties) {
        this.properties = properties;
    }

    /**
     * 将合并后的分段/预处理策略写入文档实体（入库前调用）。
     *
     * @param doc      待入库文档（将被修改快照字段）
     * @param kb       所属知识库默认配置
     * @param override 单次添加可选覆盖项，可为 null
     */
    public void applySnapshot(KbDocument doc, KbKnowledgeBase kb, SegmentConfigBo override) {
        String segmentMode = resolveSegmentMode(kb, override);
        int chunkSize = resolveChunkSize(kb, override);
        int chunkOverlap = resolveChunkOverlap(kb, override);
        String chunkDelimiter = resolveChunkDelimiter(kb, override);
        int normalizeWs = resolveFlag(kb != null ? kb.getPreprocessNormalizeWs() : null,
            override != null ? override.getPreprocessNormalizeWs() : null, true);
        int removeUrl = resolveFlag(kb != null ? kb.getPreprocessRemoveUrl() : null,
            override != null ? override.getPreprocessRemoveUrl() : null, false);
        int removeEmail = resolveFlag(kb != null ? kb.getPreprocessRemoveEmail() : null,
            override != null ? override.getPreprocessRemoveEmail() : null, false);

        doc.setSegmentMode(segmentMode);
        doc.setChunkSize(chunkSize);
        doc.setChunkOverlap(chunkOverlap);
        doc.setChunkDelimiter(chunkDelimiter);
        doc.setPreprocessNormalizeWs(normalizeWs);
        doc.setPreprocessRemoveUrl(removeUrl);
        doc.setPreprocessRemoveEmail(removeEmail);
    }

    private String resolveSegmentMode(KbKnowledgeBase kb, SegmentConfigBo override) {
        if (override != null && StrUtil.isNotBlank(override.getSegmentMode())) {
            validateSegmentMode(override.getSegmentMode());
            return override.getSegmentMode();
        }
        if (kb != null && StrUtil.isNotBlank(kb.getSegmentMode())) {
            return kb.getSegmentMode();
        }
        return KbSegmentMode.AUTO;
    }

    private int resolveChunkSize(KbKnowledgeBase kb, SegmentConfigBo override) {
        if (override != null && override.getChunkSize() != null && override.getChunkSize() > 0) {
            return override.getChunkSize();
        }
        if (kb != null && kb.getChunkSize() != null && kb.getChunkSize() > 0) {
            return kb.getChunkSize();
        }
        return properties.getIngest().getDefaultChunkSize();
    }

    private int resolveChunkOverlap(KbKnowledgeBase kb, SegmentConfigBo override) {
        if (override != null && override.getChunkOverlap() != null && override.getChunkOverlap() >= 0) {
            return override.getChunkOverlap();
        }
        if (kb != null && kb.getChunkOverlap() != null && kb.getChunkOverlap() >= 0) {
            return kb.getChunkOverlap();
        }
        return properties.getIngest().getDefaultChunkOverlap();
    }

    private String resolveChunkDelimiter(KbKnowledgeBase kb, SegmentConfigBo override) {
        if (override != null && StrUtil.isNotBlank(override.getChunkDelimiter())) {
            validateChunkDelimiter(override.getChunkDelimiter());
            return override.getChunkDelimiter();
        }
        if (kb != null && StrUtil.isNotBlank(kb.getChunkDelimiter())) {
            return kb.getChunkDelimiter();
        }
        return KbChunkDelimiter.DOUBLE_NEWLINE;
    }

    private static int resolveFlag(Integer kbFlag, Boolean overrideFlag, boolean defaultValue) {
        if (overrideFlag != null) {
            return overrideFlag ? 1 : 0;
        }
        if (kbFlag != null) {
            return kbFlag;
        }
        return defaultValue ? 1 : 0;
    }

    private static void validateSegmentMode(String mode) {
        if (!KbSegmentMode.AUTO.equals(mode) && !KbSegmentMode.CUSTOM.equals(mode)) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "无效的分段模式: " + mode);
        }
    }

    private static void validateChunkDelimiter(String delimiter) {
        if (!KbChunkDelimiter.SINGLE_NEWLINE.equals(delimiter)
            && !KbChunkDelimiter.DOUBLE_NEWLINE.equals(delimiter)) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "无效的分隔符: " + delimiter);
        }
    }
}
