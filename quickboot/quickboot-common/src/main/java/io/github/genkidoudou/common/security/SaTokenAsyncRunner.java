package io.github.genkidoudou.common.security;

import cn.dev33.satoken.context.mock.SaTokenContextMockUtil;
import cn.dev33.satoken.stp.StpUtil;

/**
 * 在异步线程中恢复 Sa-Token 登录上下文，避免调用 StpUtil 时抛出「上下文尚未初始化」。
 * <p>
 * 典型场景：SSE 聊天在 {@code CompletableFuture.runAsync} 中触发工作流 Tool，内部会访问登录态。
 * </p>
 */
public final class SaTokenAsyncRunner {

    private SaTokenAsyncRunner() {
    }

    /**
     * 从当前 Web 请求线程捕获 token；非 Web 或未登录时返回 {@code null}。
     *
     * @return Sa-Token 值
     */
    public static String captureTokenValue() {
        try {
            if (!StpUtil.isLogin()) {
                return null;
            }
            return StpUtil.getTokenValue();
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * 在 Mock 上下文中执行任务；token 为空时直接执行（无登录态）。
     *
     * @param tokenValue 捕获的 token
     * @param task       异步任务
     */
    public static void run(String tokenValue, Runnable task) {
        if (task == null) {
            return;
        }
        if (tokenValue == null || tokenValue.isBlank()) {
            task.run();
            return;
        }
        SaTokenContextMockUtil.setMockContext(() -> {
            StpUtil.setTokenValueToStorage(tokenValue);
            task.run();
        });
    }
}
