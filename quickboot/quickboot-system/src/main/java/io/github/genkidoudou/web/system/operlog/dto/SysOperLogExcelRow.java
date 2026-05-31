package io.github.genkidoudou.web.system.operlog.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import io.github.genkidoudou.common.excel.annotation.ExcelDictFormat;
import io.github.genkidoudou.common.excel.convert.ExcelDictConverter;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志导出 Excel 行。
 */
@Data
public class SysOperLogExcelRow {

    @ExcelProperty("日志编号")
    private Long operId;

    @ExcelProperty("系统模块")
    private String title;

    @ExcelProperty(value = "业务类型", converter = ExcelDictConverter.class)
    @ExcelDictFormat(dictType = "sys_oper_business_type")
    private String businessType;

    @ExcelProperty("方法")
    private String method;

    @ExcelProperty("请求方式")
    private String requestMethod;

    @ExcelProperty("操作人员")
    private String operName;

    @ExcelProperty("IP")
    private String operIp;

    @ExcelProperty(value = "状态", converter = ExcelDictConverter.class)
    @ExcelDictFormat(dictType = "sys_oper_status")
    private String status;

    @ExcelProperty("操作时间")
    private LocalDateTime operTime;

    @ExcelProperty("耗时(ms)")
    private Long costTime;

    @ExcelProperty("链路ID")
    private String traceId;

    @ExcelProperty("操作ID")
    private String clientOperationId;

    @ExcelProperty("客户端ID")
    private String clientId;
}
