package io.github.genkidoudou.web.knowledge.constants;

/**
 * 文档分段模式（对应 {@code kb_document.segment_mode} / {@code kb_knowledge_base.segment_mode}）。
 */
public final class KbSegmentMode {

    /** 自动 Token 分块（{@link org.springframework.ai.transformer.splitter.TokenTextSplitter}）。 */
    public static final String AUTO = "AUTO";

    /** 自定义分隔符 + Token 上限/重叠。 */
    public static final String CUSTOM = "CUSTOM";

    private KbSegmentMode() {
    }
}
