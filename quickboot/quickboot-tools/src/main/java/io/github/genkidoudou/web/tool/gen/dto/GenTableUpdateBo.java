package io.github.genkidoudou.web.tool.gen.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 保存代码生成配置请求体。
 */
@Data
public class GenTableUpdateBo {

    @NotNull(message = "表ID不能为空")
    private Long tableId;

    @NotBlank(message = "表名称不能为空")
    private String tableName;

    private String tableComment;

    @NotBlank(message = "实体类名称不能为空")
    private String className;

    private String tplCategory;
    /** 前端模板：c7 / element-plus */
    private String tplWebType;
    private String packageName;
    private String moduleName;
    private String businessName;
    private String functionName;
    private String functionAuthor;
    private String genType;
    private String genPath;
    private Long parentMenuId;
    private String treeCode;
    private String treeParentCode;
    private String treeName;
    private String remark;

    @Valid
    @NotNull(message = "字段配置不能为空")
    private List<GenTableColumnVo> columns;
}
