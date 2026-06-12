package io.github.genkidoudou.web.knowledge.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 独立文档库文件条目，对应表 {@code kb_doc_library_file}。
 */
@Data
@TableName("kb_doc_library_file")
public class KbDocLibraryFile implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "lib_file_id", type = IdType.ASSIGN_ID)
    private Long libFileId;

    /** 所属目录 ID。 */
    private Long folderId;

    /** 关联 {@code sys_file.file_id}。 */
    private Long fileId;

    /** 展示标题。 */
    private String title;

    /** 文件扩展名（小写，不含点）。 */
    private String fileExt;

    /** 文件大小（字节）。 */
    private Long fileSize;

    /** 备注。 */
    private String remark;

    /** 逻辑删除：0 否 / 1 是。 */
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
