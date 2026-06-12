package io.github.genkidoudou.web.knowledge.ingest;

import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.web.knowledge.config.KnowledgeProperties;
import io.github.genkidoudou.web.knowledge.constants.KbDocSourceType;
import io.github.genkidoudou.web.knowledge.constants.KnowledgeConstants;
import io.github.genkidoudou.web.knowledge.domain.KbDocLibraryFile;
import io.github.genkidoudou.web.knowledge.domain.KbDocument;
import io.github.genkidoudou.web.knowledge.domain.KbKnowledgeBase;
import io.github.genkidoudou.web.knowledge.dto.SegmentConfigBo;
import io.github.genkidoudou.web.knowledge.dto.SegmentPreviewBo;
import io.github.genkidoudou.web.knowledge.dto.SegmentPreviewItemVo;
import io.github.genkidoudou.web.knowledge.dto.SegmentPreviewVo;
import io.github.genkidoudou.web.knowledge.ingest.chunk.ChunkStrategy;
import io.github.genkidoudou.web.knowledge.ingest.preprocess.TextPreprocessor;
import io.github.genkidoudou.web.knowledge.ingest.source.TikaDocumentLoader;
import io.github.genkidoudou.web.knowledge.ingest.web.WebContentFetcher;
import io.github.genkidoudou.web.knowledge.mapper.KbKnowledgeBaseMapper;
import io.github.genkidoudou.web.knowledge.service.KbDocLibraryFileService;
import io.github.genkidoudou.web.knowledge.support.SegmentConfigResolver;
import io.github.genkidoudou.web.system.file.domain.SysFile;
import io.github.genkidoudou.web.system.file.mapper.SysFileMapper;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 入库前分段预览：解析来源 → 预处理 → 分块，不写库、不 embedding。
 */
@Service
public class SegmentPreviewService {

    private static final int PREVIEW_DISPLAY_LIMIT = 200;

    private final KbKnowledgeBaseMapper knowledgeBaseMapper;
    private final SegmentConfigResolver segmentConfigResolver;
    private final KnowledgeProperties knowledgeProperties;
    private final TextPreprocessor textPreprocessor;
    private final ChunkStrategy chunkStrategy;
    private final TikaDocumentLoader tikaDocumentLoader;
    private final WebContentFetcher webContentFetcher;
    private final KbDocLibraryFileService libraryFileService;
    private final SysFileMapper sysFileMapper;

    public SegmentPreviewService(KbKnowledgeBaseMapper knowledgeBaseMapper,
                                 SegmentConfigResolver segmentConfigResolver,
                                 KnowledgeProperties knowledgeProperties,
                                 TextPreprocessor textPreprocessor,
                                 ChunkStrategy chunkStrategy,
                                 TikaDocumentLoader tikaDocumentLoader,
                                 WebContentFetcher webContentFetcher,
                                 KbDocLibraryFileService libraryFileService,
                                 SysFileMapper sysFileMapper) {
        this.knowledgeBaseMapper = knowledgeBaseMapper;
        this.segmentConfigResolver = segmentConfigResolver;
        this.knowledgeProperties = knowledgeProperties;
        this.textPreprocessor = textPreprocessor;
        this.chunkStrategy = chunkStrategy;
        this.tikaDocumentLoader = tikaDocumentLoader;
        this.webContentFetcher = webContentFetcher;
        this.libraryFileService = libraryFileService;
        this.sysFileMapper = sysFileMapper;
    }

    /**
     * JSON 来源预览（手动 / 网页 / 文档库）。
     */
    public SegmentPreviewVo preview(SegmentPreviewBo req) {
        KbKnowledgeBase kb = requireKb(req.getKbId());
        IngestSegmentConfig segmentConfig = resolveConfig(kb, req.getSegmentConfig());
        List<Document> parsed = loadParsedDocuments(req);
        return buildPreview(parsed, segmentConfig);
    }

