package io.github.genkidoudou.common.file;

/**
 * 上传生命周期钩子，多 Bean 时使用 Spring {@link org.springframework.core.annotation.Order} 排序。
 * <p>
 * 保留接口以供扩展；本期不注册全局 {@code sys_file} 登记钩子。
 */
public interface FileUploadHook {

  /**
   * 上传前调用；抛出任意运行时异常可中止上传。
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
   * 上传失败时调用。
   */
  default void onError(FileUploadErrorContext ctx) {
  }
}
