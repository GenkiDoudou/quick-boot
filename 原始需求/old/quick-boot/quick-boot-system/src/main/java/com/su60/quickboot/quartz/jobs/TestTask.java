package com.su60.quickboot.quartz.jobs;

import cn.hutool.core.util.RandomUtil;
import com.su60.quickboot.quartz.utils.ITask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 测试定时任务
 *
 * @author luyanan
 * @since 2024/11/14
 */
@Slf4j
@Component("testTask")
public class TestTask implements ITask {


	@Override
	public void execute(String params) {

		try {
			Thread.sleep(RandomUtil.randomInt(1000));
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		log.info("执行任务:{},参数为:{}", "testTask", params);
		int a = 1 / RandomUtil.randomInt(2);

	}
}
