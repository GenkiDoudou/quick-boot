package io.github.genkidoudou.common.mybatisplus;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

/**
 * Tier-1 CRUD 查询条件组装契约。
 * <p>
 * 由 {@link CrudServiceImpl} 在分页、导出列表等场景调用；子类按 Vo 查询字段填充 {@link LambdaQueryWrapper}。
 *
 * @param <T> 持久化实体类型
 * @param <V> 查询 Vo 类型
 */
public interface CrudQuerySupport<T, V> {

  /**
   * 按查询 Vo 组装列表/分页条件；{@code param} 为 null 时不追加业务筛选。
   *
   * @param query 条件包装器
   * @param param 查询参数（可为 null）
   */
  void applyQuery(LambdaQueryWrapper<T> query, V param);
}
