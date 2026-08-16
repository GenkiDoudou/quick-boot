package io.github.genkidoudou.common.file;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.common.file.url.FileUrlSupport;

/**
 * {@link FileTemplate} 默认实现：分类校验、路径规则、钩子与存储委派。
 * <p>
 * 分类 {@code compressEnabled=1} 时，对 jpg/png/bmp 等图片按分类压缩参数（回退 qc.file.compress）做缩小与重编码。
 */
public class DefaultFileTemplate implements FileTemplate {

  private final QcFileProperties props;
  private final FileStorageOperations storage;
  private final FileClassifyRuleResolver classifyResolver;
  private final List<FileUploadHook> hooks;

  /**
   * @param props             文件模块配置
   * @param storage           底层存储
   * @param classifyResolver  分类规则解析
   * @param hooks             上传生命周期钩子（按 {@link org.springframework.core.annotation.Order} 排序）
   */
  public DefaultFileTemplate(
      QcFileProperties props,
      FileStorageOperations storage,
      FileClassifyRuleResolver classifyResolver,
      List<FileUploadHook> hooks) {
    this.props = props;
    this.storage = storage;
    this.classifyResolver = classifyResolver;
    this.hooks = new ArrayList<>(hooks);
    this.hooks.sort(AnnotationAwareOrderComparator.INSTANCE);
  }

  @Override
  public String upload(MultipartFile file, String classify) {
    if (file == null || file.isEmpty()) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "上传文件为空");
    }
    String classifyKey = FilePathSupport.normalizeClassifyKey(classify, props.getDefaultClassify());
    FileClassifyRule rule = requireEnabledRule(classifyKey);
    String originalName = file.getOriginalFilename();
    String ext = FilePathSupport.normalizeExtension(originalName);
    FilePathSupport.validateAgainstRule(ext, file.getSize(), rule);
    byte[] payload;
    String contentType = file.getContentType();
    try {
      payload = file.getBytes();
    } catch (Exception e) {
      throw wrap(e);
    }
    var compressed = ImageCompressSupport.maybeCompress(payload, originalName, rule, props.getCompress());
    if (compressed.isPresent()) {
      payload = compressed.get().bytes();
      contentType = compressed.get().contentType();
    }
    String relativePath = FilePathSupport.buildRelativePath(classifyKey, ext);
    FileUploadBeforeContext before = new FileUploadBeforeContext(file, classifyKey);
    invokeBefore(before);
    try (InputStream in = new ByteArrayInputStream(payload)) {
      storage.put(relativePath, in, payload.length, contentType);
    } catch (Exception e) {
      invokeOnError(before, e, null);
      throw wrap(e);
    }
    invokeAfter(relativePath, before);
    return relativePath;
  }

  @Override
  public String upload(byte[] content, String filename, String classify) {
    if (content == null || content.length == 0) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "上传内容为空");
    }
    String classifyKey = FilePathSupport.normalizeClassifyKey(classify, props.getDefaultClassify());
    FileClassifyRule rule = requireEnabledRule(classifyKey);
    String ext = FilePathSupport.normalizeExtension(filename);
    FilePathSupport.validateAgainstRule(ext, content.length, rule);
    byte[] payload = content;
    String ct = null;
    var compressed = ImageCompressSupport.maybeCompress(payload, filename, rule, props.getCompress());
    if (compressed.isPresent()) {
      payload = compressed.get().bytes();
      ct = compressed.get().contentType();
    }
    String relativePath = FilePathSupport.buildRelativePath(classifyKey, ext);
    FileUploadBeforeContext before = new FileUploadBeforeContext(content, filename, classifyKey, ct);
    invokeBefore(before);
    try (InputStream in = new ByteArrayInputStream(payload)) {
      storage.put(relativePath, in, payload.length, ct);
    } catch (Exception e) {
      invokeOnError(before, e, null);
      throw wrap(e);
    }
    invokeAfter(relativePath, before);
    return relativePath;
  }

  @Override
  public Resource download(String relativePath) {
    FilePathSupport.validateRelativePath(relativePath);
    try {
      InputStream in = storage.openStream(relativePath);
      return new InputStreamResource(in);
    } catch (Exception e) {
      throw new FileStorageException("读取文件失败: " + relativePath, e);
    }
  }

  @Override
  public String view(String relativePath) {
    if (!StringUtils.hasText(relativePath)) {
      throw new FileStorageException("相对路径不能为空");
    }
    String p = relativePath.trim();
    if (isAbsoluteUrl(p)) {
      return p;
    }
    FilePathSupport.validateRelativePath(p);
    if (!StringUtils.hasText(props.getDomain())) {
      return p;
    }
    return FileUrlSupport.join(props.getDomain().trim(), p);
  }

  @Override
  public String getShortUrl(String relativePath) {
    return view(relativePath);
  }

  @Override
  public String getPresignedUrl(String relativePath, int expireSeconds) {
    if (expireSeconds <= 0) {
      throw new FileStorageException("expireSeconds 必须大于 0");
    }
    FilePathSupport.validateRelativePath(relativePath);
    try {
      return storage.presignedGetUrl(relativePath, expireSeconds);
    } catch (UnsupportedOperationException e) {
      return view(relativePath);
    } catch (Exception e) {
      throw new FileStorageException("生成预签名 URL 失败: " + relativePath, e);
    }
  }

  @Override
  public void delete(String relativePath) {
    FilePathSupport.validateRelativePath(relativePath);
    try {
      storage.remove(relativePath);
    } catch (Exception e) {
      throw new FileStorageException("删除文件失败: " + relativePath, e);
    }
  }

  @Override
  public boolean exists(String relativePath) {
    FilePathSupport.validateRelativePath(relativePath);
    try {
      return storage.objectExists(relativePath);
    } catch (Exception e) {
      throw new FileStorageException("检查文件是否存在失败: " + relativePath, e);
    }
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

  private void invokeBefore(FileUploadBeforeContext before) {
    for (FileUploadHook h : hooks) {
      try {
        h.beforeUpload(before);
      } catch (RuntimeException ex) {
        invokeOnError(before, ex, null);
        throw ex;
      }
    }
  }

  private void invokeAfter(String relativePath, FileUploadBeforeContext before) {
    FileUploadAfterContext after = new FileUploadAfterContext(relativePath, before);
    for (FileUploadHook h : hooks) {
      try {
        h.afterUpload(after);
      } catch (RuntimeException ex) {
        try {
          storage.remove(relativePath);
        } catch (Exception ignore) {
          // 避免掩盖原始 afterUpload 异常
        }
        invokeOnError(before, ex, relativePath);
        throw ex;
      }
    }
  }

  private void invokeOnError(FileUploadBeforeContext before, Throwable error, String relativePath) {
    FileUploadErrorContext ctx = new FileUploadErrorContext(before, error, relativePath);
    for (FileUploadHook h : hooks) {
      try {
        h.onError(ctx);
      } catch (RuntimeException ignore) {
        // 不掩盖原始异常
      }
    }
  }

  private static FileStorageException wrap(Exception e) {
    if (e instanceof FileStorageException f) {
      return f;
    }
    return new FileStorageException(e.getMessage() != null ? e.getMessage() : "上传失败", e);
  }

  private static boolean isAbsoluteUrl(String s) {
    String t = s.toLowerCase();
    return t.startsWith("http://") || t.startsWith("https://");
  }
}
