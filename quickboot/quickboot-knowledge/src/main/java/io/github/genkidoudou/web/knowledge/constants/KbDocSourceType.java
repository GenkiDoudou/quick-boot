package io.github.genkidoudou.web.knowledge.constants;

/**
 * 知识库文档来源类型（对应 {@code kb_document.source_type}）。
 */
public final class KbDocSourceType {

    /** 直接上传文件。 */
    public static final String FILE = "FILE";

    /** 手动录入标题与正文。 */
    public static final String MANUAL = "MANUAL";

    /** 网页 URL 抓取。 */
    public static final String WEB = "WEB";

    /** 从独立文档库选取。 */
    public static final String LIBRARY = "LIBRARY";

    private KbDocSourceType() {
    }
}
