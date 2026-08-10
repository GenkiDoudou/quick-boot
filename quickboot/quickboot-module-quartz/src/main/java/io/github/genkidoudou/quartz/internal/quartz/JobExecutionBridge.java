package io.github.genkidoudou.quartz.internal.quartz;

import io.github.genkidoudou.quartz.internal.mapper.SysJobMapper;
import io.github.genkidoudou.quartz.internal.service.SysJobLogService;
import jakarta.annotation.PostConstruct;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

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
    private static JdbcTemplate jdbcTemplate;
    private static TransactionTemplate transactionTemplate;

    private final ApplicationContext ctx;
    private final SysJobLogService logService;
    private final SysJobMapper sysJobMapper;
    private final JobTaskInvoker taskInvoker;
    private final JobExecutionLogger executionLogger;
    private final JdbcTemplate jdbc;
    private final TransactionTemplate transactionTemplateBean;

    public JobExecutionBridge(
        ApplicationContext ctx,
        SysJobLogService logService,
        SysJobMapper sysJobMapper,
        JobTaskInvoker taskInvoker,
        JobExecutionLogger executionLogger,
        JdbcTemplate jdbc,
        TransactionTemplate transactionTemplateBean
    ) {
        this.ctx = ctx;
        this.logService = logService;
        this.sysJobMapper = sysJobMapper;
        this.taskInvoker = taskInvoker;
        this.executionLogger = executionLogger;
        this.jdbc = jdbc;
        this.transactionTemplateBean = transactionTemplateBean;
    }

    @PostConstruct
    void init() {
        applicationContext = ctx;
        jobLogService = logService;
        jobMapper = sysJobMapper;
        jobTaskInvoker = taskInvoker;
        jobExecutionLogger = executionLogger;
        jdbcTemplate = jdbc;
        transactionTemplate = transactionTemplateBean;
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

    public static JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public static TransactionTemplate getTransactionTemplate() {
        return transactionTemplate;
    }
}
