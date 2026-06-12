package io.github.genkidoudou.web.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 分段预览结果。
 */
@Data
@Schema(description = "分段预览结果")
public class SegmentPreviewVo {

    @Schema(description = "分块总数")
    private Integer total;

    @Schema(description = "是否因上限被截断展示")
    private Boolean truncated;

    @Schema(description = "预览分块列表")
    private List<SegmentPreviewItemVo> segments;
}
