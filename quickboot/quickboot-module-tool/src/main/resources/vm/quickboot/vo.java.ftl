package ${packageName}.internal.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
<#list columns as col>
<#if col.javaType == "LocalDateTime">
import java.time.LocalDateTime;
<#break>
</#if>
</#list>
import java.util.List;

/**
 * ${tableComment!} 视图对象（列表/表单/查询共用；Entity 不出现在 API）。
 */
@Data
@Schema(description = "${tableComment!}")
public class ${className}Vo {

<#list columns as col>
  /** ${col.columnComment!} */
  private ${col.javaType} ${col.javaField};

</#list>
  /** 导出勾选主键（非表字段） */
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  @JsonIgnore(false)
  private List<<#if pkColumn??>${pkColumn.javaType}<#else>Long</#if>> ids;
}
