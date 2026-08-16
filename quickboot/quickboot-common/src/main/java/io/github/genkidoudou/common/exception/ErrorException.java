package io.github.genkidoudou.common.exception;

/**
 * 严重异常：用于系统内部故障、关键依赖失败等需要按 5xx 语义处理的场景。
 */
public class ErrorException extends BaseException {


  /**
   * 仅指定业务码。
   *
   * @param code 业务错误码
   */
  public ErrorException(Integer code) {
    super(code);
  }

  /**
   * 指定业务码与 i18n 占位参数。
   *
   * @param code 业务错误码
   * @param args 国际化占位参数
   */
  public ErrorException(Integer code, Object... args) {
    super(code, args);
  }
}
