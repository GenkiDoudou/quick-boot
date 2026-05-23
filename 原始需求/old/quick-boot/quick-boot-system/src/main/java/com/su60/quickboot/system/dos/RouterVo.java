package com.su60.quickboot.system.dos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 路由信息
 *
 * @author luyanan
 * @since 2024/8/14
 */
@Accessors(chain = true)
@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class RouterVo {

	/**
	 * 路由名称
	 *
	 * @since 2024/8/14
	 */

	private String name;


	/**
	 * 路由地址
	 *
	 * @since 2024/8/14
	 */

	private String path;


	/**
	 * 是否隐藏路由,当为true的时候该路由不会在侧边栏出现
	 *
	 * @since 2024/8/14
	 */

	private boolean hidden;

	/**
	 * 重定向地址，当设置 noRedirect 的时候该路由在面包屑导航中不可被点击
	 *
	 * @since 2024/8/14
	 */

	private String redirect;


	/**
	 * 组件地址
	 *
	 * @since 2024/8/14
	 */

	private String component;

	/**
	 * 路由参数：如 {"id": 1, "name": "ry"}
	 *
	 * @since 2024/8/14
	 */

	private String query;

	/**
	 * 当你一个路由下面的 children 声明的路由大于1个时，自动会变成嵌套的模式--如组件页面
	 *
	 * @since 2024/8/14
	 */

	private Boolean alwaysShow;


	/**
	 * 其他元素
	 *
	 * @since 2024/8/14
	 */

	private MetaVo meta;


	/**
	 * 子路由
	 *
	 * @since 2024/8/14
	 */

	private List<RouterVo> children;
}
