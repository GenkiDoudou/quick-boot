package ${packageName}.${moduleName}.dto;

import lombok.Data;
<#list columns as col>
<#if col.javaType == "LocalDateTime">
import java.time.LocalDateTime;
<#break>
</#if>
</#list>

/**
 * ${tableComment!} 视图对象。
 */
@Data
public class ${className}Vo {

<#list columns as col>
    private ${col.javaType} ${col.javaField};
</#list>
}
