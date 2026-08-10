package io.github.genkidoudou.tool.internal.gen.dto;

import lombok.Data;

/**
 * 代码生成列配置 VO。
 */
@Data
public class GenTableColumnVo {

    private Long columnId;
    private Long tableId;
    private String columnName;
    private String columnComment;
    private String columnType;
    private String javaType;
    private String javaField;
    private String isPk;
    private String isIncrement;
    private String isRequired;
    private String isInsert;
    private String isEdit;
    private String isList;
    private String isQuery;
    private String queryType;
    private String htmlType;
    private String dictType;
    private Integer sort;
}
