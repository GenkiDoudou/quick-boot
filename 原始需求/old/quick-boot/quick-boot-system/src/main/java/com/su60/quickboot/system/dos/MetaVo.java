package com.su60.quickboot.system.dos;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 路由显示信息
 *
 * @author luyanan
 * @since 2024/8/14
 */
@Getter
@Setter
@Accessors(chain = true)
public class MetaVo {


	public MetaVo(String title, String icon, boolean noCache, String link) {
		this.title = title;
		this.icon = icon;
		this.noCache = noCache;
		if (link.startsWith("http")) {
			this.link = link;
		}
	}

	public MetaVo(String title, String icon, String link) {
		this.title = title;
		this.icon = icon;
		this.link = link;
	}

	public MetaVo(String title, String icon) {
		this.title = title;
		this.icon = icon;
	}

	/**
	 * 设置该路由在侧边栏和面包屑中展示的名字
	 *
	 * @since 2024/8/14
	 */

	private String title;

	/**
	 * 设置该路由的图标，对应路径src/assets/icons/svg
	 *
	 * @since 2024/8/14
	 */

	private String icon;

	/**
	 * 设置为true，则不会被 <keep-alive>缓存
	 *
	 * @since 2024/8/14
	 */

	private boolean noCache;


	/**
	 * 内链地址（http(s)://开头）
	 *
	 * @since 2024/8/14
	 */

	private String link;
}
