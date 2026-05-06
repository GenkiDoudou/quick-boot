package io.github.genkidoudou.common.api;

import jakarta.validation.constraints.Min;

/**
 * Controller 层分页入参契约；Service 内可再转为 MyBatis-Plus {@link com.baomidou.mybatisplus.extension.plugins.pagination.Page}。
 * <p>
 * {@code current} 默认 1，{@code size} 默认 10；{@code size} 最小为 1（校验 {@link Min}）。
 *
 * @param <T> 查询条件载荷类型
 */
public class PageRequest<T> {

    @Min(1)
    private int current = 1;

    /** 页大小，默认 10，最小 1。 */
    @Min(1)
    private int size = 10;

    private T param;

    public PageRequest() {
    }

    public PageRequest(int current, int size, T param) {
        this.current = current;
        this.size = size;
        this.param = param;
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public T getParam() {
        return param;
    }

    public void setParam(T param) {
        this.param = param;
    }

    /**
     * 数据库偏移量：{@code (current - 1) * size}。
     *
     * @return 非负偏移
     */
    public long getOffset() {
        return (long) (current - 1) * size;
    }
}
