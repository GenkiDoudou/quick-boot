package io.github.genkidoudou.common.monitor;

/**
 * 未处理异常上报 SPI：由 app 层 {@code GlobalExceptionHandler} 通过 {@code ObjectProvider} 可选注入。
 * <p>
 * 实现方可将异常投影到链路追踪（如 LiteTrace be_error span）；无实现时 GEH 跳过上报。
 */
public interface ExceptionReporter {

  /**
   * 上报未处理异常；实现方应吞掉内部失败，避免影响 HTTP 响应。
   *
   * @param ex 未处理异常；为 {@code null} 时忽略
   */
  void report(Throwable ex);
}
