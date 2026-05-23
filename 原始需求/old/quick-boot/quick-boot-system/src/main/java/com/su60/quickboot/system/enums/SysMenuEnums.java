package com.su60.quickboot.system.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 菜单枚举
 *
 * @author luyanan
 * @since 2024/8/7
 */
@AllArgsConstructor
@Getter
public enum SysMenuEnums implements IBaseEnum {


	/**
	 * 目录
	 *
	 * @since 2024/8/7
	 */
	MENU_TYPE_DIR(MenuType.MENU_TYPE, "目录", "M"),


	/**
	 * 菜单
	 *
	 * @since 2024/8/7
	 */
	MENU_TYPE_MENU(MenuType.MENU_TYPE, "菜单", "C"),


	/**
	 * 按钮
	 *
	 * @since 2024/8/7
	 */
	MENU_TYPE_BUTTON(MenuType.MENU_TYPE, "按钮", "F"),

	/**
	 * 是
	 *
	 * @since 2024/8/10
	 */

	IS_FRAME_YES(MenuType.IS_FRAME, "是", "0"),


	/**
	 * 否
	 *
	 * @since 2024/8/10
	 */

	IS_FRAME_NO(MenuType.IS_FRAME, "否", "1"),


	/**
	 * Layout 组件
	 *
	 * @since 2024/8/11
	 */

	COMPONENT_LAYOUT(MenuType.COMPONENT, "Layout", "Layout"),

	/**
	 * ParentView组件标识
	 *
	 * @since 2024/8/11
	 */

	COMPONENT_PARENTVIEW(MenuType.COMPONENT, "ParentView", "ParentView"),
	/**
	 * InnerLink组件标识
	 *
	 * @since 2024/8/11
	 */

	COMPONENT_INNER_LINK(MenuType.COMPONENT, "InnerLink", "InnerLink"),

	;

	private MenuType type;
	/**
	 * 标签名
	 *
	 * @since 2024/8/7
	 */

	private String label;

	/**
	 * 标签值
	 *
	 * @since 2024/8/7
	 */

	private String value;


	public enum MenuType implements IType {

		/**
		 * 菜单类型
		 *
		 * @since 2024/8/7
		 */

		MENU_TYPE,
		// 是否为外链
		IS_FRAME,
		// 组件
		COMPONENT,
	}
}
