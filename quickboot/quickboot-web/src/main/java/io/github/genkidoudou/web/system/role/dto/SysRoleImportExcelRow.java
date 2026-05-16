package io.github.genkidoudou.web.system.role.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import io.github.genkidoudou.common.excel.annotation.ExcelDictFormat;
import io.github.genkidoudou.common.excel.convert.ExcelDictConverter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 角色 Excel 导入行模型（与导入模板列头一致）。
 */
@Data
public class SysRoleImportExcelRow {

    @NotBlank(message = "角色名称不能为空")
    @Size(max = 30, message = "角色名称长度不能超过30")
    @ExcelProperty("角色名称")
    private String roleName;

    @NotBlank(message = "权限字符不能为空")
    @Size(max = 100, message = "权限字符长度不能超过100")
    @ExcelProperty("权限字符")
    private String roleKey;

    @ExcelProperty("显示顺序")
    private Integer roleSort;

    @ExcelProperty(value = "状态", converter = ExcelDictConverter.class)
    @ExcelDictFormat(dictType = "sys_normal_disable")
    private String status;

    /**
     * 可为空（默认全部）；支持 1–5 或中文：全部/自定义/本部门/本部门及以下/仅本人。
     */
    @ExcelProperty("数据范围")
    private String dataScope;

    @Size(max = 500, message = "备注长度不能超过500")
    @ExcelProperty("备注")
    private String remark;
}
