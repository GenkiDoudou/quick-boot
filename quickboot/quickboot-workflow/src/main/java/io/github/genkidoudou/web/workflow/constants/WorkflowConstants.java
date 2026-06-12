package io.github.genkidoudou.web.workflow.constants;

/**
 * 工作流模块通用常量。
 */
public final class WorkflowConstants {

    /** 未逻辑删除。 */
    public static final int NOT_DELETED = 0;

    /** 已逻辑删除。 */
    public static final int DELETED = 1;

    /** 当前编辑草稿版本标记。 */
    public static final int DRAFT_VERSION = 1;

    /** 非草稿（已发布）版本标记。 */
    public static final int PUBLISHED_VERSION = 0;

    /** DSL 当前版本号。 */
    public static final int GRAPH_VERSION = 1;

    /** if-else 真分支 handle。 */
    public static final String HANDLE_TRUE = "true";

    /** if-else 假分支 handle。 */
    public static final String HANDLE_FALSE = "false";

    /** 内置默认 RAG 模板编码。 */
    public static final String TEMPLATE_DEFAULT_RAG = "default-rag";

    private WorkflowConstants() {
    }
}
