package io.github.genkidoudou.monitor.internal.litetrace.service;

import io.github.genkidoudou.monitor.internal.litetrace.entity.SysTraceIndex;
import io.github.genkidoudou.monitor.internal.litetrace.entity.SysTraceSpan;

import java.time.LocalDateTime;

/**
 * 将各来源投影为 {@code sys_trace_index} / {@code sys_trace_span}。
 */
public interface TraceProjectionService {

    /**
     * 插入或合并更新链路索引。
     *
     * @param patch 待写入字段；traceId 为空则忽略
     */
    void upsertIndex(SysTraceIndex patch);

    /**
     * 插入链路 span；traceId 为空或插入失败时静默跳过。
     *
     * @param span 待插入 span
     */
    void insertSpan(SysTraceSpan span);

    /**
     * 将 HTTP 访问投影为 service span 并更新索引。
     *
     * @param traceId     链路标识
     * @param appId       应用标识
     * @param rootSource  根来源：browser / api 等
     * @param entry       入口描述
     * @param caller      调用方名称
     * @param operationId 客户端操作 ID
     * @param method      HTTP 方法
     * @param uri         请求 URI
     * @param status      HTTP 状态码
     * @param durationMs  耗时毫秒
     * @param clientIp    客户端 IP
     * @param ua          User-Agent
     * @param startedAt   请求开始时间
     */
    void projectAccess(String traceId, String appId, String rootSource, String entry, String caller,
                       String operationId, String method, String uri, int status, long durationMs,
                       String clientIp, String ua, LocalDateTime startedAt);

    /**
     * 将慢 SQL 投影为 sql span，并按需补全索引。
     *
     * @param traceId  链路标识
     * @param sqlText  SQL 文本
     * @param costMs   耗时毫秒
     * @param mapperId MyBatis Mapper 标识
     */
    void projectSql(String traceId, String sqlText, long costMs, String mapperId);

    /**
     * 将后端未处理异常投影为 be_error span 并标记索引失败。
     *
     * @param traceId       链路标识
     * @param summary       异常摘要
     * @param stackSnippet  堆栈片段
     */
    void projectBeError(String traceId, String summary, String stackSnippet);
}
