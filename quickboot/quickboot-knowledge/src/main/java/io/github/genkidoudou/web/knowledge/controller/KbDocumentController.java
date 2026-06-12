package io.github.genkidoudou.web.knowledge.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.web.knowledge.dto.KbDocumentAddFromLibraryBo;
import io.github.genkidoudou.web.knowledge.dto.KbDocumentAddFromWebBo;
import io.github.genkidoudou.web.knowledge.dto.KbDocumentAddManualBo;
import io.github.genkidoudou.web.knowledge.dto.KbDocumentChunkVo;
import io.github.genkidoudou.web.knowledge.dto.KbDocumentQueryBo;
import io.github.genkidoudou.web.knowledge.dto.KbDocumentUploadVo;
import io.github.genkidoudou.web.knowledge.dto.KbDocumentVo;
import io.github.genkidoudou.web.knowledge.dto.SegmentConfigBo;
import io.github.genkidoudou.web.knowledge.dto.SegmentPreviewBo;
import io.github.genkidoudou.web.knowledge.dto.SegmentPreviewVo;
import io.github.genkidoudou.web.knowledge.ingest.SegmentPreviewService;
import io.github.genkidoudou.web.knowledge.dto.KbDocumentChunkUpdateBo;
import io.github.genkidoudou.web.knowledge.service.KbDocumentChunkService;
import io.github.genkidoudou.web.knowledge.service.KbDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 知识库文档管理接口。
 */
@Tag(name = "知识库文档管理")
@Validated
@RestController
@RequestMapping("/knowledge/doc")
@RequiredArgsConstructor
public class KbDocumentController {

    private final KbDocumentService service;
    private final SegmentPreviewService segmentPreviewService;
    private final KbDocumentChunkService chunkService;

    @Operation(summary = "文档分页列表")
    @SaCheckPermission("knowledge:doc:list")
    @GetMapping("/list")
    public R<PageInfo<KbDocumentVo>> list(@Validated KbDocumentQueryBo query) {
        return R.ok(service.page(query));
    }

    @Operation(summary = "文档详情")
    @SaCheckPermission("knowledge:doc:list")
    @GetMapping("/getInfo")
    public R<KbDocumentVo> getInfo(
        @Parameter(description = "文档ID") @RequestParam @Min(1) Long docId) {
        return R.ok(service.getInfo(docId));
    }

    @Operation(summary = "文档分块列表")
    @SaCheckPermission("knowledge:doc:list")
    @GetMapping("/chunks")
    public R<List<KbDocumentChunkVo>> chunks(
        @Parameter(description = "文档ID") @RequestParam @Min(1) Long docId) {
        return R.ok(service.listChunks(docId));
    }

    @Operation(summary = "入库前分段预览（手动/网页/文档库）")
    @SaCheckPermission("knowledge:doc:upload")
    @PostMapping("/previewSegments")
    public R<SegmentPreviewVo> previewSegments(@Validated @RequestBody SegmentPreviewBo req) {
        return R.ok(segmentPreviewService.preview(req));
    }

    @Operation(summary = "入库前分段预览（文件上传）")
    @SaCheckPermission("knowledge:doc:upload")
    @PostMapping(value = "/previewSegmentsFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<SegmentPreviewVo> previewSegmentsFile(
        @Parameter(description = "知识库ID") @RequestParam @Min(1) Long kbId,
        @Parameter(description = "文档文件") @RequestPart("file") MultipartFile file,
        @Parameter(description = "可选分段配置") @RequestPart(value = "segmentConfig", required = false) SegmentConfigBo segmentConfig) {
        return R.ok(segmentPreviewService.previewFile(kbId, file, segmentConfig));
    }

    @Operation(summary = "更新分块（编辑正文 / 启用禁用）")
    @SaCheckPermission("knowledge:doc:reindex")
    @PostMapping("/chunk/update")
    public R<Void> updateChunk(@Validated @RequestBody KbDocumentChunkUpdateBo req) {
        chunkService.updateChunk(req);
        return R.ok();
    }

    @Operation(summary = "上传文档并触发异步入库")
    @SaCheckPermission("knowledge:doc:upload")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<KbDocumentUploadVo> upload(
        @Parameter(description = "知识库ID") @RequestParam @Min(1) Long kbId,
        @Parameter(description = "文档文件") @RequestPart("file") MultipartFile file,
        @Parameter(description = "可选分段配置") @RequestPart(value = "segmentConfig", required = false) SegmentConfigBo segmentConfig) {
        return R.ok(service.upload(kbId, file, segmentConfig));
    }

    @Operation(summary = "手动录入文档")
    @SaCheckPermission("knowledge:doc:upload")
    @PostMapping("/addManual")
    public R<KbDocumentUploadVo> addManual(@Validated @RequestBody KbDocumentAddManualBo req) {
        return R.ok(service.addManual(req));
    }

    @Operation(summary = "网页 URL 抓取入库")
    @SaCheckPermission("knowledge:doc:upload")
    @PostMapping("/addFromWeb")
    public R<KbDocumentUploadVo> addFromWeb(@Validated @RequestBody KbDocumentAddFromWebBo req) {
        return R.ok(service.addFromWeb(req));
    }

    @Operation(summary = "从文档库选取文件入库")
    @SaCheckPermission("knowledge:doc:upload")
    @PostMapping("/addFromLibrary")
    public R<KbDocumentUploadVo> addFromLibrary(@Validated @RequestBody KbDocumentAddFromLibraryBo req) {
        return R.ok(service.addFromLibrary(req));
    }

    @Operation(summary = "重建文档向量索引")
    @SaCheckPermission("knowledge:doc:upload")
    @PostMapping("/reindex")
    public R<KbDocumentUploadVo> reindex(
        @Parameter(description = "文档ID") @RequestParam @Min(1) Long docId) {
        return R.ok(service.reindex(docId));
    }

    @Operation(summary = "删除文档")
    @SaCheckPermission("knowledge:doc:remove")
    @PostMapping("/remove")
    public R<Void> remove(@RequestBody List<Long> docIds) {
        service.removeBatch(docIds);
        return R.ok();
    }
}
