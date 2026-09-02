package io.github.genkidoudou.common.web;

import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.R;

/**
 * Tier-1 CRUD Controller 可选辅助：统一 {@link R} 包装，避免各 Controller 重复 {@code R.ok(...)} 样板。
 * <p>
 * 实现类仍须自行处理鉴权与参数校验；本接口不提供 HTTP 映射。
 */
public interface CrudControllerSupport {

  /**
   * 分页结果包装为 {@link R}。
   *
   * @param page 分页数据
   * @param <V>  Vo 类型
   * @return 统一响应
   */
  default <V> R<PageInfo<V>> crudPageResult(PageInfo<V> page) {
    return R.ok(page);
  }

  /**
   * 详情包装为 {@link R}。
   *
   * @param vo  详情 Vo
   * @param <V> Vo 类型
   * @return 统一响应
   */
  default <V> R<V> crudDetailResult(V vo) {
    return R.ok(vo);
  }

  /**
   * 无 data 的成功响应。
   *
   * @return 统一响应
   */
  default R<Void> crudOk() {
    return R.ok();
  }
}
