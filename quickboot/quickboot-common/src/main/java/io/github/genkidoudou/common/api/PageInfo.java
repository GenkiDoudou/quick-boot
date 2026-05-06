package io.github.genkidoudou.common.api;

import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Controller 层分页出参契约，由持久层分页结果回填。
 * <p>
 * {@link #pages} 使用 {@code (total + size - 1) / size} 计算（要求 {@code size >= 1}）；若 {@code size < 1}，总页数为 0。
 *
 * @param <T> 记录元素类型
 */
public class PageInfo<T> {

    private long current;
    private long size;
    private List<T> records = Collections.emptyList();
    private long total;
    private long pages;
    private Map<String, Object> ext;

    public PageInfo() {
    }

    /**
     * 由 MyBatis-Plus {@link IPage} 构造分页出参。
     *
     * @param page 持久层分页结果，不可为 {@code null}
     * @param <T>  元素类型
     * @return 填充了 {@code current/size/records/total/pages} 的 {@link PageInfo}
     */
    public static <T> PageInfo<T> from(IPage<T> page) {
        if (page == null) {
            throw new IllegalArgumentException("page must not be null");
        }
        long size = page.getSize();
        long total = page.getTotal();
        PageInfo<T> info = new PageInfo<>();
        info.setCurrent(page.getCurrent());
        info.setSize(size);
        List<T> rec = page.getRecords();
        info.setRecords(rec != null ? rec : Collections.emptyList());
        info.setTotal(total);
        info.setPages(computePages(total, size));
        return info;
    }

    /**
     * 与 {@link #from(IPage)} 相同，并附带扩展字段。
     */
    public static <T> PageInfo<T> from(IPage<T> page, Map<String, Object> ext) {
        PageInfo<T> info = from(page);
        info.setExt(ext);
        return info;
    }

    /**
     * 按约定公式计算总页数。
     *
     * @param total 总条数
     * @param size  页大小，须 ≥ 1 才有意义
     * @return 总页数；{@code size < 1} 时返回 0
     */
    public static long computePages(long total, long size) {
        if (size < 1) {
            return 0;
        }
        return (total + size - 1) / size;
    }

    public long getCurrent() {
        return current;
    }

    public void setCurrent(long current) {
        this.current = current;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records != null ? records : Collections.emptyList();
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getPages() {
        return pages;
    }

    public void setPages(long pages) {
        this.pages = pages;
    }

    public Map<String, Object> getExt() {
        return ext;
    }

    public void setExt(Map<String, Object> ext) {
        this.ext = ext;
    }
}
