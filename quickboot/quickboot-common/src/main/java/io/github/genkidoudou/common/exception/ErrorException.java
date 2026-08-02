package io.github.genkidoudou.common.exception;

/**
 * 严重异常：用于系统内部故障、关键依赖失败等需要按 5xx 语义处理的场景。
 */
public class ErrorException extends BaseException {


  public ErrorException(Integer code) {
    super(code);
  }

  public ErrorException(Integer code, Object... args) {
    super(code, args);
  }
}
