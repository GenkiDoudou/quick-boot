package io.github.genkidoudou.web.monitor.job.quartz;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.genkidoudou.web.monitor.job.domain.SysJob;
import io.github.genkidoudou.web.monitor.job.mapper.SysJobMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 启动时加载已启用的定时任务到 Scheduler。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JobScheduleRunner implements ApplicationRunner {

    private final Scheduler scheduler;
    private final SysJobMapper jobMapper;

    @Override
    public void run(ApplicationArguments args) {
        List<SysJob> jobs = jobMapper.selectList(
            Wrappers.<SysJob>lambdaQuery().eq(SysJob::getStatus, "0")
        );
        for (SysJob job : jobs) {
            try {
                ScheduleUtils.createScheduleJob(scheduler, JobTaskSnapshot.from(job));
            } catch (SchedulerException e) {
                log.error("加载定时任务失败 jobId={}", job.getJobId(), e);
            }
        }
        log.info("定时任务加载完成，共 {} 条启用任务", jobs.size());
    }
}
