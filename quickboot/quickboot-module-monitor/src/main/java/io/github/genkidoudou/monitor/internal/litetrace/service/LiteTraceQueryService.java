package io.github.genkidoudou.monitor.internal.litetrace.service;

import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.monitor.internal.litetrace.dto.PageVisitVo;
import io.github.genkidoudou.monitor.internal.litetrace.dto.TraceIndexQueryBo;
import io.github.genkidoudou.monitor.internal.litetrace.dto.TraceIndexVo;
import io.github.genkidoudou.monitor.internal.litetrace.dto.TraceSpanVo;

import java.util.List;

/**
 * Lite 链路索引与 span 查询服务。
 */
public interface LiteTraceQueryService {

    /**
     * 分页查询链路索引。
     *
     * @param query 筛选、排序与分页条件
     * @return 链路索引分页结果
     */
    PageInfo<TraceIndexVo> page(TraceIndexQueryBo query);

    /** 按 pageVisitId 聚合的页面访问列表 */
    List<PageVisitVo> listPageVisits(TraceIndexQueryBo query);

    /** 某次页面访问下的全部 trace */
    List<TraceIndexVo> listByPageVisit(String pageVisitId, String beginTime, String endTime);

    /**
     * 按 traceId 查询链路索引详情。
     *
     * @param traceId 链路标识
     * @return 链路索引 VO
     */
    TraceIndexVo detail(String traceId);

    /**
     * 查询某条链路下的全部 span。
     *
     * @param traceId 链路标识
     * @return span 列表
     */
    List<TraceSpanVo> spans(String traceId);

    /** 写入纯 API/Job 样例或业务入口 index（供验证）。 */
    void ensureRoot(String traceId, String rootSource, String entry, String caller);
}
