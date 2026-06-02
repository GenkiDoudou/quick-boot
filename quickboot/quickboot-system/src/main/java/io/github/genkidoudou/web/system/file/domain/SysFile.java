package io.github.genkidoudou.web.system.file.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统文件元数据，与表 {@code sys_file} 对应。
 */
@Data
@TableName("sys_file")
public class SysFile implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "file_id", type = IdType.ASSIGN_ID)
    private Long fileId;

    /** 原始文件名。 */
    private String originalName;

    /** 扩展名（小写，不含点）。 */
    private String ext;

    /** 文件大小（字节）。 */
    private Long sizeBytes;

    /** Content-Type，可空。 */
    private String contentType;

    /** 分类（FileTemplate classify）。 */
    private String classify;

    /** 相对路径（FileTemplate.upload 返回值），唯一。 */
    private String relativePath;

    /** 上传人用户ID（无登录态为 0）。 */
    private Long uploaderUserId;

    /** 上传人用户名（无登录态为空）。 */
    private String uploaderUserName;

    /** 上传时间。 */
    private LocalDateTime uploadTime;

    /** 是否删除：0否 1是。 */
    private Integer deleted;

    /** 删除人。 */
    private String deleteBy;

    /** 删除时间。 */
    private LocalDateTime deleteTime;
}

