package io.github.genkidoudou.web.knowledge.service;

import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.web.knowledge.domain.KbKnowledgeBase;
import io.github.genkidoudou.web.knowledge.dto.KbKnowledgeBaseBo;
import io.github.genkidoudou.web.knowledge.dto.KbKnowledgeBaseQueryBo;
import io.github.genkidoudou.web.knowledge.dto.KbKnowledgeBaseVo;

import java.util.List;

/**
 * 知识库管理服务。
 */
public interface KbKnowledgeBaseService {

    /**
     * 知识库分页列表。
     */
    PageInfo<KbKnowledgeBaseVo> page(KbKnowledgeBaseQueryBo query);

    /**
     * 按 ID 查询详情。
     */
    KbKnowledgeBase getById(Long kbId);

    /**
     * 新增知识库。
     */
    void add(KbKnowledgeBaseBo req);

    /**
     * 修改知识库。
     */
    void update(KbKnowledgeBaseBo req);

    /**
     * 批量删除（级联删文档向量与记录）。
     */
    void removeBatch(List<Long> kbIds);
}
