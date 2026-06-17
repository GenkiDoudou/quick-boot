package io.github.genkidoudou.web.aiapp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.genkidoudou.web.aiapp.domain.AiAppMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 应用消息 Mapper。
 */
@Mapper
public interface AiAppMessageMapper extends BaseMapper<AiAppMessage> {
}
