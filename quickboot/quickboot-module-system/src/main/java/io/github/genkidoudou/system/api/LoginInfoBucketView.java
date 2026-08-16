package io.github.genkidoudou.system.api;

/**
 * 登录趋势分桶（跨模块只读视图）。
 *
 * @param bucket       桶标签（小时：{@code yyyy-MM-dd HH:00:00}；日：{@code yyyy-MM-dd}）
 * @param successCount 成功次数
 * @param failCount    失败次数
 */
public record LoginInfoBucketView(String bucket, long successCount, long failCount) {
}
