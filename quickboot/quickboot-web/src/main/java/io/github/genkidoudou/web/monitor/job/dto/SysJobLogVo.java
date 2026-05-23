package io.github.genkidoudou.web.monitor.job.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 调度日志视图对象。
 */
@Data
public class SysJobLogVo {

    private Long jobLogId;

    private Long jobId;

    private String jobName;

    private String jobGroup;

    private String invokeTarget;

    private String jobMessage;

    private String status;

    private String exceptionInfo;

    private LocalDateTime createTime;
}
