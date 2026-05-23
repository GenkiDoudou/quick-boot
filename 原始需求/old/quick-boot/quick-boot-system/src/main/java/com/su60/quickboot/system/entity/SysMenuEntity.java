package com.su60.quickboot.system.entity;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.*;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 菜单权限表
 * </p>
 *
 * @author luyanan
 * @since 2024/08/07
 */
@Data
@Accessors(chain = true)
@TableName("sys_menu")
public class SysMenuEntity implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 菜单ID
	 *
	 * @since 2024/08/07
	 */

	@TableId(value = "id", type = IdType.ASSIGN_ID)
	private Long id;


	/**
	 * 菜单名称
	 *
	 * @since 2024/08/07
	 */
	@TableField("menu_name")
	private String menuName;


	/**
	 * 父菜单ID
	 *
	 * @since 2024/08/07
	 */
	@TableField("parent_id")
	private Long parentId;


	/**
	 * 显示顺序
	 *
	 * @since 2024/08/07
	 */
	@TableField("order_num")
	private Integer orderNum;


	/**
	 * 路由地址
	 *
	 * @since 2024/08/07
	 */
	@TableField("path")
	private String path;


	/**
	 * 组件路径
	 *
	 * @since 2024/08/07
	 */
	@TableField("component")
	private String component;


	/**
	 * 路由参数
	 *
	 * @since 2024/08/07
	 */
	@TableField("query")
	private String query;


	/**
	 * 路由名称
	 *
	 * @since 2024/08/07
	 */
	@TableField("route_name")
	private String routeName;


	/**
	 * 是否为外链（0是 1否）
	 *
	 * @since 2024/08/07
	 */
	@TableField("is_frame")
	private String isFrame;


	/**
	 * 是否缓存（0缓存 1不缓存）
	 *
	 * @since 2024/08/07
	 */
	@TableField("is_cache")
	private String isCache;


	/**
	 * 菜单类型（M目录 C菜单 F按钮）
	 *
	 * @since 2024/08/07
	 */
	@TableField("menu_type")
	private String menuType;


	/**
	 * 菜单状态（0显示 1隐藏）
	 *
	 * @since 2024/08/07
	 */
	@TableField("visible")
	private String visible;


	/**
	 * 菜单状态（0正常 1停用）
	 *
	 * @since 2024/08/07
	 */
	@TableField("status")
	private String status;


	/**
	 * 按钮权限标识
	 *
	 * @since 2024/08/07
	 */
	@TableField("button_perms")
	private String buttonPerms;

	/**
	 * 接口权限标识
	 * @since 2025/9/17
	 */

	@TableField("api_perms")
	private String apiPerms;


	/**
	 * 菜单图标
	 *
	 * @since 2024/08/07
	 */
	@TableField("icon")
	private String icon;


	/**
	 * 创建者
	 *
	 * @since 2024/08/07
	 */
	@TableField(value = "create_by", fill = FieldFill.INSERT)
	private Long createBy;


	/**
	 * 创建时间
	 *
	 * @since 2024/08/07
	 */
	@TableField(value = "create_time", fill = FieldFill.INSERT)
	private Date createTime;


	/**
	 * 更新者
	 *
	 * @since 2024/08/07
	 */
	@TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
	private Long updateBy;


	/**
	 * 更新时间
	 *
	 * @since 2024/08/07
	 */
	@TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
	private Date updateTime;


	/**
	 * 备注
	 *
	 * @since 2024/08/07
	 */
	@TableField("remark")
	private String remark;


}
