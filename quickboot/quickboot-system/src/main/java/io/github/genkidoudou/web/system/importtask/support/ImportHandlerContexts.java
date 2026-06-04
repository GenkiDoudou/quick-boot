package io.github.genkidoudou.web.system.importtask.support;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import io.github.genkidoudou.web.system.importtask.domain.SysImportTask;
import io.github.genkidoudou.web.system.importtask.handler.ImportHandlerContext;

/**
 * 从任务记录构建 {@link ImportHandlerContext}。
 */
public final class ImportHandlerContexts {

    private ImportHandlerContexts() {
    }

    /**
     * 根据任务主键与业务编码创建上下文，并合并 {@code contextJson} 中的键值。
     */
    public static ImportHandlerContext fromTask(SysImportTask task) {
        ImportHandlerContext ctx = new ImportHandlerContext(task.getTaskId(), task.getBizType());
        mergeJson(ctx, task.getContextJson());
        return ctx;
    }

    /**
     * 将 JSON 字符串中的顶层字段写入上下文 attributes。
     */
    public static void mergeJson(ImportHandlerContext ctx, String contextJson) {
        if (ctx == null || StrUtil.isBlank(contextJson)) {
            return;
        }
        JSONObject obj = JSONUtil.parseObj(contextJson);
        for (String key : obj.keySet()) {
            ctx.setAttribute(key, obj.get(key));
        }
    }
}
