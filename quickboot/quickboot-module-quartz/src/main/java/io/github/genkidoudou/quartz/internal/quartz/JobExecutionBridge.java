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

    /** 获取 Spring 容器（Quartz Job 静态入口使用）。 */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /** 获取调度日志服务。 */
    public static SysJobLogService getJobLogService() {
        return jobLogService;
    }

    /** 获取任务 Mapper。 */
    public static SysJobMapper getJobMapper() {
        return jobMapper;
    }

    /** 获取 {@link ITask} 调用器。 */
    public static JobTaskInvoker getJobTaskInvoker() {
        return jobTaskInvoker;
    }

    /** 获取执行日志写入器。 */
    public static JobExecutionLogger getJobExecutionLogger() {
        return jobExecutionLogger;
    }

    /** 获取 JDBC 模板（JobStore 脏数据清理等）。 */
    public static JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    /** 获取编程式事务模板。 */
    public static TransactionTemplate getTransactionTemplate() {
        return transactionTemplate;
    }
}
