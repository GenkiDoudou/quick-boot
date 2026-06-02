package io.github.genkidoudou.web.system.file.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.common.excel.ExcelUtils;
import io.github.genkidoudou.common.file.FileAccessService;
import io.github.genkidoudou.common.file.FileClassifyVo;
import io.github.genkidoudou.common.file.FileUploadResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 通用文件接口（独立于系统文件管理）：上传、分类配置、预览。
 */
@Tag(name = "通用文件")
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class CommonFileController {

  private final FileAccessService fileAccessService;

  @Operation(summary = "查询上传分类配置")
  @GetMapping("/classifies")
  public R<List<FileClassifyVo>> listClassifies() {
    return R.ok(fileAccessService.listClassifies());
  }

  @Operation(summary = "查询单个上传分类配置")
  @GetMapping("/classifies/{classify}")
  public R<FileClassifyVo> getClassify(
    @Parameter(description = "分类名") @PathVariable("classify") String classify
  ) {
    return R.ok(fileAccessService.getClassify(classify));
  }

  @Operation(summary = "按分类上传文件")
  @PostMapping(value = "/upload/{classify}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public R<FileUploadResult> upload(
    @Parameter(description = "文件") @RequestPart("file") MultipartFile file,
    @Parameter(description = "分类（路径参数，必填）") @PathVariable("classify") String classify
  ) {
    return R.ok(fileAccessService.upload(file, classify));
  }

  @SaIgnore
  @Operation(summary = "按相对路径预览文件（inline 流，供浏览器直接打开）")
  @GetMapping("/preview/{*relativePath}")
  public void preview(
    @Parameter(description = "存储相对路径，如 img/2026/06/xxx.png") @PathVariable("relativePath") String relativePath,
    HttpServletResponse response
  ) throws Exception {
    String path = normalizeViewRelativePath(relativePath);
    fileAccessService.assertPreviewAllowed(path);
    var payload = fileAccessService.openForPreview(path);
    String ct = payload.contentType();
    response.setContentType(ct != null && !ct.isBlank() ? ct : MediaType.APPLICATION_OCTET_STREAM_VALUE);
    setInlineResponseHeader(response, payload.fileName());
    try (InputStream in = payload.resource().getInputStream()) {
      in.transferTo(response.getOutputStream());
    }
  }

  private static String normalizeViewRelativePath(String relativePath) {
    String path = relativePath == null ? "" : relativePath.trim();
    if (path.startsWith("/")) {
      path = path.substring(1);
    }
    return URLDecoder.decode(path, StandardCharsets.UTF_8);
  }

  private static void setInlineResponseHeader(HttpServletResponse response, String fileName) throws UnsupportedEncodingException {
    if (fileName == null || fileName.isBlank()) {
      response.setHeader("Content-Disposition", "inline");
      return;
    }
    String encoded = ExcelUtils.percentEncode(fileName);
    response.setHeader("Content-Disposition", "inline; filename=\"" + encoded + "\"; filename*=utf-8''" + encoded);
  }
}

