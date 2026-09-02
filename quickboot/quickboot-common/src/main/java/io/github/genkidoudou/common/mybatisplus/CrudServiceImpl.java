package io.github.genkidoudou.common.mybatisplus;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.PageRequest;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Tier-1 标准 CRUD Service 模板：分页、详情、导出列表等通用骨架。
 * <p>
 * 子类提供 {@link #voClass()} 与 {@link #applyQuery}；复杂新增/修改/删除逻辑在子类 override。
 * 对外 API 仅暴露 Vo，Entity 转换在基类 {@link BaseServiceImpl#toVo}/{@link BaseServiceImpl#toEntity} 内完成。
 *
 * @param <M> Mapper 类型
 * @param <T> 实体类型
 * @param <V> Vo 类型
 */
public abstract class CrudServiceImpl<M extends BaseMapper<T>, T, V>
  extends BaseServiceImpl<M, T> implements CrudQuerySupport<T, V> {

  /**
   * 当前域列表/详情使用的 Vo 类型。
   *
   * @return Vo Class
   */
  protected abstract Class<V> voClass();

  /**
   * 标准分页：从 {@link PageRequest#getParam()} 取查询 Vo 并委托 {@link #applyQuery}。
   *
   * @param pageRequest 分页与筛选
   * @return Vo 分页结果
   */
  public PageInfo<V> crudPage(PageRequest<V> pageRequest) {
    V param = pageRequest != null ? pageRequest.getParam() : null;
    return page(pageRequest, q -> applyQuery(q, param), voClass());
  }

  /**
   * 按主键查详情；不存在时抛出业务警告异常。
   *
   * @param id              主键
   * @param notFoundMessage 不存在时的提示文案
   * @return Vo
   */
  public V crudGetDetail(Serializable id, String notFoundMessage) {
    V vo = getVoById(id, voClass());
    if (vo == null) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, notFoundMessage);
    }
    return vo;
  }

  /**
   * 导出/列表：优先按 Vo 中 {@code ids} 批量查；否则按 {@link #applyQuery} 条件 list。
   *
   * @param query    查询 Vo（含可选 ids）
   * @param idGetter 从 Vo 取 ids 列表的函数；无 ids 字段时传 {@code V::getIds} 或 {@code q -> Collections.emptyList()}
   * @return 实体列表（由子类 toVo 后导出）
   */
  protected List<T> crudListForQuery(V query, java.util.function.Function<V, List<Long>> idGetter) {
    V q = query == null ? null : query;
    if (q != null && idGetter != null) {
      List<Long> ids = idGetter.apply(q);
      if (ids != null) {
        List<Long> distinct = ids.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
        if (!distinct.isEmpty()) {
          return listByIds(distinct);
        }
      }
    }
    LambdaQueryWrapper<T> wrapper = new LambdaQueryWrapper<>();
    applyQuery(wrapper, q);
    return list(wrapper);
  }

  /**
   * 无 ids 字段时的导出列表：仅按 applyQuery 筛选。
   *
   * @param query 查询 Vo
   * @return 实体列表
   */
  protected List<T> crudListForQuery(V query) {
    return crudListForQuery(query, v -> Collections.emptyList());
  }

  /**
   * 将实体列表转为 Vo 列表（导出等场景）。
   *
   * @param entities 实体列表
   * @return Vo 列表
   */
  protected List<V> crudToVoList(List<T> entities) {
    if (CollectionUtil.isEmpty(entities)) {
      return Collections.emptyList();
    }
    return entities.stream().map(e -> toVo(e, voClass())).collect(Collectors.toList());
  }

  /**
   * 批量删除空集合安全：ids 为空时直接返回。
   *
   * @param ids 主键集合
   */
  protected void crudRemoveByIds(Collection<? extends Serializable> ids) {
    if (CollectionUtil.isEmpty(ids)) {
      return;
    }
    removeByIds(ids);
  }
}
