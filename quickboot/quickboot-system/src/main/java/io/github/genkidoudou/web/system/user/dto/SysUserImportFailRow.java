package io.github.genkidoudou.web.system.user.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户导入失败行导出模型。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysUserImportFailRow {

    @ExcelProperty("登录名称")
    private String userName;

    @ExcelProperty("失败原因")
    private String reason;
}
