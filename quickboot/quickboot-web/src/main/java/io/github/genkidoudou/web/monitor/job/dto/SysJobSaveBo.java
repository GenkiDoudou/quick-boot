package io.github.genkidoudou.web.monitor.job.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 定时任务新增/修改载荷。
 */
@Data
public class SysJobSaveBo {

    private Long jobId;

    @NotBlank(message = "任务名称不能为空")
    @Size(max = 64, message = "任务名称过长")
    private String jobName;

    @NotBlank(message = "任务组名不能为空")
    @Size(max = 64, message = "任务组名过长")
    private String jobGroup;

    @NotBlank(message = "调用目标不能为空")
    @Size(max = 500, message = "调用目标过长")
    private String invokeTarget;

    @NotBlank(message = "Cron 表达式不能为空")
    @Size(max = 255, message = "Cron 表达式过长")
    private String cronExpression;

    @NotBlank(message = "错失策略不能为空")
    private String misfirePolicy;

    @NotBlank(message = "并发策略不能为空")
    private String concurrent;

    private String status;

    @Size(max = 500, message = "参数过长")
    private String params;

    @Size(max = 500, message = "备注过长")
    private String remark;
}
