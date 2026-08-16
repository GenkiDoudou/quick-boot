package io.github.genkidoudou.quartz.api;

import java.time.LocalDateTime;

/**
 * 最近失败任务摘要（跨模块只读）。
 *
 * @param jobLogId      日志 ID
 * @param jobName       任务名
 * @param jobGroup      分组
 * @param invokeTarget  调用目标
 * @param jobMessage    消息
 * @param exceptionInfo 异常摘要
 * @param createTime    时间
 */
public record JobLogFailView(
  Long jobLogId,
  String jobName,
  String jobGroup,
  String invokeTarget,
  String jobMessage,
  String exceptionInfo,
  LocalDateTime createTime
) {
}
