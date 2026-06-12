package io.github.genkidoudou.web.knowledge.constants;

/**
 * 知识库异步入库任务状态枚举值（对应 {@code kb_ingest_task.status}）。
 */
public final class KbTaskStatus {

    /** 已入队，等待线程池执行。 */
    public static final String QUEUED = "QUEUED";

    /** 正在执行入库流水线。 */
    public static final String RUNNING = "RUNNING";

    /** 入库成功完成。 */
    public static final String SUCCESS = "SUCCESS";

    /** 入库失败。 */
    public static final String FAILED = "FAILED";

    private KbTaskStatus() {
    }
}
