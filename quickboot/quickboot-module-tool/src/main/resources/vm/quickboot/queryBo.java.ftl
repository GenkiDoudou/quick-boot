package ${packageName}.internal.dto;

import lombok.Data;

/**
 * ${tableComment!} 查询条件。
 */
@Data
public class ${className}QueryBo {

    private Integer pageNum = 1;
    private Integer pageSize = 10;
<#list queryColumns as col>
    private ${col.javaType} ${col.javaField};
</#list>
}
