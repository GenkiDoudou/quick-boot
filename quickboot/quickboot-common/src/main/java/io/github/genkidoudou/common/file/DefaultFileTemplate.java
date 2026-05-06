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

import io.github.genkidoudou.common.file.url.FileUrlSupport;

/**
 * {@link FileTemplate} 默认实现：分类校验、路径规则、钩子与存储委派。
 */
public class DefaultFileTemplate implements FileTemplate {

    private final QcFileProperties props;
    private final FileStorageOperations storage;
    private final List<FileUploadHook> hooks;

    public DefaultFileTemplate(QcFileProperties props, FileStorageOperations storage, List<FileUploadHook> hooks) {
        this.props = props;
        this.storage = storage;
        this.hooks = new ArrayList<>(hooks);
        this.hooks.sort(AnnotationAwareOrderComparator.INSTANCE);
    }

    @Override
    public String upload(MultipartFile file, String classify) {
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("上传文件为空");
        }
        String classifyKey = FilePathSupport.normalizeClassifyKey(classify, props.getDefaultClassify());
        QcFileProperties.ClassifyProperties rule = resolveRule(classifyKey);
        String ext = FilePathSupport.normalizeExtension(file.getOriginalFilename());
        FilePathSupport.validateAgainstRule(ext, file.getSize(), rule);
        String relativePath = FilePathSupport.buildRelativePath(classifyKey, ext);
        FileUploadBeforeContext before = new FileUploadBeforeContext(file, classifyKey);
        invokeBefore(before);
        try (InputStream in = file.getInputStream()) {
            storage.put(relativePath, in, file.getSize(), file.getContentType());
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
            throw new FileStorageException("上传内容为空");
        }
        String classifyKey = FilePathSupport.normalizeClassifyKey(classify, props.getDefaultClassify());
        QcFileProperties.ClassifyProperties rule = resolveRule(classifyKey);
        String ext = FilePathSupport.normalizeExtension(filename);
        FilePathSupport.validateAgainstRule(ext, content.length, rule);
        String relativePath = FilePathSupport.buildRelativePath(classifyKey, ext);
        String ct = null;
        FileUploadBeforeContext before = new FileUploadBeforeContext(content, filename, classifyKey, ct);
        invokeBefore(before);
        try (InputStream in = new ByteArrayInputStream(content)) {
            storage.put(relativePath, in, content.length, ct);
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

    private QcFileProperties.ClassifyProperties resolveRule(String classifyKey) {
        return props.getClassifies().stream()
                .filter(c -> classifyKey.equals(c.getClassify()))
                .findFirst()
                .orElse(null);
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
            h.afterUpload(after);
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
