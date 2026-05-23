package io.github.genkidoudou.web.monitor.job.quartz;

import io.github.genkidoudou.web.monitor.job.mapper.SysJobMapper;
import io.github.genkidoudou.web.monitor.job.service.SysJobLogService;
import jakarta.annotation.PostConstruct;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * 供 Quartz Job 静态入口获取 Spring Bean（Job 实例非 Spring 管理）。
 */
@Component
public class JobExecutionBridge {

    private static ApplicationContext applicationContext;
    private static SysJobLogService jobLogService;
    private static SysJobMapper jobMapper;
    private static JobTaskInvoker jobTaskInvoker;
    private static JobExecutionLogger jobExecutionLogger;

    private final ApplicationContext ctx;
    private final SysJobLogService logService;
    private final SysJobMapper sysJobMapper;
    private final JobTaskInvoker taskInvoker;
    private final JobExecutionLogger executionLogger;

    public JobExecutionBridge(
        ApplicationContext ctx,
        SysJobLogService logService,
        SysJobMapper sysJobMapper,
        JobTaskInvoker taskInvoker,
        JobExecutionLogger executionLogger
    ) {
        this.ctx = ctx;
        this.logService = logService;
        this.sysJobMapper = sysJobMapper;
        this.taskInvoker = taskInvoker;
        this.executionLogger = executionLogger;
    }

    @PostConstruct
    void init() {
        applicationContext = ctx;
        jobLogService = logService;
        jobMapper = sysJobMapper;
        jobTaskInvoker = taskInvoker;
        jobExecutionLogger = executionLogger;
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static SysJobLogService getJobLogService() {
        return jobLogService;
    }

    public static SysJobMapper getJobMapper() {
        return jobMapper;
    }

    public static JobTaskInvoker getJobTaskInvoker() {
        return jobTaskInvoker;
    }

    public static JobExecutionLogger getJobExecutionLogger() {
        return jobExecutionLogger;
    }
}
