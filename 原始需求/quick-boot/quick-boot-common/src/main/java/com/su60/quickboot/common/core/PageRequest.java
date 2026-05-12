package com.su60.quickboot.common.core;

import lombok.Data;

/**
 * 分页参数
 *
 * @param <T> 分页实体类泛型
 * @author luyanan
 * @since 2023/09/09
 **/
@Data

public class PageRequest<T> {


    /**
     * 当前页数
     *
     * @since 2023/09/09
     */

    private long current;


    /**
     * 每页条数
     *
     * @since 2023/09/09
     */

    private long size;


    /**
     * 分页参数
     *
     * @since 2023/09/09
     */

    private T param;
}
