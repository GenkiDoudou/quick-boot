package io.github.genkidoudou.web.system.exporttask.service;

import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.web.system.exporttask.dto.ExportTaskQueryBo;
import io.github.genkidoudou.web.system.exporttask.dto.ExportTaskVo;
import io.github.genkidoudou.web.system.exporttask.support.ExportSubmitOutcome;

import java.util.Map;

/**
 * Excel 导出编排服务。
 */
public interface ExportOrchestratorService {

    /**
     * 提交导出并返回写出形态（同步字节流或异步 JSON 载荷）。
     */
    ExportSubmitOutcome submitForResponse(String bizType, Map<String, Object> queryParams, String mode, Integer syncMaxRows);

    ExportTaskVo getTask(Long taskId);

    PageInfo<ExportTaskVo> listTasks(ExportTaskQueryBo query);

    void executeAsyncTask(Long taskId);
}
