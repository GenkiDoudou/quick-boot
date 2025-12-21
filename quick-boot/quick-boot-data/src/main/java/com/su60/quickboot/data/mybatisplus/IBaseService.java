package com.su60.quickboot.data.mybatisplus;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.extension.service.IService;
import com.su60.quickboot.common.core.PageInfo;
import com.su60.quickboot.common.core.PageRequest;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 基础Service
 *
 * @param <T> 实体类泛型
 * @author luyanan
 * @since 2023/09/13
 **/
public interface IBaseService<T> extends IService<T> {


    /**
     * 分页
     *
     * @param pageRequest 分页参数
     * @param pageHandler 分页处理类
     * @return 分页返回
     * @since 2023/09/13
     */
    PageInfo<T> page(PageRequest<T> pageRequest, PageHandler<T> pageHandler);


    /**
     * 分页
     *
     * @param pageRequest 分页参数
     * @return 分页返回
     * @since 2023/09/13
     */
    default PageInfo<T> page(PageRequest<T> pageRequest) {
        return page(pageRequest, null);
    }

    /**
     * vo分页
     *
     * @param <V>           vo类泛型
     * @param pageRequest   分页参数
     * @param voClass       vo类
     * @param pageVoHandler 分页处理器
     * @return 分页结果
     * @since 2023/09/13
     */
    <V> PageInfo<V> pageVo(PageRequest pageRequest, Class<V> voClass, PageVoHandler<T, V> pageVoHandler);

    /**
     * vo分页
     *
     * @param <V>         vo类泛型
     * @param pageRequest 分页参数
     * @param voClass     vo类
     * @return 分页结果
     * @since 2023/09/13
     */
    default <V> PageInfo<V> pageVo(PageRequest pageRequest, Class<V> voClass) {
        return pageVo(pageRequest, voClass, null);
    }


    @Override
    default boolean removeById(Serializable id) {
        return this.removeById(id, true);
    }

    @Override
    default List<T> listByIds(Collection<? extends Serializable> idList) {
        if (CollectionUtil.isEmpty(idList)) {
            return new ArrayList<>();
        }
        return this.getBaseMapper().selectBatchIds(idList);
    }


    /**
     * 构建Page对象
     *
     * @param pageRequest 分页参数
     * @return Page对象
     * @since 2023/09/13
     */
    default IPage getPage(PageRequest pageRequest) {
        IPage page = new Page<>();
        page.setCurrent(pageRequest.getCurrent());
        page.setSize(pageRequest.getSize());
        return page;
    }
}
