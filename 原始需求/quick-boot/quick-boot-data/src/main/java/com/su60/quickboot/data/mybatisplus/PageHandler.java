package com.su60.quickboot.data.mybatisplus;

import cn.hutool.core.collection.CollectionUtil;
import com.su60.quickboot.common.core.PageInfo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

/**
 * 分页处理类
 *
 * @param <T>  分页返回泛型
 * @author luyanan
 * @since 2023/09/13
 **/
public interface PageHandler<T> {

    /**
     * 条件构造器处理
     *
     * @param param        参数实体类
     * @param queryWrapper 条件构造
     * @since 2023/09/13
     */
    default void queryWrapperHandler(T param, LambdaQueryWrapper<T> queryWrapper) {

    }


    /**
     * 构建PageInfo对象
     *
     * @param page 分页结果
     * @return PageInfo
     * @since 2023/09/13
     */
    default PageInfo<T> getPageInfo(IPage<T> page) {
        PageInfo<T> pageInfo = new PageInfo<>();
        pageInfo.setCurrent(page.getCurrent());
        pageInfo.setSize(page.getSize());
        pageInfo.setPages(page.getPages());
        List<T> records = page.getRecords();
        if (CollectionUtil.isNotEmpty(records)) {
            recordsHandler(records);
            for (T record : records) {
                recordHandler(record);
            }
        }

        pageInfo.setRecords(records);
        pageInfo.setTotal(page.getTotal());
        return pageInfo;
    }

    /**
     * 分页数据结果
     *
     * @param records 分页结果
     * @since 2023/09/13
     */
    default void recordsHandler(List<T> records) {

    }


    /**
     * 每行结果处理
     *
     * @param t 实体类
     * @since 2023/09/13
     */
    default void recordHandler(T t) {

    }
}
