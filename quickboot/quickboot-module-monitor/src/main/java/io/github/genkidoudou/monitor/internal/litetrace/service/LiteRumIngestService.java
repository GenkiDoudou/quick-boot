package io.github.genkidoudou.monitor.internal.litetrace.service;

import io.github.genkidoudou.monitor.internal.litetrace.dto.RumIngestBo;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Lite RUM 事件接收与投影服务。
 */
public interface LiteRumIngestService {

    /**
     * 校验并持久化 RUM 事件，同时投影链路索引与 span。
     *
     * @param body    批量上报体
     * @param request 用于提取客户端 IP、User-Agent
     * @throws io.github.genkidoudou.common.exception.WarningException appId 非法或触发限流时
     */
    void ingest(RumIngestBo body, HttpServletRequest request);
}
