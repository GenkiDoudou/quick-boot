package io.github.genkidoudou.web.tool.gen.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 导入表请求体。
 */
@Data
public class GenImportTableBo {

    @NotEmpty(message = "请选择要导入的表")
    private List<String> tables;
}
