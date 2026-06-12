package io.github.genkidoudou.web.knowledge.constants;

/**
 * 自定义分段分隔符（对应 {@code kb_document.chunk_delimiter}，仅 {@link KbSegmentMode#CUSTOM} 生效）。
 */
public final class KbChunkDelimiter {

    /** 按单个换行符分段。 */
    public static final String SINGLE_NEWLINE = "SINGLE_NEWLINE";

    /** 按连续空行（双换行及以上）分段。 */
    public static final String DOUBLE_NEWLINE = "DOUBLE_NEWLINE";

    private KbChunkDelimiter() {
    }
}
