package io.github.genkidoudou.web.monitor.job.quartz;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Properties;

/**
 * Quartz JDBC 集群调度配置。
 */
@Configuration
public class ScheduleConfig {

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(
        DataSource dataSource,
        ApplicationContext applicationContext,
        PlatformTransactionManager transactionManager
    ) throws Exception {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setTransactionManager(transactionManager);
        Properties prop = new Properties();
        prop.put("org.quartz.scheduler.instanceName", "QuickScheduler");
        prop.put("org.quartz.scheduler.instanceId", "AUTO");
        prop.put("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
        prop.put("org.quartz.threadPool.threadCount", "20");
        prop.put("org.quartz.threadPool.threadPriority", "5");
        prop.put("org.quartz.jobStore.class", "org.springframework.scheduling.quartz.LocalDataSourceJobStore");
        prop.put("org.quartz.jobStore.maxMisfiresToHandleAtATime", "10");
        prop.put("org.quartz.jobStore.misfireThreshold", "12000");
        prop.put("org.quartz.jobStore.tablePrefix", "QRTZ_");
        applyJobStoreDialect(dataSource, prop);
        factory.setQuartzProperties(prop);
        factory.setSchedulerName("QuickScheduler");
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
     * Quartz 2.3.x 已移除 {@code H2Delegate}；开发库 H2 使用 {@code MODE=MySQL} + 标准委托即可。
     * H2 关闭集群；MySQL 等生产库开启集群。
     */
    private static void applyJobStoreDialect(DataSource dataSource, Properties prop) throws Exception {
        prop.put("org.quartz.jobStore.driverDelegateClass", JDBC_DELEGATE_STD);
        try (Connection conn = dataSource.getConnection()) {
            String product = conn.getMetaData().getDatabaseProductName();
            boolean h2 = product != null && product.toLowerCase().contains("h2");
            if (h2) {
                prop.put("org.quartz.jobStore.isClustered", "false");
            } else {
                prop.put("org.quartz.jobStore.isClustered", "true");
                prop.put("org.quartz.jobStore.clusterCheckinInterval", "15000");
            }
        }
    }
}
