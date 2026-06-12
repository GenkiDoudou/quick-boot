package io.github.genkidoudou.web.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识库文档列表/详情出参。
 */
@Data
@Schema(description = "知识库文档视图对象")
public class KbDocumentVo {

    @Schema(description = "文档ID")
    private Long docId;

    @Schema(description = "知识库ID")
    private Long kbId;

    @Schema(description = "来源类型：FILE/MANUAL/WEB/LIBRARY")
    private String sourceType;

    @Schema(description = "关联文件ID")
    private Long fileId;

    @Schema(description = "文档库文件ID（LIBRARY 来源）")
    private Long libraryFileId;

    @Schema(description = "网页来源 URL")
    private String sourceUrl;

    @Schema(description = "展示标题")
    private String title;

    @Schema(description = "入库快照：分段模式 AUTO/CUSTOM")
    private String segmentMode;

    @Schema(description = "入库快照：分块 token 上限")
    private Integer chunkSize;

    @Schema(description = "入库快照：分块重叠 token")
    private Integer chunkOverlap;

    @Schema(description = "入库快照：分隔符")
    private String chunkDelimiter;

    @Schema(description = "入库快照：归一化空白 0/1")
    private Integer preprocessNormalizeWs;

    @Schema(description = "入库快照：删除 URL 0/1")
    private Integer preprocessRemoveUrl;

    @Schema(description = "入库快照：删除邮箱 0/1")
    private Integer preprocessRemoveEmail;

    @Schema(description = "入库状态：PENDING/PARSING/INDEXED/FAILED")
    private String docStatus;

    @Schema(description = "成功入库分块数")
    private Integer chunkCount;

    @Schema(description = "失败原因")
    private String errorMsg;

    @Schema(description = "创建人")
    private String createBy;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
