package io.github.genkidoudou.web.ai.prompt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.genkidoudou.web.ai.prompt.domain.AiPromptVariable;
import org.apache.ibatis.annotations.Mapper;

/**
 * 提示词变量声明 Mapper。
 */
@Mapper
public interface AiPromptVariableMapper extends BaseMapper<AiPromptVariable> {
}
