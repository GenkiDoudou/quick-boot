package io.github.genkidoudou.web.ai.constants;

import cn.hutool.core.util.StrUtil;

import java.util.Locale;

/**
 * AI 模型厂商/协议，对应 {@code ai_model.provider}。
 */
public final class AiProvider {

    /** DeepSeek（OpenAI 兼容）。 */
    public static final String DEEPSEEK = "DEEPSEEK";

    /** OpenAI 官方 API。 */
    public static final String OPENAI = "OPENAI";

    /** 阿里云通义千问（DashScope 兼容模式）。 */
    public static final String TONGYI = "TONGYI";

    /** 本地 Ollama 服务。 */
    public static final String OLLAMA = "OLLAMA";

    /** 兼容旧数据：OpenAI 兼容协议。 */
    public static final String OPENAI_COMPAT = "OPENAI_COMPAT";

    private AiProvider() {
    }

    /**
     * 是否走 OpenAI 兼容客户端（DeepSeek / OpenAI / 通义 / 旧 OPENAI_COMPAT）。
     *
     * @param provider 厂商
     * @return 是否 OpenAI 兼容
     */
    public static boolean isOpenAiCompatible(String provider) {
        if (StrUtil.isBlank(provider)) {
            return false;
        }
        String p = provider.trim().toUpperCase(Locale.ROOT);
        return DEEPSEEK.equals(p) || OPENAI.equals(p) || TONGYI.equals(p) || OPENAI_COMPAT.equals(p);
    }

    /**
     * Ollama 可不配置 API Key。
     *
     * @param provider 厂商
     * @return 是否 Ollama
     */
    public static boolean isOllama(String provider) {
        return OLLAMA.equals(normalize(provider));
    }

    /**
     * 是否为当前支持的厂商。
     *
     * @param provider 厂商
     * @return 是否支持
     */
    public static boolean isSupported(String provider) {
        if (StrUtil.isBlank(provider)) {
            return false;
        }
        String p = normalize(provider);
        return DEEPSEEK.equals(p) || OPENAI.equals(p) || TONGYI.equals(p) || OLLAMA.equals(p) || OPENAI_COMPAT.equals(p);
    }

    private static String normalize(String provider) {
        return provider == null ? "" : provider.trim().toUpperCase(Locale.ROOT);
    }
}
