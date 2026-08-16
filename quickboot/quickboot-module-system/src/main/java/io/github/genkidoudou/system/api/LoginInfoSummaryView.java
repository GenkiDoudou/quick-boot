package io.github.genkidoudou.system.api;

/**
 * 登录日志时间窗聚合结果（跨模块只读视图）。
 *
 * @param loginUsers   成功登录去重用户数
 * @param successCount 成功次数
 * @param failCount    失败次数
 */
public record LoginInfoSummaryView(long loginUsers, long successCount, long failCount) {
}
