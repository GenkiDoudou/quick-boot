package io.github.genkidoudou.web.knowledge.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.web.knowledge.constants.KbDocSourceType;
import io.github.genkidoudou.web.knowledge.constants.KbDocStatus;
import io.github.genkidoudou.web.knowledge.constants.KbTaskStatus;
import io.github.genkidoudou.web.knowledge.constants.KnowledgeConstants;
import io.github.genkidoudou.web.knowledge.domain.KbDocLibraryFile;
import io.github.genkidoudou.web.knowledge.domain.KbDocument;
import io.github.genkidoudou.web.knowledge.domain.KbDocumentChunk;
import io.github.genkidoudou.web.knowledge.domain.KbIngestTask;
import io.github.genkidoudou.web.knowledge.domain.KbKnowledgeBase;
import io.github.genkidoudou.web.knowledge.dto.KbDocumentAddFromLibraryBo;
import io.github.genkidoudou.web.knowledge.dto.KbDocumentAddFromWebBo;
import io.github.genkidoudou.web.knowledge.dto.KbDocumentAddManualBo;
import io.github.genkidoudou.web.knowledge.dto.KbDocumentChunkVo;
import io.github.genkidoudou.web.knowledge.dto.KbDocumentQueryBo;
import io.github.genkidoudou.web.knowledge.dto.KbDocumentUploadVo;
import io.github.genkidoudou.web.knowledge.dto.KbDocumentVo;
import io.github.genkidoudou.web.knowledge.dto.SegmentConfigBo;
import io.github.genkidoudou.web.knowledge.ingest.IngestTaskDispatcher;
import io.github.genkidoudou.web.knowledge.ingest.web.WebContentFetcher;
import io.github.genkidoudou.web.knowledge.mapper.KbDocumentChunkMapper;
import io.github.genkidoudou.web.knowledge.mapper.KbDocumentMapper;
import io.github.genkidoudou.web.knowledge.mapper.KbIngestTaskMapper;
import io.github.genkidoudou.web.knowledge.mapper.KbKnowledgeBaseMapper;
import io.github.genkidoudou.web.knowledge.service.KbDocLibraryFileService;
import io.github.genkidoudou.web.knowledge.service.KbDocumentService;
import io.github.genkidoudou.web.knowledge.support.KnowledgeVectorSupport;
import io.github.genkidoudou.web.knowledge.support.SegmentConfigResolver;
import io.github.genkidoudou.web.system.file.dto.SysFileUploadVo;
import io.github.genkidoudou.web.system.file.service.SysFileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 知识库文档管理服务实现。
 */
@Service
public class KbDocumentServiceImpl implements KbDocumentService {

    private final KbDocumentMapper documentMapper;
    private final KbKnowledgeBaseMapper knowledgeBaseMapper;
    private final KbIngestTaskMapper taskMapper;
    private final KbDocumentChunkMapper chunkMapper;
    private final SysFileService sysFileService;
    private final IngestTaskDispatcher ingestTaskDispatcher;
    private final KnowledgeVectorSupport vectorSupport;
    private final SegmentConfigResolver segmentConfigResolver;
    private final WebContentFetcher webContentFetcher;
    private final KbDocLibraryFileService libraryFileService;

    public KbDocumentServiceImpl(KbDocumentMapper documentMapper,
                                 KbKnowledgeBaseMapper knowledgeBaseMapper,
                                 KbIngestTaskMapper taskMapper,
                                 KbDocumentChunkMapper chunkMapper,
                                 SysFileService sysFileService,
                                 IngestTaskDispatcher ingestTaskDispatcher,
                                 KnowledgeVectorSupport vectorSupport,
                                 SegmentConfigResolver segmentConfigResolver,
                                 WebContentFetcher webContentFetcher,
                                 KbDocLibraryFileService libraryFileService) {
        this.documentMapper = documentMapper;
        this.knowledgeBaseMapper = knowledgeBaseMapper;
        this.taskMapper = taskMapper;
        this.chunkMapper = chunkMapper;
        this.sysFileService = sysFileService;
        this.ingestTaskDispatcher = ingestTaskDispatcher;
        this.vectorSupport = vectorSupport;
        this.segmentConfigResolver = segmentConfigResolver;
        this.webContentFetcher = webContentFetcher;
        this.libraryFileService = libraryFileService;
    }

    @Override
    public PageInfo<KbDocumentVo> page(KbDocumentQueryBo query) {
        int pageNum = query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum();
        int pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 10 : query.getPageSize();

        LambdaQueryWrapper<KbDocument> wrapper = Wrappers.<KbDocument>lambdaQuery()
            .eq(KbDocument::getDeleted, KnowledgeConstants.NOT_DELETED)
            .eq(query.getKbId() != null, KbDocument::getKbId, query.getKbId())
            .like(StrUtil.isNotBlank(query.getTitle()), KbDocument::getTitle, query.getTitle())
            .eq(StrUtil.isNotBlank(query.getDocStatus()), KbDocument::getDocStatus, query.getDocStatus())
            .orderByDesc(KbDocument::getCreateTime);

        Page<KbDocument> mp = documentMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<KbDocumentVo> rows = new ArrayList<>(mp.getRecords().size());
        for (KbDocument row : mp.getRecords()) {
            rows.add(BeanUtil.copyProperties(row, KbDocumentVo.class));
        }
        Page<KbDocumentVo> voPage = new Page<>(mp.getCurrent(), mp.getSize(), mp.getTotal());
        voPage.setRecords(rows);
        return PageInfo.from(voPage);
    }

