package io.github.genkidoudou.web.knowledge.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.web.knowledge.constants.KnowledgeConstants;
import io.github.genkidoudou.web.knowledge.domain.KbDocument;
import io.github.genkidoudou.web.knowledge.domain.KbDocumentChunk;
import io.github.genkidoudou.web.knowledge.dto.KbDocumentChunkUpdateBo;
import io.github.genkidoudou.web.knowledge.mapper.KbDocumentChunkMapper;
import io.github.genkidoudou.web.knowledge.mapper.KbDocumentMapper;
import io.github.genkidoudou.web.knowledge.service.KbDocumentChunkService;
import io.github.genkidoudou.web.knowledge.support.KnowledgeAiGuard;
import io.github.genkidoudou.web.knowledge.support.KnowledgeVectorSupport;
import io.github.genkidoudou.web.system.file.domain.SysFile;
import io.github.genkidoudou.web.system.file.mapper.SysFileMapper;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分块在线编辑与向量同步。
 */
@Service
public class KbDocumentChunkServiceImpl implements KbDocumentChunkService {

    private static final int CONTENT_PREVIEW_MAX = 500;

    private final KbDocumentChunkMapper chunkMapper;
    private final KbDocumentMapper documentMapper;
    private final SysFileMapper sysFileMapper;
    private final VectorStore vectorStore;
    private final KnowledgeVectorSupport vectorSupport;
    private final KnowledgeAiGuard aiGuard;

    public KbDocumentChunkServiceImpl(KbDocumentChunkMapper chunkMapper,
                                      KbDocumentMapper documentMapper,
                                      SysFileMapper sysFileMapper,
                                      VectorStore vectorStore,
                                      KnowledgeVectorSupport vectorSupport,
                                      KnowledgeAiGuard aiGuard) {
        this.chunkMapper = chunkMapper;
        this.documentMapper = documentMapper;
        this.sysFileMapper = sysFileMapper;
        this.vectorStore = vectorStore;
        this.vectorSupport = vectorSupport;
        this.aiGuard = aiGuard;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateChunk(KbDocumentChunkUpdateBo req) {
        if (req.getContent() == null && req.getEnabled() == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "请至少指定正文或启用状态");
        }

        KbDocumentChunk chunk = chunkMapper.selectById(req.getChunkId());
        if (chunk == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "分块不存在");
        }

        KbDocument doc = documentMapper.selectById(chunk.getDocId());
        if (doc == null || KnowledgeConstants.DELETED == doc.getDeleted()) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "所属文档不存在或已删除");
        }

        boolean contentChanged = false;
        boolean enabledChanged = false;
        Integer targetEnabled = chunk.getEnabled() == null ? 1 : chunk.getEnabled();

        KbDocumentChunk upd = new KbDocumentChunk();
        upd.setChunkId(chunk.getChunkId());

        if (req.getContent() != null) {
            String text = req.getContent().trim();
            if (StrUtil.isBlank(text)) {
                throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "分块正文不能为空");
            }
            upd.setContentFull(text);
            upd.setContentPreview(truncatePreview(text));
            contentChanged = true;
        }

        if (req.getEnabled() != null) {
            if (req.getEnabled() != 0 && req.getEnabled() != 1) {
                throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "启用状态无效");
            }
            upd.setEnabled(req.getEnabled());
            enabledChanged = !req.getEnabled().equals(targetEnabled);
            targetEnabled = req.getEnabled();
        }

        chunkMapper.updateById(upd);

        if (contentChanged || enabledChanged) {
            syncVector(chunk, doc, contentChanged, targetEnabled);
        }
    }

    private void syncVector(KbDocumentChunk chunk, KbDocument doc, boolean contentChanged, int enabled) {
        vectorSupport.deleteByVectorId(chunk.getVectorId());

        if (enabled == 0) {
            KbDocumentChunk clearVector = new KbDocumentChunk();
            clearVector.setChunkId(chunk.getChunkId());
            clearVector.setVectorId("");
            chunkMapper.updateById(clearVector);
            return;
        }

        aiGuard.requireEmbeddingModel(doc.getKbId());

        KbDocumentChunk latest = chunkMapper.selectById(chunk.getChunkId());
        String text = StrUtil.blankToDefault(latest.getContentFull(), latest.getContentPreview());
        if (StrUtil.isBlank(text)) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "分块正文为空，无法重建向量");
        }

        SysFile file = doc.getFileId() != null ? sysFileMapper.selectById(doc.getFileId()) : null;
        String fileName = file != null && StrUtil.isNotBlank(file.getOriginalName())
            ? file.getOriginalName()
            : StrUtil.blankToDefault(doc.getTitle(), "document");

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("kbId", String.valueOf(doc.getKbId()));
        metadata.put("docId", String.valueOf(doc.getDocId()));
        metadata.put("chunkId", String.valueOf(latest.getChunkId()));
        metadata.put("fileName", fileName);
        if (latest.getPageNumber() != null) {
            metadata.put("pageNumber", latest.getPageNumber());
        }

        Document enriched = Document.builder().text(text).metadata(metadata).build();
        vectorStore.add(List.of(enriched));

        KbDocumentChunk vectorUpd = new KbDocumentChunk();
        vectorUpd.setChunkId(latest.getChunkId());
        vectorUpd.setVectorId(enriched.getId());
        chunkMapper.updateById(vectorUpd);
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
}
