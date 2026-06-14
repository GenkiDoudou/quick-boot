package io.github.genkidoudou.web.ai.prompt.service.impl;

import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.web.ai.prompt.constants.AiPromptConstants;
import io.github.genkidoudou.web.ai.prompt.dto.AiPromptOptimizeBo;
import io.github.genkidoudou.web.ai.prompt.dto.AiPromptOptimizeResultVo;
import io.github.genkidoudou.web.ai.prompt.service.AiPromptOptimizeService;
import io.github.genkidoudou.web.ai.prompt.support.AiPromptChatSupport;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 提示词内容 AI 优化：输入正文，返回优化后正文（同步 60s）。
 */
@Service
public class AiPromptOptimizeServiceImpl implements AiPromptOptimizeService {

    private static final String META_PROMPT = """
        你是 Prompt 工程师。请优化以下提示词，使其更清晰、具体、可执行，保留原意。
        只输出优化后的提示词正文，不要附加解释或 markdown 代码块。
        
        原始提示词：
        %s
        """;

    private final AiPromptChatSupport chatSupport;

    public AiPromptOptimizeServiceImpl(AiPromptChatSupport chatSupport) {
        this.chatSupport = chatSupport;
    }

    @Override
    public AiPromptOptimizeResultVo optimize(AiPromptOptimizeBo req) {
        if (StrUtil.isBlank(req.getContent())) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "提示词内容不能为空");
        }

        AiPromptChatSupport.ResolvedChatModel resolved = chatSupport.requireChatModel(req.getModelId());
        String userPrompt = META_PROMPT.formatted(req.getContent().trim());

        AiPromptOptimizeResultVo vo = new AiPromptOptimizeResultVo();
        try {
            String optimized = invokeChat(resolved, userPrompt);
            if (StrUtil.isBlank(optimized)) {
                vo.setSuccess(false);
                vo.setErrorMsg("模型返回为空");
                return vo;
            }
            vo.setSuccess(true);
            vo.setOptimizedContent(stripCodeFence(optimized.trim()));
            return vo;
        } catch (TimeoutException ex) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "AI 优化超时（60s），请稍后重试");
        } catch (Exception ex) {
            vo.setSuccess(false);
            vo.setErrorMsg(ex.getMessage() == null ? "调用失败" : ex.getMessage());
            return vo;
        }
    }

    private String invokeChat(AiPromptChatSupport.ResolvedChatModel resolved, String userPrompt) throws Exception {
        ChatClient client = ChatClient.builder(resolved.chatModel()).build();
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() ->
            client.prompt().user(userPrompt).call().content()
        );
        String content = future.get(AiPromptConstants.OPTIMIZE_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        return content == null ? "" : content;
    }

    private String stripCodeFence(String text) {
        if (text.startsWith("```") && text.endsWith("```")) {
            int firstNewline = text.indexOf('\n');
            if (firstNewline > 0) {
                return text.substring(firstNewline + 1, text.length() - 3).trim();
            }
        }
        return text;
    }
}
