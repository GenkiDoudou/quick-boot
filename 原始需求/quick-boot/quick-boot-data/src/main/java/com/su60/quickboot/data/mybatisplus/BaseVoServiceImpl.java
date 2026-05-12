package com.su60.quickboot.data.mybatisplus;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.toolkit.reflect.GenericTypeUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.su60.quickboot.common.bean.BeanConvertUtils;
import com.su60.quickboot.common.core.PageInfo;
import com.su60.quickboot.common.core.PageRequest;
import com.su60.quickboot.data.spring.SpringContextHolder;
import jakarta.servlet.http.HttpServletRequest;

import java.io.Serializable;
import java.util.*;

/**
 * vo类
 * @author luyanan
 * @since 2025/12/28
 */

public abstract class BaseVoServiceImpl<M extends BaseBaseMapper<T>, T, D> extends BaseServiceImpl<M, T> {

	/**
	 * 解析泛型参数。
	 * 使用 final 确保在初始化时完成解析。
	 */
	private final Class<?>[] typeArguments = GenericTypeUtils.resolveTypeArguments(getClass(), BaseVoServiceImpl.class);

	/**
	 * 获取 VO/DTO 的 Class 对象
	 *
	 * @return D 类的 Class 对象
	 */
	@SuppressWarnings("unchecked")
	protected Class<D> currentDoClass() {
		// 健壮性检查：防止代理类解析失败或泛型丢失导致 NPE
		if (Objects.isNull(typeArguments) || typeArguments.length < 3) {
			throw new IllegalStateException(
					String.format("无法从类 [%s] 中解析出泛型 D。请确保子类正确指定了泛型参数。", getClass().getName())
			);
		}
		return (Class<D>) this.typeArguments[2];
	}


	/**
	 * 分页查询
	 * @since 2025/12/28
	 * @param pageRequest 分页参数
	 * @param pageHandler 分页处理器
	 * @return
	 */

	protected PageInfo<D> page(PageRequest<D> pageRequest, PageVoHandler<T, D> pageHandler) {
		return this.pageVo(pageRequest, currentDoClass(), pageHandler);
	}

	/**
	 * 分页查询
	 * @since 2025/12/28
	 * @param d 分页参数
	 * @param pageHandler 分页处理器
	 * @return
	 */

	protected PageInfo<D> page(D d, PageVoHandler<T, D> pageHandler) {
		HttpServletRequest request = SpringContextHolder.getRequest();
		PageRequest<D> pageRequest = PageUtils.getPage(d, request);
		return this.pageVo(pageRequest, currentDoClass(), pageHandler);
	}

	/**
	 * 分页查询
	 * @since 2025/12/28
	 * @param d  查询参数
	 * @param request  请求参数
	 * @param pageVoHandler  分页处理器
	 * @return
	 */
	protected PageInfo<D> page(D d, HttpServletRequest request, PageVoHandler<T, D> pageVoHandler) {
		PageRequest<D> pageRequest = PageUtils.getPage(d, request);
		return page(pageRequest, pageVoHandler);
	}


	/**
	 * 保存
	 * @since 2025/12/28
	 * @param d
	 * @return
	 */
	protected Boolean saveVo(D d) {
		T t = BeanConvertUtils.convertTo(d, getEntityClass());
		boolean save = this.save(t);
		BeanConvertUtils.convertTo(t, d);
		return save;
	}


	/**
	 * 根据id修改
	 * @since 2025/12/28
	 * @param d
	 * @return
	 */
	protected Boolean updateVoById(D d) {
		T t = BeanConvertUtils.convertTo(d, getEntityClass());
		boolean b = this.updateById(t);
		return b;
	}


	/**
	 * 根据id查询
	 * @since 2025/12/28
	 * @param id  主键id
	 * @return
	 */
	protected D getVoById(Serializable id) {
		return BeanConvertUtils.convertTo(this.getById(id), currentDoClass());
	}


	/**
	 * 根据id集合查询
	 * @since 2025/12/28
	 * @param ids id集合
	 * @return
	 */
	protected List<D> getVoByIds(Collection<? extends Serializable> ids) {
		if (CollectionUtil.isEmpty(ids)) {
			return new ArrayList<>();
		}
		List<T> ts = this.listByIds(ids);
		return BeanConvertUtils.convertListTo(ts, currentDoClass());
	}


	/**
	 * 根据id删除
	 * @since 2025/12/28
	 * @param id id
	 *
	 * @return
	 */
	protected Boolean deleteById(Serializable id) {
		return this.removeById(id, true);
	}

	/**
	 * 根据id集合删除
	 * @since 2025/12/28
	 * @param ids id集合
	 * @return
	 */
	protected Boolean deleteByIds(Collection<? extends Serializable> ids) {
		if (CollectionUtil.isEmpty(ids)) {
			return false;
		}
		return this.removeByIds(ids, true);
	}

	/**
	 * 根据 id数组删除
	 * @since 2025/12/28
	 * @param ids id数组
	 * @return
	 */
	protected Boolean deleteByIds(Long[] ids) {
		if (null == ids || ids.length == 0) {
			return false;
		}
		return this.deleteByIds(Arrays.stream(ids).toList());

	}

}
