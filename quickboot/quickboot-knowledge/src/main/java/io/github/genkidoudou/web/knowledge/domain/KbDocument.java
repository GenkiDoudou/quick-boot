package io.github.genkidoudou.web.knowledge.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 知识库文档实体，对应表 {@code kb_document}。
 */
@Data
@TableName("kb_document")
public class KbDocument implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "doc_id", type = IdType.ASSIGN_ID)
    private Long docId;

    /** 所属知识库 ID。 */
    private Long kbId;

    /**
     * 文档来源类型：FILE / MANUAL / WEB / LIBRARY。
     *
     * @see io.github.genkidoudou.web.knowledge.constants.KbDocSourceType
     */
    private String sourceType;

    /** 关联 {@code sys_file.file_id}；手动/网页来源归档后亦有值，可为空。 */
    private Long fileId;

    /** 来源为文档库时关联 {@code kb_doc_library_file.lib_file_id}。 */
    private Long libraryFileId;

    /** 网页来源原始 URL。 */
    private String sourceUrl;

    /**
     * 入库时快照的分段模式：AUTO / CUSTOM。
     *
     * @see io.github.genkidoudou.web.knowledge.constants.KbSegmentMode
     */
    private String segmentMode;

    /** 入库时快照的分块 token 上限。 */
    private Integer chunkSize;

    /** 入库时快照的分块重叠 token 数。 */
    private Integer chunkOverlap;

    /**
     * 入库时快照的自定义分隔符：SINGLE_NEWLINE / DOUBLE_NEWLINE。
     *
     * @see io.github.genkidoudou.web.knowledge.constants.KbChunkDelimiter
     */
    private String chunkDelimiter;

    /** 入库时快照：是否归一化连续空白，0 否 / 1 是。 */
    private Integer preprocessNormalizeWs;

    /** 入库时快照：是否删除 URL，0 否 / 1 是。 */
    private Integer preprocessRemoveUrl;

    /** 入库时快照：是否删除电子邮箱，0 否 / 1 是。 */
    private Integer preprocessRemoveEmail;

    /** 展示标题，默认可取自原始文件名。 */
    private String title;

    /**
     * 文档入库状态：PENDING / PARSING / INDEXED / FAILED。
     *
     * @see io.github.genkidoudou.web.knowledge.constants.KbDocStatus
     */
    private String docStatus;

    /** 成功入库的分块数量。 */
    private Integer chunkCount;

    /** 最近一次入库失败原因；重建/重试时需显式置 null 清空，故 update 策略为 ALWAYS。 */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String errorMsg;

    /** 逻辑删除：0 否 / 1 是（手动维护，非 TableLogic）。 */
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
