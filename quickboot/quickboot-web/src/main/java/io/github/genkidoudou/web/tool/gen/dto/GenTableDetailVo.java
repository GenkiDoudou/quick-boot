package io.github.genkidoudou.web.tool.gen.dto;

import lombok.Data;

import java.util.List;

/**
 * 代码生成表详情（含列配置）。
 */
@Data
public class GenTableDetailVo {

    private GenTableVo info;
    private List<GenTableColumnVo> columns;
}
