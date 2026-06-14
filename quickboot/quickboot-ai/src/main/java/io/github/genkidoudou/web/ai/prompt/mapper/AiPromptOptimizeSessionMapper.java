package io.github.genkidoudou.web.ai.prompt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.genkidoudou.web.ai.prompt.domain.AiPromptOptimizeSession;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 提示词优化会话 Mapper。
 */
@Mapper
public interface AiPromptOptimizeSessionMapper extends BaseMapper<AiPromptOptimizeSession> {
}
