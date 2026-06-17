package io.github.genkidoudou.web.aiapp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.genkidoudou.web.aiapp.domain.AiAppSession;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 应用会话 Mapper。
 */
@Mapper
public interface AiAppSessionMapper extends BaseMapper<AiAppSession> {
}
