package io.github.genkidoudou.quartz.internal.quartz;

import org.quartz.DisallowConcurrentExecution;

/**
 * 禁止并发执行的 Quartz Job。
 */
@DisallowConcurrentExecution
public class QuartzDisallowConcurrentExecution extends QuartzJobExecution {
}
