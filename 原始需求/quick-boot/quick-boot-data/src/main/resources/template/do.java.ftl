package ${package}.dos;

import cn.t200.quickboot.common.validation.AddGroup;
import cn.t200.quickboot.common.validation.UpdateGroup;
import lombok.Data;
import lombok.experimental.Accessors;
import java.io.Serial;
import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

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
public class ${className}Do implements Serializable{
@Serial
private static final long serialVersionUID = 1L;

<#-- ----------  BEGIN 字段循环遍历  ---------->
<#list tableFields as field>


    /**
    * ${field.columnComment}
    * @since ${date}
    */
       <#if field.isPk == '0' && field.isRequired == '0' >
     @NotNull(groups = UpdateGroup.class, message = "${field.columnComment} 不能为空")
       <#elseif field.isPk == '0' && field.isRequired == '0' && field.attrType != 'String' >
     @NotNull(groups = AddGroup.class, message = "${field.columnComment}不能为空")
      <#elseif field.isRequired == '0'  && field.attrType == 'String'>
      @NotBlank(groups = AddGroup.class, message = "${field.columnComment}不能为空")
    </#if>
    <#if field.attrName == 'createBy'  || field.attrName == 'updateTime' || field.attrName == 'updateBy' || field.attrName == 'delFlag' >
        @JsonIgnore
    </#if>
       private ${field.attrType} ${field.attrName};


</#list>

}
