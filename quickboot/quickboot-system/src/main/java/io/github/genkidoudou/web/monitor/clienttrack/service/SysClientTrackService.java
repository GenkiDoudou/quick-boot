package io.github.genkidoudou.web.monitor.clienttrack.service;

import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.web.monitor.clienttrack.dto.ClientTrackReportBo;
import io.github.genkidoudou.web.monitor.clienttrack.dto.SysClientTrackQueryBo;
import io.github.genkidoudou.web.monitor.clienttrack.dto.SysClientTrackVo;
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
     * 按主键批量删除。
     *
     * @param batchIds 批次 ID 列表
     */
    void removeBatch(List<Long> batchIds);
}
