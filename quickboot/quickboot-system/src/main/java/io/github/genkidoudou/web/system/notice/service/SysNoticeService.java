package io.github.genkidoudou.web.system.notice.service;

import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.web.system.notice.dto.SysNoticeBo;
import io.github.genkidoudou.web.system.notice.dto.SysNoticeQueryBo;
import io.github.genkidoudou.web.system.notice.dto.SysNoticeVo;

import java.util.List;

/**
 * 通知公告服务。
 */
public interface SysNoticeService {

    /**
     * 分页查询通知公告（列表不返回正文大字段）。
     *
     * @param query 查询与分页条件
     * @return 分页结果
     */
    PageInfo<SysNoticeVo> page(SysNoticeQueryBo query);

    /**
     * 按主键查询详情（含正文）。
     *
     * @param noticeId 主键
     * @return 视图对象，不存在时为 {@code null}
     */
    SysNoticeVo getById(Long noticeId);

    /**
     * 新增通知公告。
     *
     * @param req 入参
     */
    void add(SysNoticeBo req);

    /**
     * 更新通知公告。
     *
     * @param req 入参
     */
    void update(SysNoticeBo req);

    /**
     * 批量物理删除。
     *
     * @param noticeIds 主键列表
     */
    void removeBatch(List<Long> noticeIds);
}
