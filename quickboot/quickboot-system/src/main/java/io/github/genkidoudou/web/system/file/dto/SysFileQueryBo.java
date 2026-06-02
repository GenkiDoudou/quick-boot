package io.github.genkidoudou.web.system.file.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件管理分页查询条件。
 */
@Data
public class SysFileQueryBo {

    private Integer pageNum;

    private Integer pageSize;

    /** 分类（精确）。 */
    private String classify;

    /** 原始文件名（模糊）。 */
    private String originalName;

    /** 上传人用户名（模糊）。 */
    private String uploaderUserName;

    /** 上传时间起。 */
    private LocalDateTime uploadTimeFrom;

    /** 上传时间止。 */
    private LocalDateTime uploadTimeTo;

    /**
     * 是否删除：0 否，1 是。默认 null 表示只查未删除。
     */
    private Integer deleted;
}

