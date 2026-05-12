package com.su60.quickboot.data.mybatisplus;

import cn.hutool.core.lang.Opt;
import cn.hutool.core.util.StrUtil;
import com.su60.quickboot.common.core.PageInfo;
import com.su60.quickboot.common.core.PageRequest;
import com.su60.quickboot.data.spring.SpringContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 分页工具类
 *
 * @author luyanan
 * @since 2024/10/24
 */
@UtilityClass
public class PageUtils {

	/**
	 * 获取分页请求参数
	 *
	 * @param t       条件
	 * @param <T>     泛型
	 * @param request http请求类
	 * @return 分页请求参数
	 * @since 2024/10/24
	 */
	public <T> PageRequest<T> getPage(T t, HttpServletRequest request) {
		if (null == request) {
			request = SpringContextHolder.getRequest();
		}
		String page = request.getParameter("page");
		if (StrUtil.isBlank(page)) {
			page = request.getParameter("pageNum");

		}
		String limit = request.getParameter("limit");
		if (StrUtil.isBlank(limit)){
			limit = request.getParameter("pageSize");
		}

		PageRequest<T> pageRequest = new PageRequest<>();
		pageRequest.setCurrent(Opt.ofNullable(page).map(Long::parseLong).orElse(0L));
		pageRequest.setSize(Opt.ofNullable(limit).map(Long::parseLong).orElse(10L));
		pageRequest.setParam(t);
		return pageRequest;
	}


	/**
	 * 逻辑分页
	 *
	 * @param lists    列表
	 * @param page     当前页数
	 * @param <R>      返回类泛型
	 * @param <T>      实体类泛型
	 * @param limit    每页条数
	 * @param function 回调处理
	 * @return 分页返回
	 * @since 2024/10/24
	 */
	public <T, R> PageInfo<R> ljPage(List<T> lists, long page, long limit, Function<T, R> function) {
		// 将list的数据逻辑分页
		List<R> list = lists.stream().skip((page - 1) * limit).map(function).limit(limit).collect(Collectors.toList());
		PageInfo<R> pageInfo = new PageInfo<>();
		pageInfo.setCurrent(page);
		pageInfo.setSize(limit);
		pageInfo.setTotal(lists.size());
		pageInfo.setRecords(list);
		return pageInfo;
	}


	/**
	 * 逻辑分页
	 *
	 * @param lists 列表
	 * @param page  页面
	 * @param limit 限制
	 * @param <T>   返回内容泛型
	 * @return 页面信息<t>
	 * @since 2024/10/24
	 */
	public <T> PageInfo<T> ljPage(List<T> lists, long page, long limit) {
		return ljPage(lists, page, limit, t -> t);
	}

}
