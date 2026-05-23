package io.github.genkidoudou.web.monitor.job.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 定时任务，表 {@code sys_job}。
 */
@Data
@TableName("sys_job")
public class SysJob implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "job_id", type = IdType.ASSIGN_ID)
    private Long jobId;

    private String jobName;

    private String jobGroup;

    private String invokeTarget;

    private String cronExpression;

    private String misfirePolicy;

    /** 0 允许并发，1 禁止。 */
    private String concurrent;

    /** 0 正常，1 暂停。 */
    private String status;

    private String params;

    private String createBy;

    private LocalDateTime createTime;

    private String updateBy;

    private LocalDateTime updateTime;

    private String remark;
}
