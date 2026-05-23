package ${packageName}.${moduleName}.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
<#list columns as col>
<#if col.javaType == "LocalDateTime">
import java.time.LocalDateTime;
<#break>
</#if>
</#list>

/**
 * ${tableComment!}。
 *
 * @author ${author!}
 */
@Data
@TableName("${tableName}")
public class ${className} implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

<#list columns as col>
    <#if col.isPk == "1">
    @TableId(value = "${col.columnName}", type = IdType.ASSIGN_ID)
    </#if>
    /** ${col.columnComment!} */
    private ${col.javaType} ${col.javaField};

</#list>
}
