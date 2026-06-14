package io.github.genkidoudou.web.ai.prompt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.genkidoudou.web.ai.prompt.domain.AiPromptAbRun;
import org.apache.ibatis.annotations.Mapper;

/**
 * 提示词 A/B 对比运行 Mapper。
 */
@Mapper
public interface AiPromptAbRunMapper extends BaseMapper<AiPromptAbRun> {
}
