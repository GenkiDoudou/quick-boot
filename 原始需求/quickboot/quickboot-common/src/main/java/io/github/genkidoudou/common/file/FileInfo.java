package io.github.genkidoudou.common.file;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件信息 DTO
 *
 * @author genkidoudou
 * @since 2026/03/11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileInfo {

    /**
     * 相对路径
     */
    private String relativePath;

    /**
     * 完整访问 URL
     */
    private String url;

    /**
     * 原始文件名
     */
    private String originalFilename;

    /**
     * 文件大小（字节）
     */
    private Long size;

    /**
     * 文件扩展名
     */
    private String extension;
}
