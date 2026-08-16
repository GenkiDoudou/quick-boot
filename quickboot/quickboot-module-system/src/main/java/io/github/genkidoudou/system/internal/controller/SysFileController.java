package io.github.genkidoudou.system.internal.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.PageRequest;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.common.excel.ExcelUtils;
import io.github.genkidoudou.system.internal.service.ISysFileService;
import io.github.genkidoudou.system.internal.vo.SysFileUploadVo;
import io.github.genkidoudou.system.internal.vo.SysFileVo;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * 系统文件管理：列表、上传登记、预览、下载、删除。
 */
@Tag(name = "文件管理")
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("system/file")
public class SysFileController {

  private final ISysFileService sysFileService;

  /**
   * 分页列表。
   *
   * @param pageNum          页码，默认 1
   * @param pageSize         每页条数，默认 10
   * @param originalName     原始文件名模糊
   * @param uploaderUserName 上传人用户名模糊
   * @param classify         分类键
   * @return 文件记录分页
   */
  @Operation(summary = "文件分页列表")
  @SaCheckPermission("system:file:list")
  @GetMapping("/list")
  public R<PageInfo<SysFileVo>> list(
    @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
    @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
    @RequestParam(value = "originalName", required = false) String originalName,
    @RequestParam(value = "uploaderUserName", required = false) String uploaderUserName,
    @RequestParam(value = "classify", required = false) String classify
  ) {
    SysFileVo param = new SysFileVo();
    param.setOriginalName(originalName);
    param.setUploaderUserName(uploaderUserName);
    param.setClassify(classify);
    int current = pageNum == null || pageNum < 1 ? 1 : pageNum;
    int size = pageSize == null || pageSize < 1 ? 10 : pageSize;
    return R.ok(sysFileService.page(new PageRequest<>(current, size, param)));
  }

  /**
   * 管理端上传并登记 {@code sys_file}。
   *
   * @param file     上传文件
   * @param classify 分类键
   * @return 文件主键、相对路径等登记结果
   */
  @Operation(summary = "文件上传（登记）")
  @SaCheckPermission("system:file:upload")
  @PostMapping(value = "/upload/{classify}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public R<SysFileUploadVo> upload(
    @Parameter(description = "文件") @RequestPart("file") MultipartFile file,
    @Parameter(description = "分类键") @PathVariable("classify") String classify
  ) {
    return R.ok(sysFileService.upload(file, classify));
  }

  /**
   * 按相对路径 inline 预览。
   *
   * @param relativePath 存储相对路径
   * @param response     写入 inline 文件流
   */
  @Operation(summary = "按相对路径预览")
  @SaCheckPermission("system:file:view")
  @GetMapping("/view/{*relativePath}")
  public void view(
    @Parameter(description = "存储相对路径") @PathVariable("relativePath") String relativePath,
    HttpServletResponse response
  ) throws Exception {
    String path = normalizeViewRelativePath(relativePath);
    ISysFileService.DownloadPayload payload = sysFileService.viewStream(path);
    writeInlineFile(response, payload);
  }

  /**
   * 按文件主键 inline 预览（管理端弹窗鉴权拉流；响应带 Content-Type）。
   *
   * @param fileId   文件主键
   * @param response 写入 inline 文件流
   */
  @Operation(summary = "按文件主键预览")
  @SaCheckPermission("system:file:view")
  @GetMapping("/preview/{fileId}")
  public void preview(
    @Parameter(description = "文件主键") @PathVariable @Min(1) Long fileId,
    HttpServletResponse response
  ) throws Exception {
    ISysFileService.DownloadPayload payload = sysFileService.preview(fileId);
    writeInlineFile(response, payload);
  }

  /**
   * 附件下载。
   *
   * @param fileId   文件主键
   * @param response 写入 attachment 文件流
   */
  @Operation(summary = "下载文件")
  @SaCheckPermission("system:file:download")
  @GetMapping("/download/{fileId}")
  public void download(
    @Parameter(description = "文件主键") @PathVariable @Min(1) Long fileId,
    HttpServletResponse response
  ) throws Exception {
    ISysFileService.DownloadPayload payload = sysFileService.download(fileId);
    writeAttachmentFile(response, payload);
  }

  /**
   * 批量删除（软删 + 删本地对象）。
   *
   * @param ids 文件主键数组
   * @return ok；副作用为软删记录并删除存储对象
   */
  @Operation(summary = "删除文件")
  @SaCheckPermission("system:file:remove")
  @PostMapping("/remove")
  public R<Void> remove(@RequestBody Long[] ids) {
    List<Long> idList = ids == null ? List.of() : Arrays.asList(ids);
    sysFileService.remove(idList);
    return R.ok();
  }

  private static String normalizeViewRelativePath(String relativePath) {
    String path = relativePath == null ? "" : relativePath.trim();
    if (path.startsWith("/")) {
      path = path.substring(1);
    }
    return URLDecoder.decode(path, StandardCharsets.UTF_8);
  }

  private static void writeInlineFile(HttpServletResponse response, ISysFileService.DownloadPayload payload)
    throws Exception {
    String ct = payload.contentType();
    response.setContentType(ct != null && !ct.isBlank() ? ct : MediaType.APPLICATION_OCTET_STREAM_VALUE);
    setInlineResponseHeader(response, payload.originalName());
    try (InputStream in = payload.resource().getInputStream()) {
      in.transferTo(response.getOutputStream());
    }
  }

  private static void writeAttachmentFile(HttpServletResponse response, ISysFileService.DownloadPayload payload)
    throws Exception {
    String ct = payload.contentType();
    response.setContentType(ct != null && !ct.isBlank() ? ct : MediaType.APPLICATION_OCTET_STREAM_VALUE);
    ExcelUtils.setAttachmentResponseHeader(response, payload.originalName());
    try (InputStream in = payload.resource().getInputStream()) {
      in.transferTo(response.getOutputStream());
    }
  }

  private static void setInlineResponseHeader(HttpServletResponse response, String fileName)
    throws UnsupportedEncodingException {
    if (fileName == null || fileName.isBlank()) {
      response.setHeader("Content-Disposition", "inline");
      return;
    }
    String encoded = ExcelUtils.percentEncode(fileName);
    response.setHeader("Content-Disposition",
      "inline; filename=\"" + encoded + "\"; filename*=utf-8''" + encoded);
  }
}
