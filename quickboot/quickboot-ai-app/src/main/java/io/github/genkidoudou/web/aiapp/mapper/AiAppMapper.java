package io.github.genkidoudou.web.aiapp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.genkidoudou.web.aiapp.domain.AiApp;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 应用定义 Mapper。
 */
@Mapper
public interface AiAppMapper extends BaseMapper<AiApp> {
}
