package io.github.genkidoudou.web.system.importtask.service;

import io.github.genkidoudou.web.system.importtask.dto.ImportSubmitResultVo;
import io.github.genkidoudou.web.system.importtask.dto.ImportTaskQueryBo;
import io.github.genkidoudou.web.system.importtask.dto.ImportTaskVo;
import io.github.genkidoudou.web.system.file.service.SysFileService;
import io.github.genkidoudou.common.api.PageInfo;
import org.springframework.web.multipart.MultipartFile;

/**
 * Excel 导入编排服务。
 */
public interface ImportOrchestratorService {

    /**
     * 提交导入（自动分流同步/异步）。
     */
    ImportSubmitResultVo submit(MultipartFile file, String bizType, boolean updateSupport,
                                String mode, Integer syncMaxRows, String contextJson);

    /**
     * 查询任务详情。
     */
    ImportTaskVo getTask(Long taskId);

    /**
     * 分页列表（默认当前用户）。
     */
    PageInfo<ImportTaskVo> listTasks(ImportTaskQueryBo query);

    /**
     * 执行异步任务（LOAD + PROCESS），供异步执行器调用。
     */
    void executeAsyncTask(Long taskId);

    /**
     * 下载导入失败明细（校验任务归属或文件管理下载权限）。
     */
    SysFileService.DownloadPayload downloadErrorFile(Long fileId);
}
