package io.github.genkidoudou.web.knowledge.service;

import io.github.genkidoudou.web.knowledge.dto.KbIngestTaskVo;

/**
 * 异步入库任务查询服务。
 */
public interface KbIngestTaskService {

    /**
     * 按任务 ID 查询入库进度。
     *
     * @param taskId 任务 ID
     * @return 任务详情，不存在时返回 null
     */
    KbIngestTaskVo getInfo(Long taskId);
}
