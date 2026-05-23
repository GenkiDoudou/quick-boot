package io.github.genkidoudou.web.monitor.job.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 调度日志导出行。
 */
@Data
public class SysJobLogExcelRow {

    @ExcelProperty("日志编号")
    private Long jobLogId;

    @ExcelProperty("任务名称")
    private String jobName;

    @ExcelProperty("任务组名")
    private String jobGroup;

    @ExcelProperty("调用目标")
    private String invokeTarget;

    @ExcelProperty("日志信息")
    private String jobMessage;

    @ExcelProperty("执行状态")
    private String status;

    @ExcelProperty("执行时间")
    private String createTime;
}
