package io.github.genkidoudou.web.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.genkidoudou.web.workflow.domain.WfApiKey;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工作流 API Key Mapper（P0 预留）。
 */
@Mapper
public interface WfApiKeyMapper extends BaseMapper<WfApiKey> {
}