    @Override
    public KbDocumentVo getInfo(Long docId) {
        KbDocument doc = getById(docId);
        if (doc == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "文档不存在或已删除");
        }
        return BeanUtil.copyProperties(doc, KbDocumentVo.class);
    }

    @Override
    public List<KbDocumentChunkVo> listChunks(Long docId) {
        KbDocument doc = getById(docId);
        if (doc == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "文档不存在或已删除");
        }
        List<KbDocumentChunk> rows = chunkMapper.selectList(
            Wrappers.<KbDocumentChunk>lambdaQuery()
                .eq(KbDocumentChunk::getDocId, docId)
                .orderByAsc(KbDocumentChunk::getChunkIndex)
        );
        List<KbDocumentChunkVo> result = new ArrayList<>(rows.size());
        for (KbDocumentChunk row : rows) {
            KbDocumentChunkVo vo = BeanUtil.copyProperties(row, KbDocumentChunkVo.class);
            vo.setContent(StrUtil.blankToDefault(row.getContentFull(), row.getContentPreview()));
            result.add(vo);
        }
        return result;
    }

    @Override
    public KbDocument getById(Long docId) {
        if (docId == null) {
            return null;
        }
        KbDocument row = documentMapper.selectById(docId);
        if (row == null || KnowledgeConstants.DELETED == row.getDeleted()) {
            return null;
        }
        return row;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public KbDocumentUploadVo upload(Long kbId, MultipartFile file, SegmentConfigBo segmentConfig) {
        KbKnowledgeBase kb = requireActiveKnowledgeBase(kbId);
        if (file == null || file.isEmpty()) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "上传文件不能为空");
        }

        SysFileUploadVo fileVo = sysFileService.upload(file, KnowledgeConstants.FILE_CLASSIFY);
        String title = StrUtil.blankToDefault(file.getOriginalFilename(), fileVo.getFileName());
        return createDocumentAndTask(kb, KbDocSourceType.FILE, fileVo.getFileId(), null, null, title, segmentConfig);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public KbDocumentUploadVo addManual(KbDocumentAddManualBo req) {
        KbKnowledgeBase kb = requireActiveKnowledgeBase(req.getKbId());
        byte[] bytes = req.getContent().getBytes(StandardCharsets.UTF_8);
        if (bytes.length > KnowledgeConstants.MANUAL_CONTENT_MAX_BYTES) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "正文长度不能超过 512KB");
        }

        String filename = sanitizeFilename(req.getTitle()) + ".md";
        SysFileUploadVo fileVo = sysFileService.uploadBytes(bytes, filename, KnowledgeConstants.FILE_CLASSIFY);
        return createDocumentAndTask(kb, KbDocSourceType.MANUAL, fileVo.getFileId(), null, null,
            req.getTitle().trim(), req.getSegmentConfig());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public KbDocumentUploadVo addFromWeb(KbDocumentAddFromWebBo req) {
        KbKnowledgeBase kb = requireActiveKnowledgeBase(req.getKbId());
        String url = req.getUrl().trim();

        String content;
        try {
            content = webContentFetcher.fetch(url);
        } catch (RuntimeException ex) {
            String msg = StrUtil.blankToDefault(ex.getMessage(), "网页抓取失败");
            if (!msg.startsWith("网页抓取失败")) {
                msg = "网页抓取失败：" + msg;
            }
            throw new WarningException(ErrorCodes.Biz.KNOWLEDGE_STATE_NOT_ALLOWED, msg);
        }
        if (StrUtil.isBlank(content)) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "网页抓取失败：正文为空");
        }

        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        String title = StrUtil.blankToDefault(req.getTitle(), deriveTitleFromUrl(url));
        String filename = sanitizeFilename(title) + ".txt";
        SysFileUploadVo fileVo = sysFileService.uploadBytes(bytes, filename, KnowledgeConstants.FILE_CLASSIFY);

        return createDocumentAndTask(kb, KbDocSourceType.WEB, fileVo.getFileId(), null, url,
            title, req.getSegmentConfig());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public KbDocumentUploadVo addFromLibrary(KbDocumentAddFromLibraryBo req) {
        KbKnowledgeBase kb = requireActiveKnowledgeBase(req.getKbId());
        KbDocLibraryFile libraryFile = libraryFileService.getById(req.getLibFileId());
        if (libraryFile == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "文档库文件不存在或已删除");
        }

        String title = StrUtil.blankToDefault(req.getTitle(), libraryFile.getTitle());
        return createDocumentAndTask(kb, KbDocSourceType.LIBRARY, libraryFile.getFileId(),
            libraryFile.getLibFileId(), null, title, req.getSegmentConfig());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public KbDocumentUploadVo reindex(Long docId) {
        KbDocument doc = getById(docId);
        if (doc == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "文档不存在或已删除");
        }
        requireActiveKnowledgeBase(doc.getKbId());

        if (KbDocStatus.PARSING.equals(doc.getDocStatus())) {
            throw new WarningException(ErrorCodes.Biz.KNOWLEDGE_STATE_NOT_ALLOWED, "文档正在入库中，请稍后再试");
        }

        vectorSupport.deleteByDocId(docId);
        chunkMapper.delete(Wrappers.<KbDocumentChunk>lambdaQuery().eq(KbDocumentChunk::getDocId, docId));

        KbDocument docUpd = new KbDocument();
        docUpd.setDocId(docId);
        docUpd.setDocStatus(KbDocStatus.PENDING);
        docUpd.setChunkCount(0);
        docUpd.setErrorMsg(null);
        documentMapper.updateById(docUpd);

        KbIngestTask task = createQueuedTask(docId);
        ingestTaskDispatcher.dispatchAfterCommit(task.getTaskId());

        KbDocumentUploadVo vo = new KbDocumentUploadVo();
        vo.setDocId(docId);
        vo.setTaskId(task.getTaskId());
        vo.setFileId(doc.getFileId());
        vo.setTitle(doc.getTitle());
        vo.setDocStatus(KbDocStatus.PENDING);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeBatch(List<Long> docIds) {
        if (docIds == null || docIds.isEmpty()) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "删除文档ID不能为空");
        }
        for (Long docId : docIds) {
            KbDocument doc = getById(docId);
            if (doc == null) {
                throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "存在无效的文档ID: " + docId);
            }
        }
        for (Long docId : docIds) {
            vectorSupport.deleteByDocId(docId);
            chunkMapper.delete(Wrappers.<KbDocumentChunk>lambdaQuery().eq(KbDocumentChunk::getDocId, docId));

            KbDocument upd = new KbDocument();
            upd.setDocId(docId);
            upd.setDeleted(KnowledgeConstants.DELETED);
            documentMapper.updateById(upd);
        }
    }

    private KbDocumentUploadVo createDocumentAndTask(KbKnowledgeBase kb,
                                                     String sourceType,
                                                     Long fileId,
                                                     Long libraryFileId,
                                                     String sourceUrl,
                                                     String title,
                                                     SegmentConfigBo segmentConfig) {
        KbDocument doc = new KbDocument();
        doc.setKbId(kb.getKbId());
        doc.setSourceType(sourceType);
        doc.setFileId(fileId);
        doc.setLibraryFileId(libraryFileId);
        doc.setSourceUrl(sourceUrl);
        doc.setTitle(title);
        doc.setDocStatus(KbDocStatus.PENDING);
        doc.setChunkCount(0);
        doc.setDeleted(KnowledgeConstants.NOT_DELETED);
        segmentConfigResolver.applySnapshot(doc, kb, segmentConfig);
        documentMapper.insert(doc);

        KbIngestTask task = createQueuedTask(doc.getDocId());
        ingestTaskDispatcher.dispatchAfterCommit(task.getTaskId());

        KbDocumentUploadVo vo = new KbDocumentUploadVo();
        vo.setDocId(doc.getDocId());
        vo.setTaskId(task.getTaskId());
        vo.setFileId(doc.getFileId());
        vo.setTitle(doc.getTitle());
        vo.setDocStatus(doc.getDocStatus());
        return vo;
    }

    private KbIngestTask createQueuedTask(Long docId) {
        KbIngestTask task = new KbIngestTask();
        task.setDocId(docId);
        task.setStatus(KbTaskStatus.QUEUED);
        task.setProgress(0);
        task.setRetryCount(0);
        taskMapper.insert(task);
        return task;
    }

    private KbKnowledgeBase requireActiveKnowledgeBase(Long kbId) {
        if (kbId == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "知识库ID不能为空");
        }
        KbKnowledgeBase kb = knowledgeBaseMapper.selectById(kbId);
        if (kb == null || KnowledgeConstants.DELETED == kb.getDeleted()) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "知识库不存在或已删除");
        }
        if (KnowledgeConstants.KB_STATUS_DISABLED == kb.getStatus()) {
            throw new WarningException(ErrorCodes.Biz.KNOWLEDGE_STATE_NOT_ALLOWED, "知识库已停用，禁止上传文档");
        }
        return kb;
    }

    private static String sanitizeFilename(String name) {
        String base = StrUtil.blankToDefault(name, "document");
        return base.replaceAll("[\\\\/:*?\"<>|\\s]+", "_");
    }

    private static String deriveTitleFromUrl(String url) {
        try {
            URI uri = URI.create(url);
            String path = uri.getPath();
            if (StrUtil.isNotBlank(path) && !"/".equals(path)) {
                String segment = path.substring(path.lastIndexOf('/') + 1);
                if (StrUtil.isNotBlank(segment)) {
                    return segment;
                }
            }
            String host = uri.getHost();
            if (StrUtil.isNotBlank(host)) {
                return host;
            }
        } catch (Exception ignored) {
            // 回退为原始 URL 截断
        }
        return StrUtil.sub(url, 0, 200);
    }
}
