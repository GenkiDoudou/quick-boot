package io.github.genkidoudou.web.system.file.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.common.excel.ExcelUtils;
import io.github.genkidoudou.web.system.file.dto.SysFileQueryBo;
import io.github.genkidoudou.web.system.file.dto.SysFileUploadVo;
import io.github.genkidoudou.web.system.file.dto.SysFileVo;
import io.github.genkidoudou.web.system.file.service.SysFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 系统文件管理控制器。
 */
@Tag(name = "文件管理")
@Validated
@RestController
@RequestMapping("/system/file")
@RequiredArgsConstructor
public class SysFileController {

    private final SysFileService service;

    @Operation(summary = "文件分页列表")
    @SaCheckPermission("system:file:list")
    @GetMapping("/list")
    public R<PageInfo<SysFileVo>> list(@Validated SysFileQueryBo query) {
        return R.ok(service.page(query));
    }

    @Operation(summary = "文件上传")
    @SaCheckPermission("system:file:upload")
    @PostMapping(value = "/upload/{classify}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<SysFileUploadVo> upload(
        @Parameter(description = "文件") @RequestPart("file") MultipartFile file,
        @Parameter(description = "分类（路径参数，必填）") @PathVariable("classify") String classify
    ) {
        return R.ok(service.upload(file, classify));
    }

    @Operation(summary = "按相对路径预览文件（inline 流，供浏览器直接打开）")
    @SaCheckPermission("system:file:view")
    @GetMapping("/view/{*relativePath}")
    public void view(
        @Parameter(description = "存储相对路径，如 img/2026/06/xxx.png") @PathVariable("relativePath") String relativePath,
        HttpServletResponse response
    ) throws Exception {
        String path = normalizeViewRelativePath(relativePath);
        var payload = service.viewStream(path);
        writeInlineFile(response, payload);
    }

    @Operation(summary = "下载文件（附件）")
    @SaCheckPermission("system:file:download")
    @GetMapping("/download/{fileId}")
    public void download(@Parameter(description = "文件ID") @PathVariable @Min(1) Long fileId, HttpServletResponse response) throws Exception {
        var payload = service.download(fileId);
        writeAttachmentFile(response, payload);
    }

    private static String normalizeViewRelativePath(String relativePath) {
        String path = relativePath == null ? "" : relativePath.trim();
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        return URLDecoder.decode(path, StandardCharsets.UTF_8);
    }

    private static void writeInlineFile(HttpServletResponse response, SysFileService.DownloadPayload payload) throws Exception {
        String ct = payload.contentType();
        response.setContentType(ct != null && !ct.isBlank() ? ct : MediaType.APPLICATION_OCTET_STREAM_VALUE);
        setInlineResponseHeader(response, payload.originalName());
        try (InputStream in = payload.resource().getInputStream()) {
            in.transferTo(response.getOutputStream());
        }
    }

    private static void writeAttachmentFile(HttpServletResponse response, SysFileService.DownloadPayload payload) throws Exception {
        String ct = payload.contentType();
        response.setContentType(ct != null && !ct.isBlank() ? ct : MediaType.APPLICATION_OCTET_STREAM_VALUE);
        ExcelUtils.setAttachmentResponseHeader(response, payload.originalName());
        try (InputStream in = payload.resource().getInputStream()) {
            in.transferTo(response.getOutputStream());
        }
    }

    private static void setInlineResponseHeader(HttpServletResponse response, String fileName) throws UnsupportedEncodingException {
        if (fileName == null || fileName.isBlank()) {
            response.setHeader("Content-Disposition", "inline");
            return;
        }
        String encoded = ExcelUtils.percentEncode(fileName);
        response.setHeader("Content-Disposition", "inline; filename=\"" + encoded + "\"; filename*=utf-8''" + encoded);
    }

    @Operation(summary = "删除文件（支持批量）")
    @SaCheckPermission("system:file:remove")
    @PostMapping("/remove")
    public R<Void> remove(@org.springframework.web.bind.annotation.RequestBody List<Long> fileIds) {
        service.removeBatch(fileIds);
        return R.ok();
    }
}

