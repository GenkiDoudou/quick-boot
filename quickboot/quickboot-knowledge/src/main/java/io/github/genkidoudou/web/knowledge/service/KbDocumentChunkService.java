package io.github.genkidoudou.web.knowledge.service;

import io.github.genkidoudou.web.knowledge.dto.KbDocumentChunkUpdateBo;

/**
 * 文档分块维护：在线编辑与启用/禁用。
 */
public interface KbDocumentChunkService {

    /**
     * 更新分块正文和/或启用状态；正文变更或重新启用时会重嵌入向量。
     *
     * @param req 更新请求
     */
    void updateChunk(KbDocumentChunkUpdateBo req);
}
