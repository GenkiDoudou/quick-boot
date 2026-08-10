package io.github.genkidoudou.monitor.internal.clienttrack.service;

import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.monitor.internal.clienttrack.dto.ClientTrackReportBo;
import io.github.genkidoudou.monitor.internal.clienttrack.dto.ClientTrackPageVisitNodeVo;
import io.github.genkidoudou.monitor.internal.clienttrack.dto.ClientTrackTimelinePageQueryBo;
import io.github.genkidoudou.monitor.internal.clienttrack.dto.ClientTrackTimelineQueryBo;
import io.github.genkidoudou.monitor.internal.clienttrack.dto.ClientTrackTimelineVo;
import io.github.genkidoudou.monitor.internal.clienttrack.dto.SysClientTrackQueryBo;
import io.github.genkidoudou.monitor.internal.clienttrack.dto.SysClientTrackVo;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 前端用户行为监控批次服务。
 */
public interface SysClientTrackService {

    /**
     * 接收 quick-ui 上报的一批事件并落库。
     *
     * @param body    上报体
     * @param request 用于解析客户端 IP
     */
    void report(ClientTrackReportBo body, HttpServletRequest request);

    /**
     * 管理端分页查询批次。
     *
     * @param query 查询条件
     * @return 分页结果
     */
    PageInfo<SysClientTrackVo> page(SysClientTrackQueryBo query);

    /**
     * 行为轨迹聚合：按 browserVisitId / sessionId / userName 合并批次为页面跳转 + 操作树。
     *
     * @param query 至少一项主键条件；用户名查询合并时间范围内全部批次
     * @return 聚合结果（最多 500 批，超出 {@code truncated=true}）
     */
    ClientTrackTimelineVo timeline(ClientTrackTimelineQueryBo query);

    /**
     * 行为轨迹单页明细：在 timeline 概览范围内按 pageVisitId / pagePath 加载操作批与事件（懒加载）。
     *
     * @param query 与 timeline 相同的主键范围 + sessionId + pageVisitId 或 pagePath
     * @return 单页节点（含 pageVisitBatch、actions、events）
     */
    ClientTrackPageVisitNodeVo timelinePageDetail(ClientTrackTimelinePageQueryBo query);

    /**
     * 按主键批量删除。
     *
     * @param batchIds 批次 ID 列表
     */
    void removeBatch(List<Long> batchIds);

    /**
     * 清空全部监控批次（不可恢复，仅管理端维护使用）。
     */
    void cleanAll();
}
