package io.github.genkidoudou.web.knowledge.ingest;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.genkidoudou.web.knowledge.config.KnowledgeProperties;
import io.github.genkidoudou.web.knowledge.constants.KbDocStatus;
import io.github.genkidoudou.web.knowledge.constants.KbTaskStatus;
import io.github.genkidoudou.web.knowledge.domain.KbDocument;
import io.github.genkidoudou.web.knowledge.domain.KbDocumentChunk;
import io.github.genkidoudou.web.knowledge.domain.KbIngestTask;
import io.github.genkidoudou.web.knowledge.domain.KbKnowledgeBase;
import io.github.genkidoudou.web.knowledge.ingest.chunk.ChunkStrategy;
import io.github.genkidoudou.web.knowledge.ingest.preprocess.TextPreprocessor;
import io.github.genkidoudou.web.knowledge.ingest.source.DocumentSourceAdapter;
import io.github.genkidoudou.web.knowledge.ingest.source.DocumentSourceAdapterRegistry;
import io.github.genkidoudou.web.knowledge.ingest.source.DocumentSourceContext;
import io.github.genkidoudou.web.knowledge.mapper.KbDocumentChunkMapper;
import io.github.genkidoudou.web.knowledge.mapper.KbDocumentMapper;
import io.github.genkidoudou.web.knowledge.mapper.KbIngestTaskMapper;
import io.github.genkidoudou.web.knowledge.mapper.KbKnowledgeBaseMapper;
import io.github.genkidoudou.web.knowledge.support.KnowledgeAiGuard;
import io.github.genkidoudou.web.knowledge.support.KnowledgeVectorSupport;
import io.github.genkidoudou.web.system.file.domain.SysFile;
import io.github.genkidoudou.web.system.file.mapper.SysFileMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文档入库流水线：来源适配 → 文本预处理 → 分段策略 → 向量化 → 写入 PGVector 与分块元数据。
 * <p>
 * 分段与预处理参数读取文档快照字段（{@link IngestSegmentConfig}），重建索引时不随知识库后续变更而变。
 */
@Service
public class DocumentIngestionService {

    private static final Logger log = LoggerFactory.getLogger(DocumentIngestionService.class);

    private static final int CONTENT_PREVIEW_MAX = 500;

    private final KbDocumentMapper documentMapper;
    private final KbKnowledgeBaseMapper knowledgeBaseMapper;
    private final KbDocumentChunkMapper chunkMapper;
    private final KbIngestTaskMapper taskMapper;
    private final SysFileMapper sysFileMapper;
    private final VectorStore vectorStore;
    private final KnowledgeVectorSupport vectorSupport;
    private final KnowledgeAiGuard aiGuard;
    private final KnowledgeProperties knowledgeProperties;
    private final DocumentSourceAdapterRegistry sourceAdapterRegistry;
    private final TextPreprocessor textPreprocessor;
    private final ChunkStrategy chunkStrategy;

    public DocumentIngestionService(KbDocumentMapper documentMapper,
                                    KbKnowledgeBaseMapper knowledgeBaseMapper,
                                    KbDocumentChunkMapper chunkMapper,
                                    KbIngestTaskMapper taskMapper,
                                    SysFileMapper sysFileMapper,
                                    VectorStore vectorStore,
                                    KnowledgeVectorSupport vectorSupport,
                                    KnowledgeAiGuard aiGuard,
                                    KnowledgeProperties knowledgeProperties,
                                    DocumentSourceAdapterRegistry sourceAdapterRegistry,
                                    TextPreprocessor textPreprocessor,
                                    ChunkStrategy chunkStrategy) {
        this.documentMapper = documentMapper;
        this.knowledgeBaseMapper = knowledgeBaseMapper;
        this.chunkMapper = chunkMapper;
        this.taskMapper = taskMapper;
        this.sysFileMapper = sysFileMapper;
        this.vectorStore = vectorStore;
        this.vectorSupport = vectorSupport;
        this.aiGuard = aiGuard;
        this.knowledgeProperties = knowledgeProperties;
        this.sourceAdapterRegistry = sourceAdapterRegistry;
        this.textPreprocessor = textPreprocessor;
        this.chunkStrategy = chunkStrategy;
    }

