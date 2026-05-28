package io.github.genkidoudou.web.system.user.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 用户导入 Excel 行模型（与模板列一致）。
 */
@Data
public class SysUserImportExcelRow {

    @ExcelProperty("登录名称")
    private String userName;

    @ExcelProperty("用户昵称")
    private String nickName;

    @ExcelProperty("密码")
    private String password;

    @ExcelProperty("部门编号")
    private Long deptId;

    @ExcelProperty("手机号码")
    private String phonenumber;

    @ExcelProperty("邮箱")
    private String email;

    @ExcelProperty("性别")
    private String sex;

    @ExcelProperty("帐号状态")
    private String status;

    @ExcelProperty("角色权限字符")
    private String roleKeys;
}
