package io.github.genkidoudou.common.importtask;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Excel 导入编排配置（{@code qc.import.*}）。
 */
@Data
@ConfigurationProperties(prefix = "qc.import")
public class QcImportProperties {

    /** 是否启用平台导入编排；为 false 时业务可继续走原有 importData。 */
    private boolean enabled = true;

    /** 同步路径默认最大行数。 */
    private int syncMaxRows = 500;

    /** 请求 {@code syncMaxRows} 覆盖时的硬顶。 */
    private int syncMaxRowsCap = 2000;

    /** 同步 HTTP 建议超时（秒），供文档与前端对齐。 */
    private int syncTimeoutSeconds = 120;

    /** 全局同时执行的异步导入任务数上限。 */
    private int asyncMaxConcurrent = 3;

    /** 暂存表批量写入条数。 */
    private int stagingBatchSize = 200;

    /** 任务结束后保留导入相关文件的天数（清理任务可后续实现）。 */
    private int fileRetentionDays = 7;

    /** 原始 Excel 文件分类（须在 {@code qc.file.classifies} 配置）。 */
    private String sourceClassify = "import-source";

    /** 失败明细 Excel 文件分类。 */
    private String errorClassify = "import-error";
}
