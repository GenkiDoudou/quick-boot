package io.github.genkidoudou.common.file;

/**
 * 文件存储模块异常（路径不安全、读写失败等）。
 */
public class FileStorageException extends RuntimeException {

  /**
   * @param message 异常说明
   */
  public FileStorageException(String message) {
    super(message);
  }

  /**
   * @param message 异常说明
   * @param cause   根因
   */
  public FileStorageException(String message, Throwable cause) {
    super(message, cause);
  }
}
