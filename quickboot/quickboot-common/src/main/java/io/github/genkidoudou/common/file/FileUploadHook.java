package io.github.genkidoudou.common.file;

/**
 * 上传生命周期钩子，多 Bean 时使用 Spring {@link org.springframework.core.annotation.Order} 排序。
 */
public interface FileUploadHook {

    /**
     * 上传前调用；抛出任意运行时异常可中止上传（由全局异常处理转换）。
     *
     * @param ctx 上下文（尚无最终相对路径）
     */
    default void beforeUpload(FileUploadBeforeContext ctx) {
    }

    /**
     * 上传成功后调用。
     */
    default void afterUpload(FileUploadAfterContext ctx) {
    }

    /**
     * 上传失败时调用（含 before 阶段抛错之后若已实现补偿观察点，以实际调用链为准：本实现于 catch 中调用）。
     */
    default void onError(FileUploadErrorContext ctx) {
    }
}
