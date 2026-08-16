package io.github.genkidoudou.common.file;

/**
 * {@link FileUploadHook#onError} 入参。
 */
public class FileUploadErrorContext {

  private final FileUploadBeforeContext before;
  private final Throwable error;
  private final String relativePath;

  /**
   * @param before       上传前上下文
   * @param error        失败原因
   * @param relativePath 若已生成路径则可能非 null
   */
  public FileUploadErrorContext(FileUploadBeforeContext before, Throwable error, String relativePath) {
    this.before = before;
    this.error = error;
    this.relativePath = relativePath;
  }

  public FileUploadBeforeContext getBefore() {
    return before;
  }

  public Throwable getError() {
    return error;
  }

  /**
   * 若失败发生在写入存储之后可能已生成路径，否则为 null。
   */
  public String getRelativePath() {
    return relativePath;
  }
}
