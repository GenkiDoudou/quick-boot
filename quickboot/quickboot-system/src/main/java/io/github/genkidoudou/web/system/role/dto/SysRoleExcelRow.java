package io.github.genkidoudou.web.system.role.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色导出行模型。
 */
@Data
public class SysRoleExcelRow {

    @ExcelProperty("角色编号")
    private Long roleId;

    @ExcelProperty("角色名称")
    private String roleName;

    @ExcelProperty("权限字符")
    private String roleKey;

    @ExcelProperty("显示顺序")
    private Integer roleSort;

    @ExcelProperty("状态")
    private String statusLabel;

    @ExcelProperty("数据范围")
    private String dataScopeLabel;

    @ExcelProperty("备注")
    private String remark;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;
}
