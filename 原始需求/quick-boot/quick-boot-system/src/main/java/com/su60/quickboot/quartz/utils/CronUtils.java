package com.su60.quickboot.quartz.utils;

import org.quartz.CronExpression;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * cron表达式工具类
 *
 * @author luyanan
 * @since 2024/11/12
 */
public class CronUtils {
	/**
	 * 返回一个布尔值代表一个给定的Cron表达式的有效性
	 *
	 * @param cronExpression Cron表达式
	 * @return boolean 表达式是否有效
	 */
	public static boolean isValid(String cronExpression) {
		return CronExpression.isValidExpression(cronExpression);
	}

	/**
	 * 返回一个字符串值,表示该消息无效Cron表达式给出有效性
	 *
	 * @param cronExpression Cron表达式
	 * @return String 无效时返回表达式错误描述,如果有效返回null
	 */
	public static String getInvalidMessage(String cronExpression) {
		try {
			new CronExpression(cronExpression);
			return null;
		} catch (ParseException pe) {
			return pe.getMessage();
		}
	}

	/**
	 * 返回下一个执行时间根据给定的Cron表达式
	 *
	 * @param cronExpression Cron表达式
	 * @return Date 下次Cron表达式执行时间
	 */
	public static Date getNextExecution(String cronExpression) {
		try {
			CronExpression cron = new CronExpression(cronExpression);
			return cron.getNextValidTimeAfter(new Date(System.currentTimeMillis()));
		} catch (ParseException e) {
			throw new IllegalArgumentException(e.getMessage());
		}
	}


	/**
	 * 获取下几次的执行时间
	 *
	 * @param cronExpression Cron表达式
	 * @param count          次数
	 * @return 下几次Cron表达式执行时间
	 * @since 2024/11/14
	 */
	public static List<Date> getNextExecutions(String cronExpression, Integer count) {
		List<Date> dates = new ArrayList<>();
		Date date = new Date();
		for (Integer i = 0; i < count; i++) {
			try {
				CronExpression cron = new CronExpression(cronExpression);
				Date nextValidTimeAfter = cron.getNextValidTimeAfter(date);
				date = nextValidTimeAfter;
				dates.add(date);
			} catch (ParseException e) {
				throw new IllegalArgumentException(e.getMessage());
			}
		}

		return dates;
	}
}
