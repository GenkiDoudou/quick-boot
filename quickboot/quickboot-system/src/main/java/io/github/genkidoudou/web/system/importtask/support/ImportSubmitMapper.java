package io.github.genkidoudou.web.system.importtask.support;

import io.github.genkidoudou.common.excel.ExcelImportResult;
import io.github.genkidoudou.web.system.importtask.dto.ImportSubmitResultVo;
import io.github.genkidoudou.web.system.user.dto.UserImportResultVo;

import java.util.ArrayList;
import java.util.List;

/**
 * 将平台导入结果映射为各业务模块 VO。
 */
public final class ImportSubmitMapper {

    private ImportSubmitMapper() {
    }

    public static UserImportResultVo toUserImportResult(ImportSubmitResultVo src) {
        UserImportResultVo vo = new UserImportResultVo();
        if (src == null) {
            return vo;
        }
        vo.setTotal(src.getTotal() == null ? 0 : src.getTotal().intValue());
        vo.setSuccess(src.getSuccessCount() == null ? 0 : src.getSuccessCount().intValue());
        vo.setFailure(src.getFailCount() == null ? 0 : src.getFailCount().intValue());
        if (ImportMode.ASYNC.equals(src.getMode()) && src.getTaskId() != null) {
            List<String> msgs = new ArrayList<>();
            msgs.add("已提交后台导入，任务ID: " + src.getTaskId());
            vo.setFailureMessages(msgs);
            vo.setErrorKey("task:" + src.getTaskId());
        } else if (src.getErrorFileId() != null) {
            vo.setErrorKey("file:" + src.getErrorFileId());
            // UserImportResultVo 仅 errorKey，前端 mapImportPayload 可解析
        }
        return vo;
    }

    /**
     * 映射为通用 Excel 导入结果（角色、字典类型等）。
     */
    public static ExcelImportResult toExcelImportResult(ImportSubmitResultVo src) {
        ExcelImportResult vo = new ExcelImportResult();
        if (src == null) {
            return vo;
        }
        vo.setTotal(src.getTotal());
        vo.setSuccessCount(src.getSuccessCount());
        vo.setFailCount(src.getFailCount());
        vo.setMode(src.getMode());
        if (ImportMode.ASYNC.equals(src.getMode()) && src.getTaskId() != null) {
            vo.setTaskId(src.getTaskId());
            vo.setErrorKey("task:" + src.getTaskId());
        } else if (src.getErrorFileId() != null) {
            vo.setErrorFileId(src.getErrorFileId());
            vo.setErrorKey("file:" + src.getErrorFileId());
            vo.setErrorFileName("import-error.xlsx");
        }
        return vo;
    }
}
