package io.github.genkidoudou.web.knowledge.constants;

/**
 * 知识库文档入库状态枚举值（对应 {@code kb_document.doc_status}）。
 */
public final class KbDocStatus {

    /** 已登记，等待异步入库。 */
    public static final String PENDING = "PENDING";

    /** 正在解析、分块与向量化。 */
    public static final String PARSING = "PARSING";

    /** 已成功写入向量库。 */
    public static final String INDEXED = "INDEXED";

    /** 入库失败。 */
    public static final String FAILED = "FAILED";

    private KbDocStatus() {
    }
}
