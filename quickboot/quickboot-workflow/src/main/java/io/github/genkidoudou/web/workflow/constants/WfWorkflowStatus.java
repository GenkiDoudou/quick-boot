package io.github.genkidoudou.web.workflow.constants;

/**
 * 工作流定义状态。
 */
public final class WfWorkflowStatus {

    /** 草稿：尚未发布或仅有草稿版本。 */
    public static final String DRAFT = "DRAFT";

    /** 已发布：存在可用发布版本。 */
    public static final String PUBLISHED = "PUBLISHED";

    /** 已停用：禁止运行。 */
    public static final String DISABLED = "DISABLED";

    private WfWorkflowStatus() {
    }
}
