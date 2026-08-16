package io.github.genkidoudou.common.excel.exception;

/**
 * Excel 数据校验异常。
 */
public class ExcelDataCheckException extends RuntimeException {

  /**
   * @param message 校验失败说明（含字段/字典项等上下文）
   */
  public ExcelDataCheckException(String message) {
    super(message);
  }
}

