package io.github.genkidoudou.web.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 批量 / ZIP 文档上传响应。
 */
@Data
@Schema(description = "批量文档上传响应")
public class KbDocumentBatchUploadVo {

    @Schema(description = "成功提交入库的文档数量")
    private int total;

    @Schema(description = "各文档上传结果")
    private List<KbDocumentUploadVo> items = new ArrayList<>();

    @Schema(description = "跳过的 ZIP 内文件及原因")
    private List<String> skipped = new ArrayList<>();
}
