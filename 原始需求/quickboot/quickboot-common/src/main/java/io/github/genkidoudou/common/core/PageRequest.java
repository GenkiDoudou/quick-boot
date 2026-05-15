package io.github.genkidoudou.common.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 分页请求
 *
 * @param <T> 参数类型
 * @author genkidoudou
 * @since 2023/09/09
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageRequest<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页数
     *
     * @since 2023/09/09
     */
    private long current = 1;

    /**
     * 每页条数
     *
     * @since 2023/09/09
     */
    private long size = 10;

    /**
     * 分页参数
     *
     * @since 2023/09/09
     */
    private T param;

    /**
     * 构造分页请求
     *
     * @param current 当前页
     * @param size    每页条数
     */
    public PageRequest(long current, long size) {
        this.current = current;
        this.size = size;
    }

    /**
     * 获取偏移量
     *
     * @return 偏移量
     */
    public long getOffset() {
        return (current - 1) * size;
    }
}
