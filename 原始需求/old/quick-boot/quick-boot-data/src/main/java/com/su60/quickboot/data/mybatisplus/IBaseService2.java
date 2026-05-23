//package com.su60.quickboot.data.mybatisplus;
//
//import com.su60.quickboot.common.core.PageInfo;
//import com.su60.quickboot.common.core.PageRequest;
//import com.su60.quickboot.data.spring.SpringContextHolder;
//import jakarta.servlet.http.HttpServletRequest;
//
//import java.io.Serializable;
//import java.util.Collection;
//import java.util.List;
//
///**
// * 基础Service
// *
// * @param <D> Do类
// * @param <T> 实体类泛型
// * @author luyanan
// * @since 2023/09/13
// **/
//public interface IBaseService2<T, D> {
//
//
//	/**
//	 * 分页
//	 *
//	 * @param pageRequest 分页参数
//	 * @param pageHandler 分页处理类
//	 * @return 分页返回
//	 * @since 2023/09/13
//	 */
//	PageInfo<D> page(PageRequest<D> pageRequest, PageVoHandler<T, D> pageHandler);
//
//	/**
//	 * 分页
//	 *
//	 * @param d             实体类
//	 * @param request       响应体
//	 * @param pageVoHandler 分页handler
//	 * @return 分页返回
//	 * @since 2024/8/16
//	 */
//	PageInfo<D> page(D d, HttpServletRequest request, PageVoHandler<T, D> pageVoHandler);
//
//
//	/**
//	 * 分页接口
//	 *
//	 * @param d             参数
//	 * @param pageVoHandler 分页处理器
//	 * @return 分页返回
//	 * @since 2024/10/11
//	 */
//	default PageInfo<D> page(D d, PageVoHandler<T, D> pageVoHandler) {
//		return page(d, SpringContextHolder.getRequest(), pageVoHandler);
//	}
//
//	/**
//	 * 保存
//	 *
//	 * @param d do类
//	 * @return 保存是否成功
//	 * @since 2024/06/29
//	 */
//	Boolean saveVo(D d);
//
//
//	/**
//	 * 根据id修改
//	 *
//	 * @param d do类
//	 * @return 修改是否成功
//	 * @since 2024/06/29
//	 */
//	boolean updateVoById(D d);
//
//
//	/**
//	 * 根据id查询
//	 *
//	 * @param id id
//	 * @return 查询结果
//	 * @since 2024/06/29
//	 */
//	D getVoById(Serializable id);
//
//	/**
//	 * 根据id集合查询
//	 *
//	 * @param ids id集合
//	 * @return 查询结果
//	 * @since 2024/10/14
//	 */
//	List<D> getVoByIds(Collection<? extends Serializable> ids);
//
//	/**
//	 * 根据id删除
//	 *
//	 * @param id id
//	 * @return 删除是否成功
//	 * @since 2024/10/12
//	 */
//	Boolean deleteById(Serializable id);
//
//
//	/**
//	 * 根据id集合删除
//	 *
//	 * @param ids id集合
//	 * @return 删除是否成功
//	 * @since 2024/10/12
//	 */
//	Boolean deleteByIds(Collection<? extends Serializable> ids);
//
//}
//
