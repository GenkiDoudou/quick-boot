package io.github.genkidoudou.common.mybatisplus;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.PageRequest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * MyBatis-Plus Service 基类：封装分页查询、Entity/VO 拷贝等常用能力。
 *
 * @param <M> Mapper 类型
 * @param <T> 实体类型
 */
public class BaseServiceImpl<M extends BaseMapper<T>, T> extends ServiceImpl<M, T> {


  /**
   * 由 {@link PageRequest} 构造 MyBatis-Plus {@link IPage}。
   *
   * @param pageRequest 分页参数
   * @return 未执行查询的分页对象
   */
  public IPage<T> getPage(PageRequest pageRequest) {
    IPage<T> page = new Page<>();
    page.setCurrent(pageRequest.getCurrent());
    page.setSize(pageRequest.getSize());
    return page;
  }


  /**
   * 分页查询并转换为 VO 列表。
   *
   * @param pageRequest 分页参数
   * @param consumer    可选的 {@link LambdaQueryWrapper} 条件组装
   * @param vClass      目标 VO 类型
   * @param biFunction  可选的后处理：入参为实体列表与 VO 列表，返回最终 records
   * @return 分页结果
   */
  public <V> PageInfo<V> page(PageRequest pageRequest, Consumer<LambdaQueryWrapper<T>> consumer, Class<V> vClass, BiFunction<List<T>, List<V>, List<V>> biFunction) {
    LambdaQueryWrapper<T> queryWrapper = new LambdaQueryWrapper<>();
    if (null != consumer) {
      consumer.accept(queryWrapper);
    }
    IPage<T> page = getPage(pageRequest);
    IPage<T> iPage = super.page(page, queryWrapper);
    return buildPageInf(iPage, vClass, biFunction);
  }

  /**
   * 分页查询并转换为 VO（无后处理函数）。
   *
   * @param pageRequest 分页参数
   * @param consumer    查询条件组装
   * @param vClass      目标 VO 类型
   * @return 分页结果
   */
  public <V> PageInfo<V> page(PageRequest pageRequest, Consumer<LambdaQueryWrapper<T>> consumer, Class<V> vClass) {

    return page(pageRequest, consumer, vClass, null);
  }


  /**
   * 分页查询，VO 类型与实体相同。
   *
   * @param pageRequest 分页参数
   * @param consumer    查询条件组装
   * @return 分页结果
   */
  public <V> PageInfo<V> page(PageRequest pageRequest, Consumer<LambdaQueryWrapper<T>> consumer) {
    Class<T> entityClass = getEntityClass();
    Class<V> vClass = (Class<V>) entityClass;
    return page(pageRequest, consumer, vClass, null);
  }


  /**
   * 将 MyBatis-Plus 分页结果转换为 {@link PageInfo}，并拷贝 records 为 VO。
   *
   * @param page       已查询的分页结果
   * @param vClass     目标 VO 类型
   * @param biFunction 可选 records 后处理
   * @return 业务分页对象
   */
  public <V> PageInfo<V> buildPageInf(IPage<T> page, Class<V> vClass, BiFunction<List<T>, List<V>, List<V>> biFunction) {
    List<T> records = page.getRecords();
    PageInfo<V> pageInfo = new PageInfo<>();
    pageInfo.setCurrent(page.getCurrent());
    pageInfo.setSize(page.getSize());
    pageInfo.setTotal(page.getTotal());
    pageInfo.setPages(page.getPages());
    if (CollectionUtil.isNotEmpty(records)) {
      List<V> vList = BeanUtil.copyToList(records, vClass);
      if (null != biFunction) {
        vList = biFunction.apply(records, vList);
      }
      pageInfo.setRecords(vList);
    } else {
      pageInfo.setRecords(new ArrayList<>());
    }
    return pageInfo;
  }


  /**
   * 实体转 VO；类型相同时直接返回原对象。
   *
   * @param obj      实体
   * @param vClass   目标 VO 类型
   * @param function 可选：在拷贝后基于实体再次加工 VO
   * @return VO 或 {@code null}（实体为 null）
   */
  public <V> V toVo(T obj, Class<V> vClass, Function<T, V> function) {
    if (null == obj) {
      return null;
    }
    Class<T> entityClass = getEntityClass();
    if (entityClass.equals(vClass)) {
      return (V) obj;
    }
    V v = BeanUtil.copyProperties(obj, vClass);
    if (null != function) {
      v = function.apply(obj);
    }
    return v;
  }


  /**
   * VO 转实体；类型相同时直接强转。
   *
   * @param vo 请求 VO 或 DTO
   * @return 实体实例
   */
  public T toEntity(Object vo) {
    Class<T> entityClass = getEntityClass();
    if (vo.getClass().equals(entityClass)) {
      return (T) vo;
    }
    return BeanUtil.copyProperties(vo, entityClass);
  }

  protected <V> V toVo(T obj, Class<V> vClass) {
    return toVo(obj, vClass, null);
  }


  /**
   * 按主键查询并转换为 VO。
   *
   * @param id     主键
   * @param vClass 目标 VO 类型
   * @return VO；不存在时 {@code null}
   */
  public <V> V getVoById(Serializable id, Class<V> vClass) {
    T t = super.getById(id);
    return toVo(t, vClass);
  }

}
