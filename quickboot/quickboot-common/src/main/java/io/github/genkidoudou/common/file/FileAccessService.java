package io.github.genkidoudou.common.file;

import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import cn.dev33.satoken.stp.StpUtil;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.common.file.url.FileUrlSupport;

/**
 * 通用文件上传/预览门面：分类规则、绝对路径拼装；{@code anonymous} 仅控制上传是否可匿名。
 */
public class FileAccessService {

  private final QcFileProperties props;
  private final FileTemplate fileTemplate;
  private final FileClassifyRuleResolver classifyResolver;

  /**
   * @param props             文件配置
   * @param fileTemplate      存储门面
   * @param classifyResolver  分类规则 SPI
   */
  public FileAccessService(
      QcFileProperties props,
      FileTemplate fileTemplate,
      FileClassifyRuleResolver classifyResolver) {
    this.props = props;
    this.fileTemplate = fileTemplate;
    this.classifyResolver = classifyResolver;
  }

  /**
   * 返回启用中的上传分类规则列表（供上层映射 Vo）。
   */
  public List<FileClassifyRule> listEnabledClassifyRules() {
    List<FileClassifyRule> list = new ArrayList<>();
    for (FileClassifyRule rule : classifyResolver.listEnabled()) {
      if (rule == null || !StringUtils.hasText(rule.getClassify())) {
        continue;
      }
      list.add(rule);
    }
    return list;
  }

  /**
   * 按分类名返回启用中的上传规则。
   *
   * @param classify 分类名
   */
  public FileClassifyRule getEnabledClassifyRule(String classify) {
    String classifyKey = requireClassifyKey(classify);
    return requireEnabledRule(classifyKey);
  }

  /**
   * 文件压缩全局默认配置（映射 Vo 时回退用）。
   */
  public QcFileProperties.CompressProperties compressDefaults() {
    return props.getCompress();
  }

  /**
   * 按分类上传；不写 {@code sys_file}（由调用方决定是否登记）。
   *
   * @param file     文件
   * @param classify 分类名（须经 resolver 可解析且启用）
   */
  public FileUploadResult upload(MultipartFile file, String classify) {
    if (file == null || file.isEmpty()) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "上传文件为空");
    }
    String classifyKey = requireClassifyKey(classify);
    FileClassifyRule rule = requireEnabledRule(classifyKey);
    if (!rule.isAnonymous()) {
      StpUtil.checkLogin();
    }
    String ext = FilePathSupport.normalizeExtension(file.getOriginalFilename());
    FilePathSupport.validateAgainstRule(ext, file.getSize(), rule);

    String relativePath = fileTemplate.upload(file, classifyKey);
    String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "";
    String absolutePath = FileUrlSupport.resolvePublicUrl(props, relativePath);
    return FileUploadResult.of(fileName, relativePath, absolutePath, classifyKey);
  }

  /**
   * 预览前校验相对路径合法性。
   * <p>
   * 登录与否由 Controller 层或 {@code qc.security.web.anonymous-paths} 控制；
   * 勿在此调用 {@link StpUtil#checkLogin()}。
   */
  public void assertPreviewAllowed(String relativePath) {
    FilePathSupport.validateRelativePath(relativePath);
  }

  /**
   * 按相对路径打开文件流（须先 {@link #assertPreviewAllowed}）。
   */
  public PreviewPayload openForPreview(String relativePath) {
    FilePathSupport.validateRelativePath(relativePath);
    Resource resource = fileTemplate.download(relativePath.trim());
    String fileName = fileNameFromPath(relativePath);
    String contentType = resolveContentType(null, fileName);
    return new PreviewPayload(resource, fileName, contentType);
  }

  /**
   * 解析响应 MIME：优先已存 contentType，否则按文件名后缀推断。
   *
   * @param storedContentType 库内或上传方提供的 MIME，可空
   * @param fileName          原文件名或路径末段
   * @return MIME；无法推断时可为 null
   */
  public static String resolveContentType(String storedContentType, String fileName) {
    if (StringUtils.hasText(storedContentType)) {
      return storedContentType.trim();
    }
    return guessContentType(fileName);
  }

  /**
   * 预览载荷。
   *
   * @param resource    文件资源
   * @param fileName    用于 Content-Disposition 展示名
   * @param contentType MIME，可空
   */
  public record PreviewPayload(Resource resource, String fileName, String contentType) {
  }

  private String requireClassifyKey(String classify) {
    if (!StringUtils.hasText(classify)) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "分类 classify 不能为空");
    }
    return FilePathSupport.normalizeClassifyKey(classify.trim(), classify.trim());
  }

  private FileClassifyRule requireEnabledRule(String classifyKey) {
    FileClassifyRule rule = classifyResolver.findByClassify(classifyKey).orElse(null);
    if (rule == null) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "上传分类不存在: " + classifyKey);
    }
    if (!rule.isEnabled()) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "上传分类已停用: " + classifyKey);
    }
    return rule;
  }

  private static String fileNameFromPath(String relativePath) {
    String p = relativePath.trim();
    int slash = p.lastIndexOf('/');
    return slash >= 0 ? p.substring(slash + 1) : p;
  }

  private static String guessContentType(String fileName) {
    if (!StringUtils.hasText(fileName)) {
      return null;
    }
    String lower = fileName.toLowerCase();
    if (lower.endsWith(".png")) {
      return "image/png";
    }
    if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
      return "image/jpeg";
    }
    if (lower.endsWith(".gif")) {
      return "image/gif";
    }
    if (lower.endsWith(".webp")) {
      return "image/webp";
    }
    if (lower.endsWith(".pdf")) {
      return "application/pdf";
    }
    if (lower.endsWith(".mp4")) {
      return "video/mp4";
    }
    if (lower.endsWith(".webm")) {
      return "video/webm";
    }
    return null;
  }
}
