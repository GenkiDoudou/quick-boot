package io.github.genkidoudou.web.ai.prompt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.genkidoudou.web.ai.prompt.domain.AiPrompt;
import org.apache.ibatis.annotations.Mapper;

/**
 * 提示词主表 Mapper。
 */
@Mapper
public interface AiPromptMapper extends BaseMapper<AiPrompt> {
}
