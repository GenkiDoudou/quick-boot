package ${package}.entity;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.*;
import lombok.experimental.Accessors;
import java.io.Serial;
import java.io.Serializable;
<#list importPackage as pkg>
import ${pkg};
</#list>

/**
* <p>
* ${tableComment!}
* </p>
* @author ${author}
* @since ${date}
*/
@Data
@Accessors(chain = true)
@TableName("${tableName}")
public class ${className}Entity implements Serializable{
@Serial
private static final long serialVersionUID = 1L;

<#-- ----------  BEGIN 字段循环遍历  ---------->
<#list tableFields as field>
    /**
    * ${field.columnComment}
    * @since ${date}
    */
    <#if field.isPk == '0'>

    <#-- 主键 -->
        @TableId(value = "${field.columnName}", type = IdType.ASSIGN_ID)
    <#elseif field.attrName == 'createTime' || field.attrName == 'createBy' >
        @TableField(value = "${field.columnName}", fill = FieldFill.INSERT)
    <#elseif field.attrName == 'updateTime' || field.attrName == 'updateBy' >
        @TableField(value = "${field.columnName}", fill = FieldFill.INSERT_UPDATE)
    <#elseif field.attrName == 'delFlag' >
        @TableLogic
    <#else >
        @TableField("${field.columnName}")
    </#if>
        private ${field.attrType} ${field.attrName};


</#list>

}
