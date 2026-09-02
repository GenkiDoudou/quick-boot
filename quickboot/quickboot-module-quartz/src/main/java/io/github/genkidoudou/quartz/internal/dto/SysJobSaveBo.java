package io.github.genkidoudou.quartz.internal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 定时任务新增/修改载荷。
 * <p>
 * Bean 模式使用 {@link #invokeTarget} + {@link #params}；
 * HTTP / Script 模式使用 {@link #httpConfig} / {@link #scriptConfig} 结构化字段（后端写入 invoke_target/params）。
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

    /** 任务类型：0 Bean，1 HTTP，2 Script；默认 0。 */
    private String jobType = "0";

    /** Bean 模式：ITask Bean 名称。 */
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

    /** Bean 模式可选参数字符串。 */
    @Size(max = 2000, message = "参数过长")
    private String params;

    @Size(max = 500, message = "备注过长")
    private String remark;

    /** HTTP 模式结构化配置。 */
    private JobHttpConfigBo httpConfig;

    /** Script 模式结构化配置。 */
    private JobScriptConfigBo scriptConfig;
}
