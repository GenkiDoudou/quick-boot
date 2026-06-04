package io.github.genkidoudou.common.exporttask;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Excel 导出编排配置（{@code qc.export.*}）。
 */
@Data
@ConfigurationProperties(prefix = "qc.export")
public class QcExportProperties {

    /** 是否启用平台导出编排。 */
    private boolean enabled = true;

    /** 同步路径默认最大行数，超过走异步。 */
    private int syncMaxRows = 500;

    /** 请求 {@code syncMaxRows} 覆盖时的硬顶。 */
    private int syncMaxRowsCap = 5000;

    /** 同步 HTTP 建议超时（秒）。 */
    private int syncTimeoutSeconds = 120;

    /** 全局同时执行的异步导出任务数上限。 */
    private int asyncMaxConcurrent = 3;

    /** 单次异步导出最大行数（防止 OOM）。 */
    private int asyncMaxRows = 50000;

    /** 结果 Excel 文件分类（须在 {@code qc.file.classifies} 配置）。 */
    private String resultClassify = "export-result";

    /**
     * 同步导出是否写入 {@code sys_export_task} 审计（不设置 {@code result_file_id}）。
     * 默认 false：同步不落任务表。
     */
    private boolean syncWriteTask = false;
}
