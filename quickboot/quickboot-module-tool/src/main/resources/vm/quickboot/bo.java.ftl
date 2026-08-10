package ${packageName}.internal.dto;

import lombok.Data;

/**
 * ${tableComment!} 保存请求。
 */
@Data
public class ${className}Bo {

<#list editColumns as col>
    <#if col.isPk != "1">
    private ${col.javaType} ${col.javaField};
    </#if>
</#list>
}
