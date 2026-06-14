package io.github.genkidoudou.web.ai.prompt.constants;

import cn.hutool.core.util.StrUtil;

import java.util.Locale;
import java.util.Set;

/**
 * 提示词变量类型，对应 {@code ai_prompt_variable.var_type}。
 */
public final class AiPromptVarType {

    public static final String STRING = "string";

    public static final String NUMBER = "number";

    public static final String ARRAY = "array";

    public static final String OBJECT = "object";

    private static final Set<String> SUPPORTED = Set.of(STRING, NUMBER, ARRAY, OBJECT);

    private AiPromptVarType() {
    }

    /**
     * 是否为支持的变量类型。
     *
     * @param varType 类型
     * @return 是否支持
     */
    public static boolean isSupported(String varType) {
        if (StrUtil.isBlank(varType)) {
            return false;
        }
        return SUPPORTED.contains(varType.trim().toLowerCase(Locale.ROOT));
    }

    /**
     * 规范化变量类型（小写）。
     *
     * @param varType 入参
     * @return 小写类型；无效时返回 string
     */
    public static String normalize(String varType) {
        if (StrUtil.isBlank(varType)) {
            return STRING;
        }
        String normalized = varType.trim().toLowerCase(Locale.ROOT);
        return SUPPORTED.contains(normalized) ? normalized : STRING;
    }
}
