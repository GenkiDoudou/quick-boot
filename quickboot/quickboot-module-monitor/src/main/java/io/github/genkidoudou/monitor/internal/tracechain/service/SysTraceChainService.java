package io.github.genkidoudou.monitor.internal.tracechain.service;

import io.github.genkidoudou.monitor.internal.tracechain.dto.TraceChainGraphVo;
import io.github.genkidoudou.monitor.internal.tracechain.dto.TraceChainQueryBo;

/**
 * 全链路监控聚合查询。
 */
public interface SysTraceChainService {

    /**
     * 按 operationId / traceId / 会话范围等条件聚合前后端链路图。
     *
     * @param query 查询条件
     * @return 图数据
     */
    TraceChainGraphVo graph(TraceChainQueryBo query);
}
