package io.github.genkidoudou.web.ai.prompt.service;

import io.github.genkidoudou.web.ai.prompt.dto.AiPromptOptimizeBo;
import io.github.genkidoudou.web.ai.prompt.dto.AiPromptOptimizeResultVo;

/**
 * AI 提示词优化服务。
 */
public interface AiPromptOptimizeService {

    /**
     * 根据输入内容同步调用大模型生成优化后的提示词（超时 60s）。
     *
     * @param req 优化入参
     * @return 优化结果
     */
    AiPromptOptimizeResultVo optimize(AiPromptOptimizeBo req);
}
