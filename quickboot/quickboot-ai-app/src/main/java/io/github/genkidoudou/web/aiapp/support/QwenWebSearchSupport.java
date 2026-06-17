package io.github.genkidoudou.web.aiapp.support;

import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.web.ai.constants.AiProvider;
import io.github.genkidoudou.web.ai.domain.AiModel;
import io.github.genkidoudou.web.ai.mapper.AiModelMapper;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 千问 / DashScope 联网搜索支持：检测模型厂商并在请求中注入 {@code enable_search}。
 */
@Component
public class QwenWebSearchSupport {

    private final AiModelMapper modelMapper;

    public QwenWebSearchSupport(AiModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    /**
     * 判断 chatModelId 是否对应千问/DashScope 模型。
     *
     * @param chatModelId 模型 ID
     * @return 是否为千问系列
     */
    public boolean isQwenModel(Long chatModelId) {
        if (chatModelId == null) {
            return false;
        }
        AiModel model = modelMapper.selectById(chatModelId);
        if (model == null) {
            return false;
        }
        if (AiProvider.TONGYI.equals(normalize(model.getProvider()))) {
            return true;
        }
        String baseUrl = model.getBaseUrl();
        return StrUtil.isNotBlank(baseUrl) && baseUrl.toLowerCase(Locale.ROOT).contains("dashscope");
    }

    /**
     * 构建 ChatOptions；千问模型且 {@code webSearch=true} 时注入联网参数。
     *
     * @param chatModelId 模型 ID
     * @param webSearch   是否开启联网
     * @return ChatOptions，无需特殊选项时返回 null
     */
    public ChatOptions buildChatOptions(Long chatModelId, Boolean webSearch) {
        if (!Boolean.TRUE.equals(webSearch) || !isQwenModel(chatModelId)) {
            return null;
        }
        Map<String, Object> toolContext = new HashMap<>();
        toolContext.put("enable_search", true);
        return OpenAiChatOptions.builder()
            .toolContext(toolContext)
            .build();
    }

    private static String normalize(String provider) {
        return provider == null ? "" : provider.trim().toUpperCase(Locale.ROOT);
    }
}
