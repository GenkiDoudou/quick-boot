package io.github.genkidoudou.tool.internal.gen.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 建表 SQL 请求体。
 */
@Data
public class GenCreateTableBo {

    @NotBlank(message = "SQL 不能为空")
    private String sql;
}
