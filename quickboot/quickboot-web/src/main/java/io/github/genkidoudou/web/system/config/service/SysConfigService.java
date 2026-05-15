package io.github.genkidoudou.web.system.config.service;

import io.github.genkidoudou.web.system.config.domain.SysConfig;
import io.github.genkidoudou.web.system.config.dto.SysConfigBo;
import io.github.genkidoudou.web.system.config.dto.SysConfigQueryBo;

import java.util.List;

/**
 * 系统参数服务。
 */
public interface SysConfigService {
    /**
     * 查询参数列表。
     *
     * @param query 查询条件
     * @return 参数列表
     */
    List<SysConfig> list(SysConfigQueryBo query);

    /**
     * 按ID查询参数。
     *
     * @param configId 参数ID
     * @return 参数
     */
    SysConfig getById(Long configId);

    /**
     * 按键名查询参数值。
     *
     * @param configKey 参数键名
     * @return 参数值
     */
    String getConfigValueByKey(String configKey);

    /**
     * 新增参数。
     *
     * @param req 请求
     */
    void add(SysConfigBo req);

    /**
     * 修改参数。
     *
     * @param req 请求
     */
    void update(SysConfigBo req);

    /**
     * 批量删除参数。
     *
     * @param configIds 参数ID集合
     */
    void removeBatch(List<Long> configIds);

    /**
     * 刷新全部参数缓存。
     */
    void refreshCache();
}
