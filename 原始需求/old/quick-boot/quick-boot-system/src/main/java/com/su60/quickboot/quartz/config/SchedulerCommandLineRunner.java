package com.su60.quickboot.quartz.config;

import com.su60.quickboot.quartz.dos.SysJobDo;
import com.su60.quickboot.quartz.service.ISysJobService;
import com.su60.quickboot.quartz.utils.ScheduleUtils;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 定时任务 启动
 *
 * @author luyanan
 * @since 2024/11/11
 */
@Slf4j
@Configuration
public class SchedulerCommandLineRunner implements CommandLineRunner {

	private final Scheduler scheduler;
	private final ISysJobService jobService;

	public SchedulerCommandLineRunner(Scheduler scheduler, ISysJobService jobService) {
		this.scheduler = scheduler;
		this.jobService = jobService;
	}

	@Override
	public void run(String... args) throws Exception {

		scheduler.clear();

		List<SysJobDo> sysJobDos = jobService.listAll();
		for (SysJobDo sysJobDo : sysJobDos) {
			ScheduleUtils.createScheduleJob(scheduler, sysJobDo);
		}
		log.info("定时任务加载结束");
	}
}
