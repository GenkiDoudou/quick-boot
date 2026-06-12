package io.github.genkidoudou.web.ai.constants;

/**
 * 模型连接探测结果状态，对应 {@code ai_model.last_test_status}。
 */
public final class AiTestStatus {

    /** 最近一次探测成功。 */
    public static final String SUCCESS = "SUCCESS";

    /** 最近一次探测失败。 */
    public static final String FAILED = "FAILED";

    /** 尚未执行过探测。 */
    public static final String UNTESTED = "UNTESTED";

    private AiTestStatus() {
    }
}
