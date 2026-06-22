package io.github.genkidoudou.web.knowledge.service;

import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.web.knowledge.domain.KbDocument;
import io.github.genkidoudou.web.knowledge.dto.KbDocumentAddFromLibraryBo;
import io.github.genkidoudou.web.knowledge.dto.KbDocumentAddFromWebBo;
import io.github.genkidoudou.web.knowledge.dto.KbDocumentAddManualBo;
import io.github.genkidoudou.web.knowledge.dto.KbDocumentBatchUploadVo;
import io.github.genkidoudou.web.knowledge.dto.KbDocumentChunkVo;
import io.github.genkidoudou.web.knowledge.dto.KbDocumentPreviewVo;
import io.github.genkidoudou.web.knowledge.dto.KbDocumentQueryBo;
import io.github.genkidoudou.web.knowledge.dto.KbDocumentUploadVo;
import io.github.genkidoudou.web.knowledge.dto.KbDocumentVo;
import io.github.genkidoudou.web.knowledge.dto.SegmentConfigBo;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 知识库文档管理服务。
 */
public interface KbDocumentService {

    /**
     * 文档分页列表。
     */
    PageInfo<KbDocumentVo> page(KbDocumentQueryBo query);

    /**
     * 按 ID 查询文档视图（不含分块列表）。
     */
    KbDocumentVo getInfo(Long docId);

    /**
     * 查询文档下已入库分块列表（按 chunkIndex 升序）。
     */
    List<KbDocumentChunkVo> listChunks(Long docId);

    /**
     * 按 ID 查询文档实体。
     */
    KbDocument getById(Long docId);

    /**
     * 文档预览元数据（文本 / 模式 / 是否可流式预览）。
     */
    KbDocumentPreviewVo previewInfo(Long docId);

    /**
     * 输出文档原文文件流（inline），供前端 PDF 等预览。
     */
    void writePreviewStream(Long docId, HttpServletResponse response) throws Exception;

    /**
     * 上传文档并触发异步入库。
     */
    KbDocumentUploadVo upload(Long kbId, MultipartFile file, SegmentConfigBo segmentConfig);

    /**
     * 上传 ZIP 压缩包：解压后按允许扩展名逐文件异步入库。
     */
    KbDocumentBatchUploadVo uploadZip(Long kbId, MultipartFile zipFile, SegmentConfigBo segmentConfig);

    /**
     * 手动录入文档并触发异步入库。
     */
    KbDocumentUploadVo addManual(KbDocumentAddManualBo req);

    /**
     * 网页 URL 抓取入库。
     */
    KbDocumentUploadVo addFromWeb(KbDocumentAddFromWebBo req);

    /**
     * 从文档库选取文件入库。
     */
    KbDocumentUploadVo addFromLibrary(KbDocumentAddFromLibraryBo req);

    /**
     * 重建文档向量索引。
     */
    KbDocumentUploadVo reindex(Long docId);

    /**
     * 批量删除文档（删向量 + 逻辑删记录）。
     */
    void removeBatch(List<Long> docIds);
}
