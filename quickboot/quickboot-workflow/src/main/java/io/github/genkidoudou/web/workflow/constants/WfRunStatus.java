package io.github.genkidoudou.web.workflow.constants;

/**
 * 工作流运行实例状态。
 */
public final class WfRunStatus {

    /** 已入队，等待执行。 */
    public static final String QUEUED = "QUEUED";

    /** 执行中。 */
    public static final String RUNNING = "RUNNING";

    /** 执行成功。 */
    public static final String SUCCESS = "SUCCESS";

    /** 执行失败。 */
    public static final String FAILED = "FAILED";

    /** 已取消。 */
    public static final String CANCELLED = "CANCELLED";

    private WfRunStatus() {
    }
}
