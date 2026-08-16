package io.github.genkidoudou.system.api;

/**
 * 操作日志时间窗聚合（跨模块只读视图）。
 *
 * @param requestCount 请求总数
 * @param errorCount   异常次数（{@code status <> 0}）
 */
public record OperLogSummaryView(long requestCount, long errorCount) {
}
