package io.github.genkidoudou.common.api;

import org.slf4j.MDC;

/**
 * 从 SLF4J {@link MDC} 读取链路 {@code traceId}，与 {@code logback} 中 {@code %X{traceId}} 约定一致。
 * <p>
 * {@code traceId} 依赖 <strong>Micrometer Tracing</strong>（及工程选用的 bridge）在 HTTP 请求观测路径上向 MDC
 * 写入与 {@link #MDC_KEY} 相同的键；本类<strong>不创建</strong> span、不替代框架侧传播逻辑。
 * <p>
 * 异步任务或自建线程池若未做 MDC / tracing 上下文传递，{@link #current()} 可能始终为 {@code null}，需由业务侧另行接入传播能力。
 * <p>
 * 若当前线程未设置 MDC（或值为空白），{@link #current()} 返回 {@code null}；{@link R} 序列化行为遵循全局 Jackson 策略（可能省略 null 字段）。
 */
public final class TraceIds {

    /** 与现有日志 pattern 中 {@code traceId=%X{traceId}} 对齐的 MDC 键名。 */
    public static final String MDC_KEY = "traceId";

    private TraceIds() {
    }

    /**
     * @return 非空白 traceId；否则 {@code null}
     */
    public static String current() {
        String v = MDC.get(MDC_KEY);
        if (v == null) {
            return null;
        }
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }
}
