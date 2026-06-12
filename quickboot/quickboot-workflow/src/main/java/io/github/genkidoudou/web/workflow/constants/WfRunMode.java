package io.github.genkidoudou.web.workflow.constants;

/**
 * 工作流运行模式。
 */
public final class WfRunMode {

    /** 同步阻塞直至完成或超时。 */
    public static final String SYNC = "SYNC";

    /** 异步后台执行。 */
    public static final String ASYNC = "ASYNC";

    private WfRunMode() {
    }
}
