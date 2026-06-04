package io.github.genkidoudou.web.system.importtask.handler;

/**
 * 按业务编码处理 Excel 导入行。
 */
public interface BizImportHandler {

    /**
     * 业务编码，如 {@code system:user}。
     */
    String bizType();

    /**
     * EasyExcel 行模型类型。
     */
    Class<?> rowClass();

    /**
     * 任务开始前预加载（角色字典等），同步/异步 PROCESS 前调用。
     */
    default void beforeImport(ImportHandlerContext context) {
    }

    /**
     * 处理单行；成功返回 {@code null}，失败返回错误信息。
     *
     * @param row           行模型
     * @param overwrite     是否覆盖已存在数据
     * @param context       上下文
     */
    String processRow(Object row, boolean overwrite, ImportHandlerContext context);

    /**
     * 任务全部行处理完成后回调（如刷新字典缓存）。
     */
    default void afterImport(ImportHandlerContext context) {
    }
}
