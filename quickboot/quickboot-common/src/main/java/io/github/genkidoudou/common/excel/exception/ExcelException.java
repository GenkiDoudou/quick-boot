package io.github.genkidoudou.common.excel.exception;

/**
 * Excel 通用异常。
 */
public class ExcelException extends RuntimeException {

  /**
   * @param message 异常说明
   */
  public ExcelException(String message) {
    super(message);
  }

  /**
   * @param message 异常说明
   * @param cause   根因
   */
  public ExcelException(String message, Throwable cause) {
    super(message, cause);
  }
}
