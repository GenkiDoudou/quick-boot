//package com.su60.quickboot.data.mybatisplus;
//
//import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
//import com.su60.quickboot.common.bean.BeanConvertUtils;
//import com.su60.quickboot.common.core.PageInfo;
//import com.su60.quickboot.common.core.PageRequest;
//import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
//import com.baomidou.mybatisplus.core.metadata.IPage;
//
///**
// * 基础Service实现
// *
// * @param <T> 实体类泛型
// * @param <M> mapper类
// * @author luyanan
// * @since 2023/09/16
// **/
//public class BaseServiceImpl3<M extends BaseBaseMapper<T>, T> extends ServiceImpl<M, T> implements IBaseService<T> {
//	@Override
//	public PageInfo<T> page(PageRequest<T> pageRequest, PageHandler<T> pageHandler) {
//		IPage iPage = getPage(pageRequest);
//
//		LambdaQueryWrapper<T> queryWrapper = new LambdaQueryWrapper<>();
//		T param = pageRequest.getParam();
//		if (null == pageHandler) {
//			pageHandler = new PageHandler<T>() {
//			};
//		}
//		pageHandler.queryWrapperHandler(param, queryWrapper);
//		queryWrapper.setEntity(param);
//
//
//		IPage resultPage = this.page(iPage, queryWrapper);
//		return pageHandler.getPageInfo(resultPage);
//	}
//
//	@Override
//	public <V> PageInfo<V> pageVo(PageRequest pageRequest, Class<V> voClass, PageVoHandler<T, V> pageVoHandler) {
//		IPage<T> page = getPage(pageRequest);
//		LambdaQueryWrapper<T> queryWrapper = new LambdaQueryWrapper<>();
//
//		V v = (V) pageRequest.getParam();
//
//		T t = BeanConvertUtils.instantiateClass(currentModelClass());
//		if (null != v) {
//			BeanConvertUtils.copyProperties(v, t);
//
//		}
//		queryWrapper.setEntity(t);
//		pageVoHandler.queryWrapperHandler(v, t, queryWrapper);
//		IPage<T> resultPage = this.page(page, queryWrapper);
//		return pageVoHandler.getPageInfo(resultPage, voClass);
//	}
//
//
//}
