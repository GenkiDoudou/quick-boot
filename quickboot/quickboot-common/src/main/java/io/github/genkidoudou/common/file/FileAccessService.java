package io.github.genkidoudou.common.file;

import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import cn.dev33.satoken.stp.StpUtil;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.common.file.url.FileUrlSupport;

/**
 * 通用文件上传/预览门面：分类配置校验、绝对路径拼装；{@code anonymous} 仅控制上传是否可匿名。
 */
@Service
public class FileAccessService {

    private final QcFileProperties props;
    private final FileTemplate fileTemplate;

    public FileAccessService(QcFileProperties props, FileTemplate fileTemplate) {
        this.props = props;
        this.fileTemplate = fileTemplate;
    }

    /**
     * 返回已配置的上传分类列表（供前端下拉）。
     */
    public List<FileClassifyVo> listClassifies() {
        List<FileClassifyVo> list = new ArrayList<>();
        for (QcFileProperties.ClassifyProperties c : props.getClassifies()) {
            if (c == null || !StringUtils.hasText(c.getClassify())) {
                continue;
            }
            list.add(toClassifyVo(c));
        }
        return list;
    }

    /**
     * 按分类名返回上传规则（供上传组件动态拉取类型、大小、个数等限制）。
     *
     * @param classify 分类名
     */
    public FileClassifyVo getClassify(String classify) {
        String classifyKey = requireClassifyKey(classify);
        return toClassifyVo(requireClassifyRule(classifyKey));
    }

    /**
     * 按配置分类上传；成功后触发 {@link FileUploadHook}（如 sys_file 登记）。
     *
     * @param file     文件
     * @param classify 分类名（必填，须在 {@code qc.file.classifies} 中配置）
     */
    public FileUploadResult upload(MultipartFile file, String classify) {
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("上传文件为空");
        }
        String classifyKey = requireClassifyKey(classify);
        QcFileProperties.ClassifyProperties rule = requireClassifyRule(classifyKey);
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
     * 登录与否由 Controller 层 {@code @SaIgnore} 或 {@code qc.security.web.anonymous-paths} 控制；
     * 勿在此调用 {@link StpUtil#checkLogin()}，否则与 {@code @SaIgnore} 冲突。
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
        String contentType = guessContentType(fileName);
        return new PreviewPayload(resource, fileName, contentType);
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
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "分类 classify 不能为空");
        }
        return FilePathSupport.normalizeClassifyKey(classify.trim(), classify.trim());
    }

    private FileClassifyVo toClassifyVo(QcFileProperties.ClassifyProperties c) {
        FileClassifyVo vo = new FileClassifyVo();
        vo.setClassify(c.getClassify().trim());
        vo.setLimitExt(c.getLimitExt());
        vo.setLimitSize(c.getLimitSize() != null ? c.getLimitSize() : DataSize.ofMegabytes(10));
        vo.setLimitSizeBytes(c.resolveLimitSizeBytes());
        vo.setLimitCount(c.getLimitCount() > 0 ? c.getLimitCount() : 1);
        vo.setAnonymous(c.isAnonymous());
        return vo;
    }

    private QcFileProperties.ClassifyProperties requireClassifyRule(String classifyKey) {
        QcFileProperties.ClassifyProperties rule = props.getClassifies().stream()
            .filter(c -> c != null && classifyKey.equals(c.getClassify()))
            .findFirst()
            .orElse(null);
        if (rule == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "未配置的上传分类: " + classifyKey);
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
