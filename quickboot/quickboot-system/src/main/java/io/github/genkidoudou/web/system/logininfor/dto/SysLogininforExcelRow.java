package io.github.genkidoudou.web.system.logininfor.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录日志导出行。
 */
@Data
public class SysLogininforExcelRow {
    @ExcelProperty("访问编号")
    private Long infoId;

    @ExcelProperty("用户名")
    private String userName;

    @ExcelProperty("IP")
    private String ipaddr;

    @ExcelProperty("登录地点")
    private String loginLocation;

    @ExcelProperty("操作系统")
    private String os;

    @ExcelProperty("浏览器")
    private String browser;

    @ExcelProperty("状态")
    private String status;

    @ExcelProperty("描述")
    private String msg;

    @ExcelProperty("访问时间")
    private LocalDateTime loginTime;
}
