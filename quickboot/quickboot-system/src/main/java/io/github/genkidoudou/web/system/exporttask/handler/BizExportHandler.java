package io.github.genkidoudou.web.system.exporttask.handler;

/**
 * 按业务编码执行导出数据统计与 Excel 生成。
 */
public interface BizExportHandler {

    /** 业务编码，如 {@code monitor:logininfor}。 */
    String bizType();

    /**
     * 按筛选条件统计可导出行数。
     *
     * @param queryJson 查询条件 JSON
     */
    long countRows(String queryJson);

    /**
     * 生成 Excel 字节（最多 {@code maxRows} 行）。
     *
     * @param queryJson 查询条件 JSON
     * @param maxRows   行数上限
     */
    byte[] writeExcelBytes(String queryJson, int maxRows);

    /** 默认下载文件名。 */
    String defaultFileName();
}
