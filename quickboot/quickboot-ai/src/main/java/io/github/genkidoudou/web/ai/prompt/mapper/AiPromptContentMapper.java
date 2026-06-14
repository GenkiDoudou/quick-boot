package io.github.genkidoudou.web.ai.prompt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.genkidoudou.web.ai.prompt.domain.AiPromptContent;
import org.apache.ibatis.annotations.Mapper;

/**
 * 提示词内容段 Mapper。
 */
@Mapper
public interface AiPromptContentMapper extends BaseMapper<AiPromptContent> {
}