    /**
     * 文件上传来源预览。
     */
    public SegmentPreviewVo previewFile(Long kbId, MultipartFile file, SegmentConfigBo segmentConfig) {
        if (file == null || file.isEmpty()) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "上传文件不能为空");
        }
        KbKnowledgeBase kb = requireKb(kbId);
        IngestSegmentConfig config = resolveConfig(kb, segmentConfig);
        try {
            List<Document> parsed = tikaDocumentLoader.loadFromBytes(
                file.getBytes(), file.getOriginalFilename());
            return buildPreview(parsed, config);
        } catch (WarningException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "文件解析失败：" + ex.getMessage());
        }
    }

    private List<Document> loadParsedDocuments(SegmentPreviewBo req) {
        String sourceType = req.getSourceType();
        if (KbDocSourceType.MANUAL.equals(sourceType)) {
            if (StrUtil.isBlank(req.getContent())) {
                throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "正文不能为空");
            }
            byte[] bytes = req.getContent().getBytes(StandardCharsets.UTF_8);
            if (bytes.length > KnowledgeConstants.MANUAL_CONTENT_MAX_BYTES) {
                throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "正文长度不能超过 512KB");
            }
            return tikaDocumentLoader.loadFromText(req.getContent());
        }
        if (KbDocSourceType.WEB.equals(sourceType)) {
            if (StrUtil.isBlank(req.getUrl())) {
                throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "URL 不能为空");
            }
            try {
                String content = webContentFetcher.fetch(req.getUrl().trim());
                if (StrUtil.isBlank(content)) {
                    throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "网页正文为空");
                }
                return tikaDocumentLoader.loadFromText(content);
            } catch (WarningException ex) {
                throw ex;
            } catch (RuntimeException ex) {
                String msg = StrUtil.blankToDefault(ex.getMessage(), "网页抓取失败");
                throw new WarningException(ErrorCodes.Biz.KNOWLEDGE_STATE_NOT_ALLOWED, msg);
            }
        }
        if (KbDocSourceType.LIBRARY.equals(sourceType)) {
            if (req.getLibFileId() == null) {
                throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "文档库文件 ID 不能为空");
            }
            KbDocLibraryFile libraryFile = libraryFileService.getById(req.getLibFileId());
            if (libraryFile == null) {
                throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "文档库文件不存在");
            }
            SysFile sysFile = sysFileMapper.selectById(libraryFile.getFileId());
            return tikaDocumentLoader.load(sysFile);
        }
        throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "不支持的来源类型: " + sourceType);
    }

    private SegmentPreviewVo buildPreview(List<Document> parsed, IngestSegmentConfig segmentConfig) {
        List<Document> preprocessed = textPreprocessor.preprocess(
            parsed,
            segmentConfig.isPreprocessNormalizeWs(),
            segmentConfig.isPreprocessRemoveUrl(),
            segmentConfig.isPreprocessRemoveEmail());
        if (preprocessed.isEmpty()) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "预处理后内容为空");
        }

        List<Document> chunks = chunkStrategy.chunk(
            preprocessed,
            segmentConfig.getSegmentMode(),
            segmentConfig.getChunkSize(),
            segmentConfig.getChunkOverlap(),
            segmentConfig.getChunkDelimiter());
        if (chunks.isEmpty()) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "分块结果为空");
        }

        SegmentPreviewVo vo = new SegmentPreviewVo();
        vo.setTotal(chunks.size());
        boolean truncated = chunks.size() > PREVIEW_DISPLAY_LIMIT;
        vo.setTruncated(truncated);

        int limit = Math.min(chunks.size(), PREVIEW_DISPLAY_LIMIT);
        List<SegmentPreviewItemVo> items = new ArrayList<>(limit);
        for (int i = 0; i < limit; i++) {
            Document chunk = chunks.get(i);
            SegmentPreviewItemVo item = new SegmentPreviewItemVo();
            item.setChunkIndex(i);
            item.setContent(chunk.getText());
            item.setTokenCount(estimateTokens(chunk.getText()));
            item.setPageNumber(extractPageNumber(chunk.getMetadata()));
            items.add(item);
        }
        vo.setSegments(items);
        return vo;
    }

    private IngestSegmentConfig resolveConfig(KbKnowledgeBase kb, SegmentConfigBo override) {
        KbDocument snapshot = new KbDocument();
        segmentConfigResolver.applySnapshot(snapshot, kb, override);
        return IngestSegmentConfig.fromDocumentSnapshot(snapshot, kb, knowledgeProperties);
    }

    private KbKnowledgeBase requireKb(Long kbId) {
        if (kbId == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "知识库ID不能为空");
        }
        KbKnowledgeBase kb = knowledgeBaseMapper.selectById(kbId);
        if (kb == null || KnowledgeConstants.DELETED == kb.getDeleted()) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "知识库不存在或已删除");
        }
        return kb;
    }

    private static int estimateTokens(String text) {
        if (StrUtil.isBlank(text)) {
            return 0;
        }
        return Math.max(1, text.length() / 4);
    }

    private static Integer extractPageNumber(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }
        Object page = metadata.get("page_number");
        if (page == null) {
            page = metadata.get("pageNumber");
        }
        if (page instanceof Number number) {
            return number.intValue();
        }
        return null;
    }
}
