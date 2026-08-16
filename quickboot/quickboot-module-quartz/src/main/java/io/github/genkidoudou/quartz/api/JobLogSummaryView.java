package io.github.genkidoudou.quartz.api;

/**
 * 定时任务日志时间窗汇总（跨模块只读）。
 *
 * @param successCount 成功次数
 * @param failCount    失败次数
 * @param failRatePct  失败率百分比（0–100），无样本时为 {@code null}
 */
public record JobLogSummaryView(long successCount, long failCount, Double failRatePct) {
}
