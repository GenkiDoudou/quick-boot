package io.github.genkidoudou.web.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.genkidoudou.web.knowledge.domain.KbMcpServer;
import org.apache.ibatis.annotations.Mapper;

/**
 * MCP 服务配置 Mapper。
 */
@Mapper
public interface KbMcpServerMapper extends BaseMapper<KbMcpServer> {
}
