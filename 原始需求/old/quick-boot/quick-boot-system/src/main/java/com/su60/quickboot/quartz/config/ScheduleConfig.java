package com.su60.quickboot.quartz.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * 定时任务配置（单机部署建议删除此类和qrtz数据库表，默认走内存会最高效）
 *
 * @author luyanan
 * @since 2024/11/11
 */
@Configuration
public class ScheduleConfig {


	/**
	 * schedulerFactoryBean
	 *
	 * @param dataSource 数据源
	 * @return org.springframework.scheduling.quartz.SchedulerFactoryBean
	 * @since 2024/11/11
	 */
	@Bean
	public SchedulerFactoryBean schedulerFactoryBean(DataSource dataSource) {

		SchedulerFactoryBean factoryBean = new SchedulerFactoryBean();
		factoryBean.setDataSource(dataSource);
		// quartz参数
		Properties prop = new Properties();
		prop.put("org.quartz.scheduler.instanceName", "QuickiScheduler");
		prop.put("org.quartz.scheduler.instanceId", "AUTO");
		// 线程池配置
		prop.put("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
		prop.put("org.quartz.threadPool.threadCount", "20");
		prop.put("org.quartz.threadPool.threadPriority", "5");
		// JobStore配置
		prop.put("org.quartz.jobStore.class", "org.springframework.scheduling.quartz.LocalDataSourceJobStore");
		// 集群配置
		prop.put("org.quartz.jobStore.isClustered", "true");
		prop.put("org.quartz.jobStore.clusterCheckinInterval", "15000");
		prop.put("org.quartz.jobStore.maxMisfiresToHandleAtATime", "10");
		prop.put("org.quartz.jobStore.txIsolationLevelSerializable", "true");

		// sqlserver 启用
		// prop.put("org.quartz.jobStore.selectWithLockSQL", "SELECT * FROM {0}LOCKS UPDLOCK WHERE LOCK_NAME = ?");
		prop.put("org.quartz.jobStore.misfireThreshold", "12000");
		prop.put("org.quartz.jobStore.tablePrefix", "QRTZ_");
		factoryBean.setQuartzProperties(prop);

		factoryBean.setSchedulerName("QuickScheduler");
		// 延时启动
		factoryBean.setStartupDelay(1);
		factoryBean.setApplicationContextSchedulerContextKey("applicationContextKey");
		// 可选，QuartzScheduler
		// 启动时更新己存在的Job，这样就不用每次修改targetObject后删除qrtz_job_details表对应记录了
		factoryBean.setOverwriteExistingJobs(true);
		// 设置自动启动，默认为true
		factoryBean.setAutoStartup(true);

		return factoryBean;
	}
}
