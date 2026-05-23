package io.github.genkidoudou.web.monitor.job.quartz;

import io.github.genkidoudou.web.monitor.job.domain.SysJob;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * Quartz JobDataMap 中的任务快照（可序列化）。
 */
@Data
public class JobTaskSnapshot implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public static final String TASK_PROPERTIES = "TASK_PROPERTIES";

    private Long jobId;
    private String jobName;
    private String jobGroup;
    private String invokeTarget;
    private String cronExpression;
    private String misfirePolicy;
    private String concurrent;
    private String status;
    private String params;

    /**
     * 从领域对象构建快照。
     */
    public static JobTaskSnapshot from(SysJob job) {
        JobTaskSnapshot s = new JobTaskSnapshot();
        s.setJobId(job.getJobId());
        s.setJobName(job.getJobName());
        s.setJobGroup(job.getJobGroup());
        s.setInvokeTarget(job.getInvokeTarget());
        s.setCronExpression(job.getCronExpression());
        s.setMisfirePolicy(job.getMisfirePolicy());
        s.setConcurrent(job.getConcurrent());
        s.setStatus(job.getStatus());
        s.setParams(job.getParams());
        return s;
    }
}
