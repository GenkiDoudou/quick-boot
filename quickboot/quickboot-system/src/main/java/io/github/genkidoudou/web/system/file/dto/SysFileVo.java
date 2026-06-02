package io.github.genkidoudou.web.system.file.dto;

import io.github.genkidoudou.common.file.url.FileUrl;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件管理列表 VO（展示字段 + 操作所需 fileId、relativePath）。
 */
@Data
public class SysFileVo {

    private Long fileId;

    /** 存储相对路径（FileTemplate 返回值），预览/下载接口入参。 */
    @FileUrl
    private String relativePath;

    private String originalName;

    /** 上传分类。 */
    private String classify;

    private String ext;

    private Long sizeBytes;

    private String uploaderUserName;

    private LocalDateTime uploadTime;
}

