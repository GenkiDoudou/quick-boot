package io.github.genkidoudou.system.api;

/**
 * 操作日志请求/错误分桶（跨模块只读视图）。
 *
 * @param bucket       桶标签
 * @param requestCount 请求数
 * @param errorCount   错误数
 */
public record OperLogBucketView(String bucket, long requestCount, long errorCount) {
}
