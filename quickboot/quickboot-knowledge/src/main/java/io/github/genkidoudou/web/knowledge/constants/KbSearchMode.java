package io.github.genkidoudou.web.knowledge.constants;

/**
 * 知识库检索模式。
 */
public final class KbSearchMode {

    /** 纯向量语义检索。 */
    public static final String VECTOR = "VECTOR";

    /** 向量 + 关键词混合检索（默认）。 */
    public static final String HYBRID = "HYBRID";

    private KbSearchMode() {
    }
}
