package io.github.genkidoudou.web.system.logininfor.service;

import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.web.system.logininfor.dto.SysLogininforQueryBo;
import io.github.genkidoudou.web.system.logininfor.dto.SysLogininforVo;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

/**
 * 登录日志查询与维护。
 */
public interface SysLogininforService {

    /**
     * 分页列表。
     *
     * @param query 查询条件
     * @return 分页结果
     */
    PageInfo<SysLogininforVo> page(SysLogininforQueryBo query);

    /**
     * 按条件导出全部匹配行（无分页上限，数据量大时由运维控制筛选条件）。
     *
     * @param query    与列表一致
     * @param response 响应
     */
    void export(SysLogininforQueryBo query, HttpServletResponse response);

    /**
     * 批量删除。
     *
     * @param infoIds 主键集合
     */
    void removeBatch(List<Long> infoIds);

    /** 清空全部登录日志。 */
    void cleanAll();
}
