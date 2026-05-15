package io.github.genkidoudou.common.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分页返回信息
 *
 * @param <T> 数据类型
 * @author genkidoudou
 * @since 2023/09/09
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageInfo<T> implements Serializable {

    private static final long serialVersionUID = 1L;

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
     * 返回内容
     *
     * @since 2023/09/09
     */
    private List<T> records;

    /**
     * 总条数
     *
     * @since 2023/09/09
     */
    private long total;

    /**
     * 总页数
     *
     * @since 2023/09/09
     */
    private long pages;

    /**
     * 扩展字段
     *
     * @since 2023/09/09
     */
    private Map<String, Object> ext = new HashMap<>();

    /**
     * 构造分页信息
     *
     * @param current 当前页
     * @param size    每页条数
     * @param records 数据列表
     * @param total   总条数
     */
    public PageInfo(long current, long size, List<T> records, long total) {
        this.current = current;
        this.size = size;
        this.records = records;
        this.total = total;
        this.pages = size > 0 ? (total + size - 1) / size : 0;
    }

    /**
     * 添加扩展字段
     *
     * @param key   键
     * @param value 值
     * @return 当前对象
     */
    public PageInfo<T> addExt(String key, Object value) {
        if (this.ext == null) {
            this.ext = new HashMap<>();
        }
        this.ext.put(key, value);
        return this;
    }
}
