//package com.su60.quickboot.data.mybatisplus;
//
//import cn.hutool.core.collection.CollectionUtil;
//import com.su60.quickboot.common.bean.BeanConvertUtils;
//import com.su60.quickboot.common.core.PageInfo;
//import com.su60.quickboot.common.core.PageRequest;
//import com.baomidou.mybatisplus.core.toolkit.reflect.GenericTypeUtils;
//import jakarta.servlet.http.HttpServletRequest;
//
//import java.io.Serializable;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.List;
//
///**
// * 基础Service实现
// *
// * @param <T> 实体类泛型
// * @param <M> mapper类
// * @param <D> do类
// * @author luyanan
// * @since 2023/09/16
// **/
//public class BaseServiceImpl2<M extends BaseBaseMapper<T>, T, D> extends BaseServiceImpl<M, T> implements IBaseService2<T, D> {
//
//
//	private final Class<?>[] typeArguments = GenericTypeUtils.resolveTypeArguments(getClass(), BaseServiceImpl2.class);
//
//	/**
//	 * do类class
//	 *
//	 * @return do类class
//	 * @since 2024/06/30
//	 */
//
//	protected Class<D> currentDoClass() {
//		return (Class<D>) this.typeArguments[2];
//	}
//
//	@Override
//	public PageInfo<D> page(PageRequest<D> pageRequest, PageVoHandler<T, D> pageHandler) {
//		return this.pageVo(pageRequest, currentDoClass(), pageHandler);
//	}
//
//	@Override
//	public PageInfo<D> page(D d, HttpServletRequest request, PageVoHandler<T, D> pageVoHandler) {
//		PageRequest<D> pageRequest = PageUtils.getPage(d, request);
//		return page(pageRequest, pageVoHandler);
//	}
//
//	@Override
//	public Boolean saveVo(D d) {
//
//		T t = BeanConvertUtils.convertTo(d, currentModelClass());
//		boolean save = this.save(t);
//		BeanConvertUtils.convertTo(t, d);
//		return save;
//	}
//
//	@Override
//	public boolean updateVoById(D d) {
//		T t = BeanConvertUtils.convertTo(d, currentModelClass());
//		boolean b = this.updateById(t);
//		return b;
//	}
//
//	@Override
//	public D getVoById(Serializable id) {
//		return BeanConvertUtils.convertTo(this.getById(id), currentDoClass());
//	}
//
//	@Override
//	public List<D> getVoByIds(Collection<? extends Serializable> ids) {
//		if (CollectionUtil.isEmpty(ids)) {
//			return new ArrayList<>();
//		}
//		List<T> ts = this.listByIds(ids);
//		return BeanConvertUtils.convertListTo(ts, currentDoClass());
//	}
//
//	@Override
//	public Boolean deleteById(Serializable id) {
//		return this.removeById(id, true);
//	}
//
//	@Override
//	public Boolean deleteByIds(Collection<? extends Serializable> ids) {
//		if (CollectionUtil.isEmpty(ids)) {
//			return false;
//		}
//		return this.removeByIds(ids, true);
//
//	}
//}
