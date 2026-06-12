package io.github.genkidoudou.web.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.genkidoudou.web.knowledge.domain.KbMcpEnv;
import org.apache.ibatis.annotations.Mapper;

/**
 * MCP 环境变量 Mapper。
 */
@Mapper
public interface KbMcpEnvMapper extends BaseMapper<KbMcpEnv> {
}
