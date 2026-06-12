package io.github.genkidoudou.web.workflow.constants;

/**
 * 工作流运行触发类型。
 */
public final class WfTriggerType {

    /** 画布 Debug 调试运行。 */
    public static final String DEBUG = "DEBUG";

    /** 异步后台运行。 */
    public static final String ASYNC = "ASYNC";

    /** 对外 API 调用（P0 预留）。 */
    public static final String API = "API";

    private WfTriggerType() {
    }
}
