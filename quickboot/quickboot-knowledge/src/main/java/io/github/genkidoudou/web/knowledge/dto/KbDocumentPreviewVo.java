package io.github.genkidoudou.web.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 知识库文档预览元数据（原文 / 解析文本）。
 */
@Data
@Schema(description = "知识库文档预览信息")
public class KbDocumentPreviewVo {

    @Schema(description = "文档ID")
    private Long docId;

    @Schema(description = "展示标题")
    private String title;

    @Schema(description = "来源类型")
    private String sourceType;

    @Schema(description = "入库状态")
    private String docStatus;

    @Schema(description = "网页来源 URL")
    private String sourceUrl;

    @Schema(description = "文件扩展名（小写）")
    private String fileExt;

    /**
     * 预览模式：PDF / TEXT / MARKDOWN / HTML / OFFICE / WEB / CHUNKS / NONE。
     */
    @Schema(description = "预览模式")
    private String previewMode;

    @Schema(description = "是否支持 previewStream 流式预览（PDF 等）")
    private Boolean streamable;

    @Schema(description = "文本预览内容（原文或分段拼接）")
    private String textContent;

    @Schema(description = "文本是否截断")
    private Boolean textTruncated;

    @Schema(description = "已入库分块数")
    private Integer chunkCount;
}
