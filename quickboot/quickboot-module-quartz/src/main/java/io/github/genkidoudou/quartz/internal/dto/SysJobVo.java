package io.github.genkidoudou.quartz.internal.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 定时任务视图对象。
 */
@Data
public class SysJobVo {

    private Long jobId;

    private String jobName;

    private String jobGroup;

    private String invokeTarget;

    private String cronExpression;

    private String misfirePolicy;

    private String concurrent;

    private String status;

    private String params;

    private String remark;

    private LocalDateTime createTime;

    /** 详情：未来几次执行时间（换行分隔）。 */
    private String nextTimes;

    /** Cron 语义说明（避免秒字段 * 误解为每分钟）。 */
    private String cronDescription;
}
