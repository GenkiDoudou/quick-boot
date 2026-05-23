package com.su60.quickboot.quartz.utils;

/**
 * 定时任务抽象类
 *
 * @author luyanan
 * @since 2024/11/11
 */
public interface ITask {

	/**
	 * 定时任务执行
	 *
	 * @param params 参数
	 * @return
	 * @since 2024/11/11
	 */
	void execute(String params);
}
