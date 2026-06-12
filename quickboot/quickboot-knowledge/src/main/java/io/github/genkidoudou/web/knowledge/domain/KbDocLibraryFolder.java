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
 * 独立文档库目录，对应表 {@code kb_doc_library_folder}。
 */
@Data
@TableName("kb_doc_library_folder")
public class KbDocLibraryFolder implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "folder_id", type = IdType.ASSIGN_ID)
    private Long folderId;

    /** 父目录 ID，0 表示根。 */
    private Long parentId;

    /** 目录名称。 */
    private String name;

    /** 排序号。 */
    private Integer orderNum;

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
