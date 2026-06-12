package io.github.genkidoudou.web.knowledge.service.impl;

import cn.hutool.core.bean.BeanUtil;
import io.github.genkidoudou.web.knowledge.domain.KbIngestTask;
import io.github.genkidoudou.web.knowledge.dto.KbIngestTaskVo;
import io.github.genkidoudou.web.knowledge.mapper.KbIngestTaskMapper;
import io.github.genkidoudou.web.knowledge.service.KbIngestTaskService;
import org.springframework.stereotype.Service;

/**
 * 异步入库任务查询服务实现。
 */
@Service
public class KbIngestTaskServiceImpl implements KbIngestTaskService {

    private final KbIngestTaskMapper taskMapper;

    public KbIngestTaskServiceImpl(KbIngestTaskMapper taskMapper) {
        this.taskMapper = taskMapper;
    }

    @Override
    public KbIngestTaskVo getInfo(Long taskId) {
        if (taskId == null) {
            return null;
        }
        KbIngestTask task = taskMapper.selectById(taskId);
        if (task == null) {
            return null;
        }
        return BeanUtil.copyProperties(task, KbIngestTaskVo.class);
    }
}
