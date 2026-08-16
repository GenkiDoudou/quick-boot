package io.github.genkidoudou.monitor.internal.litetrace.support;

/**
 * 异常投影钩子（由 app 层 {@code GlobalExceptionHandler} 可选注入调用）。
 */
public interface LiteTraceExceptionReporter {

    /**
     * 将当前链路上的未处理异常投影为 be_error span。
     *
     * @param ex 未处理异常；为空则忽略
     */
    void report(Throwable ex);
}
