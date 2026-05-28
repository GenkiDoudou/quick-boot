package io.github.genkidoudou.web.system.user.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户导出行模型。
 */
@Data
public class SysUserExcelRow {

    @ExcelProperty("用户编号")
    private Long userId;

    @ExcelProperty("登录账号")
    private String userName;

    @ExcelProperty("用户昵称")
    private String nickName;

    @ExcelProperty("部门")
    private String deptName;

    @ExcelProperty("手机号码")
    private String phonenumber;

    @ExcelProperty("状态")
    private String statusLabel;

    @ExcelProperty("角色")
    private String roleNames;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;
}
