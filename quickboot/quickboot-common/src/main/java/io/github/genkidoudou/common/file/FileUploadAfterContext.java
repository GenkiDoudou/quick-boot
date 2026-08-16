package io.github.genkidoudou.common.file;

/**
 * {@link FileUploadHook#afterUpload} 入参。
 */
public class FileUploadAfterContext {

  private final String relativePath;
  private final FileUploadBeforeContext before;

  /**
   * @param relativePath 已落盘的相对路径
   * @param before       对应的上传前上下文
   */
  public FileUploadAfterContext(String relativePath, FileUploadBeforeContext before) {
    this.relativePath = relativePath;
    this.before = before;
  }

  public String getRelativePath() {
    return relativePath;
  }

  public FileUploadBeforeContext getBefore() {
    return before;
  }
}
