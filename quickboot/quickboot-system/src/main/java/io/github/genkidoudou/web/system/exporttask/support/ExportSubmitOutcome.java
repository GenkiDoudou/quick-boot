package io.github.genkidoudou.web.system.exporttask.support;

import io.github.genkidoudou.web.system.exporttask.dto.ExportSubmitResultVo;

/**
 * 导出提交编排结果：由 Controller 决定写 Excel 流或 JSON。
 */
public sealed interface ExportSubmitOutcome permits ExportSubmitOutcome.SyncStream, ExportSubmitOutcome.AsyncAccepted {

    /**
     * 同步直出：Excel 字节与下载文件名（可含 {@code .xlsx} 后缀）。
     */
    record SyncStream(byte[] bytes, String fileName) implements ExportSubmitOutcome {
    }

    /**
     * 异步已接受：返回任务摘要（不含 {@code resultFileId}）。
     */
    record AsyncAccepted(ExportSubmitResultVo vo) implements ExportSubmitOutcome {
    }
}
