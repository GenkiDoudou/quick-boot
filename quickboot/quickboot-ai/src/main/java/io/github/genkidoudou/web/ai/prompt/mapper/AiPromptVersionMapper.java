package io.github.genkidoudou.web.ai.prompt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.genkidoudou.web.ai.prompt.domain.AiPromptVersion;
import org.apache.ibatis.annotations.Mapper;

/**
 * 提示词版本快照 Mapper。
 */
@Mapper
public interface AiPromptVersionMapper extends BaseMapper<AiPromptVersion> {
}
