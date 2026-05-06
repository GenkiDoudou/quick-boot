package io.github.genkidoudou.common.api;

/**
 * 统一 JSON 响应体：{@code code} / {@code msg} / {@code data} / {@code traceId} / {@code timestamp}。
 * <p>
 * 成功码固定为 {@link HttpCodes#OK}（200）；{@link #isSuccess()} 仅当 {@code code == 200}。错误场景请使用 {@link #error} 系列工厂方法。
 * <p>
 * {@code traceId} 在私有 {@code build} 路径中取自 {@link TraceIds#current()}，与日志 pattern 中 {@code %X{traceId}} 同源；
 * 未采样、非 Web 请求线程或当前无 Micrometer/MDC 上下文时可能为 {@code null}，序列化策略可能省略该字段。
 * <p>
 * {@code timestamp} 为创建该对象时的毫秒时间戳，始终有值。
 *
 * @param <T> 业务数据类型
 */
public final class R<T> {

    private int code;
    private String msg;
    private T data;
    private String traceId;
    private long timestamp;

    private R() {
    }

    /**
     * 等价于 {@code ok(null, null)}：成功、无提示文案、无载荷。
     */
    public static <T> R<T> ok() {
        return ok(null, null);
    }

    /**
     * 成功，仅带提示信息。
     */
    public static <T> R<T> ok(String msg) {
        return ok(msg, null);
    }

    /**
     * 成功，仅带业务数据；提示信息为 {@code null}。
     */
    public static <T> R<T> ok(T data) {
        return ok(null, data);
    }

    /**
     * 成功，带提示与业务数据。
     */
    public static <T> R<T> ok(String msg, T data) {
        return build(HttpCodes.OK, msg, data);
    }

    /**
     * 失败：{@code code} 为 {@link HttpCodes#INTERNAL_ERROR}，无文案、无载荷。
     */
    public static <T> R<T> error() {
        return error(HttpCodes.INTERNAL_ERROR, null, null);
    }

    /**
     * 失败：{@code code} 为 {@link HttpCodes#INTERNAL_ERROR}。
     */
    public static <T> R<T> error(String msg) {
        return error(HttpCodes.INTERNAL_ERROR, msg, null);
    }

    /**
     * 失败，指定业务码。
     */
    public static <T> R<T> error(int code, String msg) {
        return error(code, msg, null);
    }

    /**
     * 失败，指定业务码，可选携带结构化扩展数据（如校验明细）。
     */
    public static <T> R<T> error(int code, String msg, T data) {
        return build(code, msg, data);
    }

    private static <T> R<T> build(int code, String msg, T data) {
        R<T> r = new R<>();
        r.code = code;
        r.msg = msg;
        r.data = data;
        r.traceId = TraceIds.current();
        r.timestamp = System.currentTimeMillis();
        return r;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /** @return 当且仅当 {@code code == 200} */
    public boolean isSuccess() {
        return code == HttpCodes.OK;
    }

    /** @return {@code !isSuccess()} */
    public boolean isError() {
        return !isSuccess();
    }
}
