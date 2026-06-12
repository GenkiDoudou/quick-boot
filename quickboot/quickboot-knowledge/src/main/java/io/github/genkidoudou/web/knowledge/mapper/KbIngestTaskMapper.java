package io.github.genkidoudou.web.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.genkidoudou.web.knowledge.domain.KbIngestTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * 异步入库任务 Mapper。
 */
@Mapper
public interface KbIngestTaskMapper extends BaseMapper<KbIngestTask> {
}
