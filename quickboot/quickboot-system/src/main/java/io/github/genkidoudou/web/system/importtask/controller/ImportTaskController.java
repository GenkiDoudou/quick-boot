package io.github.genkidoudou.web.system.importtask.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.web.system.importtask.dto.ImportSubmitResultVo;
import io.github.genkidoudou.web.system.importtask.dto.ImportTaskQueryBo;
import io.github.genkidoudou.web.system.importtask.dto.ImportTaskVo;
import io.github.genkidoudou.common.excel.ExcelUtils;
import io.github.genkidoudou.web.system.importtask.service.ImportOrchestratorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * 平台 Excel 导入任务 API。
 */
@Tag(name = "导入任务")
@Validated
@RestController
@RequestMapping("/import")
@RequiredArgsConstructor
public class ImportTaskController {

    private final ImportOrchestratorService orchestratorService;

    @Operation(summary = "提交 Excel 导入")
    @SaCheckPermission("system:ioCenter:submit")
    @PostMapping(value = "/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<ImportSubmitResultVo> submit(
        @Parameter(description = "Excel 文件") @RequestPart("file") MultipartFile file,
        @Parameter(description = "业务编码，如 system:user") @RequestParam String bizType,
        @Parameter(description = "是否更新已存在数据") @RequestParam(defaultValue = "false") boolean updateSupport,
        @Parameter(description = "sync 或 async；async 强制异步") @RequestParam(required = false) String mode,
        @Parameter(description = "覆盖本次同步行数上限") @RequestParam(required = false) Integer syncMaxRows,
        @Parameter(description = "业务上下文 JSON，如 {\"dictType\":\"sys_user_sex\"}") @RequestParam(required = false) String contextJson
    ) {
        return R.ok(orchestratorService.submit(file, bizType, updateSupport, mode, syncMaxRows, contextJson));
    }

    @Operation(summary = "查询导入任务")
    @SaCheckPermission("system:ioCenter:list")
    @GetMapping("/task/{taskId}")
    public R<ImportTaskVo> getTask(@PathVariable Long taskId) {
        return R.ok(orchestratorService.getTask(taskId));
    }

    @Operation(summary = "导入任务分页列表")
    @SaCheckPermission("system:ioCenter:list")
    @GetMapping("/task/list")
    public R<PageInfo<ImportTaskVo>> list(@Validated ImportTaskQueryBo query) {
        return R.ok(orchestratorService.listTasks(query));
    }

    @Operation(summary = "下载导入失败明细（任务归属或文件下载权限）")
    @GetMapping("/error-file/{fileId}")
    public void downloadErrorFile(
        @Parameter(description = "失败明细文件 ID") @PathVariable @Min(1) Long fileId,
        HttpServletResponse response) throws Exception {
        var payload = orchestratorService.downloadErrorFile(fileId);
        String ct = payload.contentType();
        response.setContentType(ct != null && !ct.isBlank() ? ct
            : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        ExcelUtils.setAttachmentResponseHeader(response, payload.originalName());
        try (InputStream in = payload.resource().getInputStream()) {
            in.transferTo(response.getOutputStream());
        }
    }
}
