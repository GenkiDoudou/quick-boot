package com.su60.quickboot.data.mybatisplus;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.su60.quickboot.common.bean.BeanConvertUtils;
import com.su60.quickboot.common.core.PageInfo;
import com.su60.quickboot.common.core.PageRequest;

/**
 * service层的扩展层
 * @author luyanan
 * @since 2025/12/28
 */

public abstract class BaseServiceImpl<M extends BaseBaseMapper<T>, T> extends ServiceImpl<M, T> {


	/**
	 * 构建Page对象
	 *
	 * @param pageRequest 分页参数
	 * @return Page对象
	 * @since 2023/09/13
	 */
	public IPage getPage(PageRequest pageRequest) {
		IPage page = new Page<>();
		page.setCurrent(pageRequest.getCurrent());
		page.setSize(pageRequest.getSize());
		return page;
	}

	/**
	 * 分页
	 *
	 * @param pageRequest 分页参数
	 * @param pageHandler 分页处理类
	 * @return 分页返回
	 * @since 2023/09/13
	 */
	public PageInfo<T> page(PageRequest<T> pageRequest, PageHandler<T> pageHandler) {
		IPage iPage = getPage(pageRequest);

		LambdaQueryWrapper<T> queryWrapper = new LambdaQueryWrapper<>();
		T param = pageRequest.getParam();
		if (null == pageHandler) {
			pageHandler = new PageHandler<T>() {
			};
		}
		pageHandler.queryWrapperHandler(param, queryWrapper);
		queryWrapper.setEntity(param);
		IPage resultPage = this.page(iPage, queryWrapper);
		return pageHandler.getPageInfo(resultPage);
	}


	/************************vo类操作***********************/
	/**
	 * vo分页
	 *
	 * @param <V>           vo类泛型
	 * @param pageRequest   分页参数
	 * @param voClass       vo类
	 * @param pageVoHandler 分页处理器
	 * @return 分页结果
	 * @since 2023/09/13
	 */
	public <V> PageInfo<V> pageVo(PageRequest pageRequest, Class<V> voClass, PageVoHandler<T, V> pageVoHandler) {
		IPage<T> page = getPage(pageRequest);
		LambdaQueryWrapper<T> queryWrapper = new LambdaQueryWrapper<>();
		V v = (V) pageRequest.getParam();
		T t = BeanConvertUtils.instantiateClass(currentModelClass());
		if (null != v) {
			BeanConvertUtils.copyProperties(v, t);
		}
		queryWrapper.setEntity(t);
		pageVoHandler.queryWrapperHandler(v, t, queryWrapper);
		IPage<T> resultPage = this.page(page, queryWrapper);
		return pageVoHandler.getPageInfo(resultPage, voClass);
	}


}
