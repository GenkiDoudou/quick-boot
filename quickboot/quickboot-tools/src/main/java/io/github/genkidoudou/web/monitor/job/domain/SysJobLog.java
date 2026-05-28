package io.github.genkidoudou.web.monitor.job.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 定时任务调度日志，表 {@code sys_job_log}。
 */
@Data
@TableName("sys_job_log")
public class SysJobLog implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "job_log_id", type = IdType.ASSIGN_ID)
    private Long jobLogId;

    private Long jobId;

    private String jobName;

    private String jobGroup;

    private String invokeTarget;

    private String jobMessage;

    /** 0 成功，1 失败。 */
    private String status;

    private String exceptionInfo;

    private LocalDateTime createTime;
}
