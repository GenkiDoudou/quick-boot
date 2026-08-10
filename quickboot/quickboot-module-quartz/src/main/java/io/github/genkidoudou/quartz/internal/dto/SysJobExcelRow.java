package io.github.genkidoudou.quartz.internal.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 定时任务导出行。
 */
@Data
public class SysJobExcelRow {

    @ExcelProperty("任务编号")
    private Long jobId;

    @ExcelProperty("任务名称")
    private String jobName;

    @ExcelProperty("任务组名")
    private String jobGroup;

    @ExcelProperty("调用目标")
    private String invokeTarget;

    @ExcelProperty("Cron表达式")
    private String cronExpression;

    @ExcelProperty("状态")
    private String status;
}
