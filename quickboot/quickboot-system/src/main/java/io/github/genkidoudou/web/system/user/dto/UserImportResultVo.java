package io.github.genkidoudou.web.system.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户导入结果摘要。
 */
@Data
@Schema(description = "用户导入结果")
public class UserImportResultVo {

    @Schema(description = "总行数")
    private int total;

    @Schema(description = "成功行数")
    private int success;

    @Schema(description = "失败行数")
    private int failure;

    @Schema(description = "失败摘要（便于 toast）")
    private List<String> failureMessages = new ArrayList<>();

    @Schema(description = "失败明细下载键；无失败时为 null")
    private String errorKey;
}