    /**
     * 执行单次文档入库任务（由 {@link IngestTaskAsyncExecutor} 异步调用）。
     *
     * @param taskId 入库任务 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void ingest(Long taskId) {
        KbIngestTask task = taskMapper.selectById(taskId);
        if (task == null) {
            log.warn("入库任务不存在: taskId={}", taskId);
            return;
        }
        KbDocument doc = documentMapper.selectById(task.getDocId());
        if (doc == null) {
            failTask(task, null, "文档不存在");
            return;
        }

        aiGuard.requireEmbeddingModel(doc.getKbId());

        markRunning(task, doc);

        try {
            KbKnowledgeBase kb = knowledgeBaseMapper.selectById(doc.getKbId());
            if (kb == null) {
                throw new IllegalStateException("知识库不存在: " + doc.getKbId());
            }

            IngestSegmentConfig segmentConfig = IngestSegmentConfig.fromDocumentSnapshot(doc, kb, knowledgeProperties);

            vectorSupport.deleteByDocId(doc.getDocId());
            chunkMapper.delete(Wrappers.<KbDocumentChunk>lambdaQuery().eq(KbDocumentChunk::getDocId, doc.getDocId()));

            SysFile file = doc.getFileId() != null ? sysFileMapper.selectById(doc.getFileId()) : null;
            DocumentSourceAdapter adapter = sourceAdapterRegistry.resolve(doc.getSourceType());
            DocumentSourceContext sourceContext = new DocumentSourceContext(doc, kb, file);
            List<Document> parsed = adapter.load(sourceContext);
            if (parsed.isEmpty()) {
                throw new IllegalStateException("文档解析结果为空");
            }

            List<Document> preprocessed = textPreprocessor.preprocess(
                parsed,
                segmentConfig.isPreprocessNormalizeWs(),
                segmentConfig.isPreprocessRemoveUrl(),
                segmentConfig.isPreprocessRemoveEmail());
            if (preprocessed.isEmpty()) {
                throw new IllegalStateException("预处理后文档内容为空");
            }

            List<Document> chunks = chunkStrategy.chunk(
                preprocessed,
                segmentConfig.getSegmentMode(),
                segmentConfig.getChunkSize(),
                segmentConfig.getChunkOverlap(),
                segmentConfig.getChunkDelimiter());
            if (chunks.isEmpty()) {
                throw new IllegalStateException("文档分块结果为空");
            }

            updateProgress(task, 30);

            String fileName = resolveFileName(file, doc);
            String kbIdStr = String.valueOf(doc.getKbId());
            String docIdStr = String.valueOf(doc.getDocId());

            List<Document> toEmbed = new ArrayList<>(chunks.size());
            List<KbDocumentChunk> chunkRows = new ArrayList<>(chunks.size());

            for (int i = 0; i < chunks.size(); i++) {
                Document chunk = chunks.get(i);
                KbDocumentChunk row = new KbDocumentChunk();
                row.setDocId(doc.getDocId());
                row.setChunkIndex(i);
                row.setContentPreview(truncatePreview(chunk.getText()));
                row.setContentFull(chunk.getText());
                row.setEnabled(1);
                row.setPageNumber(extractPageNumber(chunk.getMetadata()));
                chunkMapper.insert(row);

                Map<String, Object> metadata = new HashMap<>(chunk.getMetadata());
                metadata.put("kbId", kbIdStr);
                metadata.put("docId", docIdStr);
                metadata.put("chunkId", String.valueOf(row.getChunkId()));
                metadata.put("fileName", fileName);
                if (row.getPageNumber() != null) {
                    metadata.put("pageNumber", row.getPageNumber());
                }

                Document enriched = Document.builder()
                    .text(chunk.getText())
                    .metadata(metadata)
                    .build();
                toEmbed.add(enriched);
                chunkRows.add(row);
            }

            updateProgress(task, 60);

            vectorStore.add(toEmbed);

            for (int i = 0; i < toEmbed.size(); i++) {
                Document embedded = toEmbed.get(i);
                KbDocumentChunk row = chunkRows.get(i);
                KbDocumentChunk upd = new KbDocumentChunk();
                upd.setChunkId(row.getChunkId());
                upd.setVectorId(embedded.getId());
                chunkMapper.updateById(upd);
            }

            KbDocument docUpd = new KbDocument();
            docUpd.setDocId(doc.getDocId());
            docUpd.setDocStatus(KbDocStatus.INDEXED);
            docUpd.setChunkCount(chunks.size());
            docUpd.setErrorMsg(null);
            documentMapper.updateById(docUpd);

            task.setStatus(KbTaskStatus.SUCCESS);
            task.setProgress(100);
            task.setErrorMsg(null);
            task.setEndTime(LocalDateTime.now());
            taskMapper.updateById(task);
        } catch (Throwable ex) {
            log.error("文档入库失败 taskId={}, docId={}", taskId, doc.getDocId(), ex);
            String message = StrUtil.sub(ex.getMessage(), 0, 500);
            if (StrUtil.isBlank(message)) {
                message = "文档入库失败";
            }
            failTask(task, doc, message);
        }
    }

    private String resolveFileName(SysFile file, KbDocument doc) {
        if (file != null && StrUtil.isNotBlank(file.getOriginalName())) {
            return file.getOriginalName();
        }
        return StrUtil.blankToDefault(doc.getTitle(), "document");
    }

    private void markRunning(KbIngestTask task, KbDocument doc) {
        LocalDateTime now = LocalDateTime.now();
        task.setStatus(KbTaskStatus.RUNNING);
        task.setProgress(5);
        task.setStartTime(now);
        task.setEndTime(null);
        task.setErrorMsg(null);
        taskMapper.updateById(task);

        KbDocument docUpd = new KbDocument();
        docUpd.setDocId(doc.getDocId());
        docUpd.setDocStatus(KbDocStatus.PARSING);
        docUpd.setErrorMsg(null);
        documentMapper.updateById(docUpd);
    }

    private void updateProgress(KbIngestTask task, int progress) {
        task.setProgress(progress);
        taskMapper.updateById(task);
    }

    private void failTask(KbIngestTask task, KbDocument doc, String errorMsg) {
        task.setStatus(KbTaskStatus.FAILED);
        task.setProgress(100);
        task.setErrorMsg(errorMsg);
        task.setEndTime(LocalDateTime.now());
        taskMapper.updateById(task);

        KbDocument target = doc;
        if (target == null && task.getDocId() != null) {
            target = documentMapper.selectById(task.getDocId());
        }
        if (target != null) {
            KbDocument docUpd = new KbDocument();
            docUpd.setDocId(target.getDocId());
            docUpd.setDocStatus(KbDocStatus.FAILED);
            docUpd.setErrorMsg(errorMsg);
            documentMapper.updateById(docUpd);
        }
    }

    private static String truncatePreview(String text) {
        if (text == null) {
            return "";
        }
        String normalized = text.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= CONTENT_PREVIEW_MAX) {
            return normalized;
        }
        return normalized.substring(0, CONTENT_PREVIEW_MAX);
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
        if (page instanceof String str && StrUtil.isNotBlank(str)) {
            try {
                return Integer.parseInt(str.trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }
}
