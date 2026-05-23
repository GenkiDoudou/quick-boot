package com.su60.quickboot.data.file.validation;

import com.su60.quickboot.data.file.FileProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;

/**
 * 文件校验服务
 *
 * @author luyanan
 * @since 2025/01/07
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileValidationService {

    private final FileProperties fileProperties;

    /**
     * 校验上传文件
     *
     * @param file 上传文件
     * @param classify 文件分类
     * @throws FileValidationException 校验失败异常
     */
    public void validateFile(MultipartFile file, String classify) throws FileValidationException {
        FileProperties.FileValidationProperties validation = fileProperties.getValidation();
        
        if (!validation.getEnabled()) {
            return;
        }

        // 1. 空文件检查
        if (file.isEmpty() && !validation.getAllowEmptyFile()) {
            throw new FileValidationException("文件不能为空");
        }

        // 2. 文件大小检查
        validateFileSize(file, validation);

        // 3. 文件名安全检查（路径穿越）
        validateFileName(file.getOriginalFilename());

        // 4. 文件后缀检查
        validateFileExtension(file.getOriginalFilename(), validation);

        // 5. MIME 类型检查
        validateMimeType(file, validation);

        // 6. 文件头魔数检查（可选）
        if (validation.getCheckFileMagic()) {
            validateFileMagic(file);
        }

        log.debug("文件校验通过: {}", file.getOriginalFilename());
    }

    /**
     * 校验文件大小
     */
    private void validateFileSize(MultipartFile file, FileProperties.FileValidationProperties validation) 
            throws FileValidationException {
        long fileSize = file.getSize();
        long maxSize = validation.getMaxSize();
        
        if (fileSize > maxSize) {
            throw new FileValidationException(
                String.format("文件大小超出限制，当前: %d bytes，最大允许: %d bytes", fileSize, maxSize)
            );
        }
    }

    /**
     * 校验文件名（防止路径穿越攻击）
     */
    private void validateFileName(String fileName) throws FileValidationException {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new FileValidationException("文件名不能为空");
        }

        // 检查危险字符
        String[] dangerousChars = {"..", "/", "\\", ":", "*", "?", "\"", "<", ">", "|"};
        for (String dangerous : dangerousChars) {
            if (fileName.contains(dangerous)) {
                throw new FileValidationException("文件名包含非法字符: " + dangerous);
            }
        }

        // 检查路径穿越
        Path path = Paths.get(fileName).normalize();
        if (path.startsWith("..") || path.toString().contains("..")) {
            throw new FileValidationException("文件名存在路径穿越风险");
        }
    }

    /**
     * 校验文件后缀
     */
    private void validateFileExtension(String fileName, FileProperties.FileValidationProperties validation) 
            throws FileValidationException {
        List<String> allowedExtensions = validation.getAllowedExtensions();
        if (allowedExtensions == null || allowedExtensions.isEmpty()) {
            return; // 未配置则跳过
        }

        if (fileName == null || !fileName.contains(".")) {
            throw new FileValidationException("文件必须包含扩展名");
        }

        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase(Locale.ROOT);
        if (!allowedExtensions.contains(extension)) {
            throw new FileValidationException(
                String.format("不支持的文件类型: %s，允许的类型: %s", extension, allowedExtensions)
            );
        }
    }

    /**
     * 校验 MIME 类型
     */
    private void validateMimeType(MultipartFile file, FileProperties.FileValidationProperties validation) 
            throws FileValidationException {
        List<String> allowedMimeTypes = validation.getAllowedMimeTypes();
        if (allowedMimeTypes == null || allowedMimeTypes.isEmpty()) {
            return; // 未配置则跳过
        }

        String mimeType = file.getContentType();
        if (mimeType == null || !allowedMimeTypes.contains(mimeType)) {
            throw new FileValidationException(
                String.format("不支持的 MIME 类型: %s，允许的类型: %s", mimeType, allowedMimeTypes)
            );
        }
    }

    /**
     * 校验文件头魔数（更安全的文件类型检测）
     */
    private void validateFileMagic(MultipartFile file) throws FileValidationException {
        try {
            byte[] header = new byte[8];
            int bytesRead = file.getInputStream().read(header);
            
            if (bytesRead < 4) {
                throw new FileValidationException("文件头信息不足，无法进行类型检测");
            }

            // 常见文件类型的魔数检测
            if (isImageFile(header)) {
                return;
            }
            if (isPdfFile(header)) {
                return;
            }
            if (isOfficeFile(header)) {
                return;
            }

            // 如果无法识别文件类型，记录警告但不阻止上传
            log.warn("无法识别文件类型，文件: {}", file.getOriginalFilename());
            
        } catch (IOException e) {
            throw new FileValidationException("文件读取失败: " + e.getMessage());
        }
    }

    /**
     * 检查是否为图片文件
     */
    private boolean isImageFile(byte[] header) {
        // JPEG: FF D8 FF
        if (header[0] == (byte) 0xFF && header[1] == (byte) 0xD8 && header[2] == (byte) 0xFF) {
            return true;
        }
        // PNG: 89 50 4E 47
        if (header[0] == (byte) 0x89 && header[1] == 0x50 && header[2] == 0x4E && header[3] == 0x47) {
            return true;
        }
        // GIF: 47 49 46 38
        if (header[0] == 0x47 && header[1] == 0x49 && header[2] == 0x46 && header[3] == 0x38) {
            return true;
        }
        return false;
    }

    /**
     * 检查是否为 PDF 文件
     */
    private boolean isPdfFile(byte[] header) {
        // PDF: 25 50 44 46
        return header[0] == 0x25 && header[1] == 0x50 && header[2] == 0x44 && header[3] == 0x46;
    }

    /**
     * 检查是否为 Office 文件
     */
    private boolean isOfficeFile(byte[] header) {
        // ZIP-based Office files (docx, xlsx, pptx): 50 4B 03 04
        if (header[0] == 0x50 && header[1] == 0x4B && header[2] == 0x03 && header[3] == 0x04) {
            return true;
        }
        // Legacy Office files: D0 CF 11 E0
        if (header[0] == (byte) 0xD0 && header[1] == (byte) 0xCF && header[2] == 0x11 && header[3] == (byte) 0xE0) {
            return true;
        }
        return false;
    }
}
