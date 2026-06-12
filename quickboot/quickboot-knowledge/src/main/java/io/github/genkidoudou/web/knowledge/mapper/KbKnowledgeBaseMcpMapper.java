package io.github.genkidoudou.web.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.genkidoudou.web.knowledge.domain.KbKnowledgeBaseMcp;
import org.apache.ibatis.annotations.Mapper;

/**
 * 知识库与 MCP 绑定 Mapper。
 */
@Mapper
public interface KbKnowledgeBaseMcpMapper extends BaseMapper<KbKnowledgeBaseMcp> {
}
