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

public class BaseServiceImpl<M extends BaseMapper<T>, T> extends ServiceImpl<M, T> {


  /**
   * 获取IPage 对象
   *
   * @param pageRequest
   * @return
   * @since 2026/8/1
   */
  public IPage<T> getPage(PageRequest pageRequest) {
    IPage<T> page = new Page<>();
    page.setCurrent(pageRequest.getCurrent());
    page.setSize(pageRequest.getSize());
    return page;
  }


  /**
   * 分页
   *
   * @param pageRequest 分页参数
   * @param consumer    参数处理
   * @param vClass      返回vo类
   * @param biFunction  列表转换
   * @return
   * @since 2026/8/1
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
   * 分页
   *
   * @param pageRequest 分页参数
   * @param consumer    参数处理
   * @param vClass      返回vo类
   * @return
   * @since 2026/8/1
   */
  public <V> PageInfo<V> page(PageRequest pageRequest, Consumer<LambdaQueryWrapper<T>> consumer, Class<V> vClass) {

    return page(pageRequest, consumer, vClass, null);
  }


  /**
   * 分页
   *
   * @param pageRequest 分页参数
   * @param consumer    参数处理
   * @return
   * @since 2026/8/1
   */
  public <V> PageInfo<V> page(PageRequest pageRequest, Consumer<LambdaQueryWrapper<T>> consumer) {
    Class<T> entityClass = getEntityClass();
    Class<V> vClass = (Class<V>) entityClass;
    return page(pageRequest, consumer, vClass, null);
  }


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
   * 将实体类转换为vo类
   *
   * @param obj    实体类
   * @param vClass vo类
   * @return
   * @since 2026/8/2
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


  public <V> V getVoById(Serializable id, Class<V> vClass) {
    T t = super.getById(id);
    return toVo(t, vClass);
  }

}
