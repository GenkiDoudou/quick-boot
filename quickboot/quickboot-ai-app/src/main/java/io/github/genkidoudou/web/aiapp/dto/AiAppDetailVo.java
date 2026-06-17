package io.github.genkidoudou.web.aiapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI 应用详情 VO（含 config_json）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "AI 应用详情")
public class AiAppDetailVo extends AiAppVo {

    @Schema(description = "草稿配置 JSON")
    private String configJson;

    @Schema(description = "发布快照 JSON")
    private String publishedConfigJson;
}
