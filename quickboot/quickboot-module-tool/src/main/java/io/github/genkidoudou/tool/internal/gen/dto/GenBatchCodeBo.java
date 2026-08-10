package io.github.genkidoudou.tool.internal.gen.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 批量下载生成代码请求。
 */
@Data
public class GenBatchCodeBo {

    @NotBlank(message = "tables 不能为空")
    private String tables;
}
