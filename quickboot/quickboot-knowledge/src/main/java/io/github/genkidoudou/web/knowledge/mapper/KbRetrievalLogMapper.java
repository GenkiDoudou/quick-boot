package io.github.genkidoudou.web.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.genkidoudou.web.knowledge.domain.KbRetrievalLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 检索测试历史 Mapper。
 */
@Mapper
public interface KbRetrievalLogMapper extends BaseMapper<KbRetrievalLog> {
}
