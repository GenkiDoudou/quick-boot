package io.github.genkidoudou.web.system.user.datascope;

import cn.dev33.satoken.stp.StpUtil;

/**
 * 在 Sa-Token Session 中读写 {@link DataScopeSession}（登录后写入，登出随会话销毁）。
 */
public final class DataScopeSessionStore {

    /** Session 键名（与业务数据隔离）。 */
    public static final String SESSION_KEY = "qbDataScope";

    private DataScopeSessionStore() {
    }

    /**
     * 写入当前登录会话；须在 {@link StpUtil#login(Object)} 之后调用。
     *
     * @param session 预计算快照
     */
    public static void save(DataScopeSession session) {
        if (session == null) {
            return;
        }
        StpUtil.getSession().set(SESSION_KEY, session);
    }

    /**
     * @return 当前会话快照；未登录或未写入时返回 {@code null}
     */
    public static DataScopeSession get() {
        try {
            if (!StpUtil.isLogin()) {
                return null;
            }
            return (DataScopeSession) StpUtil.getSession().get(SESSION_KEY);
        } catch (Exception ignored) {
            // 非 Web 线程（如 SSE 异步聊天）无 Sa-Token 上下文
            return null;
        }
    }
}
