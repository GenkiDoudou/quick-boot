package io.github.genkidoudou.web.monitor.job.quartz;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.web.monitor.job.domain.SysJobLog;
import io.github.genkidoudou.web.monitor.job.service.SysJobLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 写入 {@code sys_job_log} 调度日志。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JobExecutionLogger {

    private final SysJobLogService logService;

    /**
     * 记录一次执行结果。
     */
    public void write(JobTaskSnapshot snapshot, LocalDateTime start, Exception error) {
        try {
            SysJobLog row = new SysJobLog();
            row.setJobId(snapshot.getJobId());
            row.setJobName(snapshot.getJobName());
            row.setJobGroup(snapshot.getJobGroup());
            row.setInvokeTarget(snapshot.getInvokeTarget());
            row.setCreateTime(LocalDateTime.now());
            if (error == null) {
                row.setStatus("0");
                row.setJobMessage("执行成功");
            } else {
                row.setStatus("1");
                row.setJobMessage("执行失败");
                row.setExceptionInfo(StrUtil.sub(ExceptionUtil.getMessage(error), 0, 2000));
            }
            logService.addLog(row);
        } catch (Exception e) {
            log.error("写入调度日志失败 jobId={}", snapshot.getJobId(), e);
        }
    }
}
