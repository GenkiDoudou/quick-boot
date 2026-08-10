package io.github.genkidoudou.quartz.internal.quartz;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Properties;

/**
 * Quartz JDBC 调度配置：MySQL 开启集群；H2（本地 dev）关闭集群以免锁/校验异常。
 */
@Configuration
@EnableScheduling
public class ScheduleConfig {

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(
        DataSource dataSource,
        ApplicationContext applicationContext,
        PlatformTransactionManager transactionManager,
        JdbcTemplate jdbcTemplate
    ) throws Exception {
        CleaningSchedulerFactoryBean factory = new CleaningSchedulerFactoryBean(jdbcTemplate);
        factory.setDataSource(dataSource);
        factory.setTransactionManager(transactionManager);
        Properties prop = new Properties();
        prop.put("org.quartz.scheduler.instanceName", ScheduleUtils.SCHEDULER_NAME);
        prop.put("org.quartz.scheduler.instanceId", "AUTO");
        prop.put("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
        prop.put("org.quartz.threadPool.threadCount", "20");
        prop.put("org.quartz.threadPool.threadPriority", "5");
        prop.put("org.quartz.jobStore.class", "org.springframework.scheduling.quartz.LocalDataSourceJobStore");
        prop.put("org.quartz.jobStore.maxMisfiresToHandleAtATime", "10");
        prop.put("org.quartz.jobStore.misfireThreshold", "12000");
        prop.put("org.quartz.jobStore.tablePrefix", "QRTZ_");
        applyJobStoreDialect(prop, dataSource);
        factory.setQuartzProperties(prop);
        factory.setSchedulerName(ScheduleUtils.SCHEDULER_NAME);
        factory.setStartupDelay(1);
        factory.setApplicationContextSchedulerContextKey("applicationContext");
        factory.setApplicationContext(applicationContext);
        factory.setOverwriteExistingJobs(true);
        factory.setAutoStartup(true);
        return factory;
    }

    @Bean
    public Scheduler scheduler(SchedulerFactoryBean schedulerFactoryBean) throws SchedulerException {
        return schedulerFactoryBean.getScheduler();
    }

    private static final String JDBC_DELEGATE_STD = "org.quartz.impl.jdbcjobstore.StdJDBCDelegate";

    /**
     * StdJDBCDelegate；H2 关闭集群，MySQL 等开启集群。
     */
    private static void applyJobStoreDialect(Properties prop, DataSource dataSource) {
        prop.put("org.quartz.jobStore.driverDelegateClass", JDBC_DELEGATE_STD);
        boolean clustered = !isH2(dataSource);
        prop.put("org.quartz.jobStore.isClustered", Boolean.toString(clustered));
        if (clustered) {
            prop.put("org.quartz.jobStore.clusterCheckinInterval", "15000");
        }
    }

    private static boolean isH2(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            String url = connection.getMetaData().getURL();
            return url != null && url.toLowerCase().contains("jdbc:h2:");
        } catch (Exception ignored) {
            return false;
        }
    }
}
