package io.github.genkidoudou.web.system.file.dto;

import lombok.Data;

/**
 * 文件管理上传返回值。
 */
@Data
public class SysFileUploadVo {

    private Long fileId;

    /** 原始文件名。 */
    private String fileName;

    private String relativePath;

    /** 对外可访问的绝对 URL（由 qc.file.domain / qc.file.viewUrlBase 拼接）。 */
    private String absolutePath;
}

