package io.github.genkidoudou.web.knowledge.constants;

/**
 * 知识库模块通用常量。
 */
public final class KnowledgeConstants {

    /** 知识库文档上传 classify，须在 {@code qc.file.classifies} 中配置。 */
    public static final String FILE_CLASSIFY = "knowledge";

    /** 独立文档库文件 classify，须在 {@code qc.file.classifies} 中配置。 */
    public static final String LIBRARY_FILE_CLASSIFY = "knowledge-library";

    /** 知识库状态：正常可用。 */
    public static final int KB_STATUS_NORMAL = 0;

    /** 知识库状态：停用，禁止上传新文档。 */
    public static final int KB_STATUS_DISABLED = 1;

    /** 逻辑删除：未删除。 */
    public static final int NOT_DELETED = 0;

    /** 逻辑删除：已删除。 */
    public static final int DELETED = 1;

    /** 手动录入正文最大字节数（512KB）。 */
    public static final int MANUAL_CONTENT_MAX_BYTES = 512 * 1024;

    private KnowledgeConstants() {
    }
}
