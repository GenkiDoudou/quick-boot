package io.github.genkidoudou.web.system.importtask.support;

import cn.hutool.json.JSONUtil;
import io.github.genkidoudou.common.importtask.QcImportProperties;
import io.github.genkidoudou.web.system.importtask.dto.ImportSubmitResultVo;
import io.github.genkidoudou.web.system.importtask.service.ImportOrchestratorService;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 业务 Controller 委托平台导入编排的便捷方法。
 */
public final class ImportOrchestratorSupport {

    private ImportOrchestratorSupport() {
    }

    /**
     * 在编排启用时提交导入；未启用时返回 {@code null} 由调用方走旧 Service。
     */
    public static ImportSubmitResultVo submitIfEnabled(QcImportProperties props,
                                                       ImportOrchestratorService orchestrator,
                                                       MultipartFile file,
                                                       String bizType,
                                                       boolean updateSupport,
                                                       String mode,
                                                       Integer syncMaxRows,
                                                       String contextJson) {
        if (props == null || !props.isEnabled()) {
            return null;
        }
        return orchestrator.submit(file, bizType, updateSupport, mode, syncMaxRows, contextJson);
    }

    public static String toContextJson(Map<String, ?> context) {
        if (context == null || context.isEmpty()) {
            return null;
        }
        return JSONUtil.toJsonStr(context);
    }
}
