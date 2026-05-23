package com.su60.quickboot.system.dos;

import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNode;
import com.baomidou.mybatisplus.annotation.TableField;
import com.su60.quickboot.common.validation.AddGroup;
import com.su60.quickboot.common.validation.UpdateGroup;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.experimental.Accessors;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
public class SysMenuDo implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;


	/**
	 * 菜单ID
	 *
	 * @since 2024/08/07
	 */
	@NotNull(groups = UpdateGroup.class, message = "菜单ID 不能为空")
	private Long id;


	/**
	 * 菜单名称
	 *
	 * @since 2024/08/07
	 */
	@NotBlank(groups = AddGroup.class, message = "菜单名称不能为空")
	private String menuName;


	/**
	 * 父菜单ID
	 *
	 * @since 2024/08/07
	 */
	private Long parentId;


	/**
	 * 显示顺序
	 *
	 * @since 2024/08/07
	 */
	private Integer orderNum;


	/**
	 * 路由地址
	 *
	 * @since 2024/08/07
	 */
	private String path;


	/**
	 * 组件路径
	 *
	 * @since 2024/08/07
	 */
	private String component;


	/**
	 * 路由参数
	 *
	 * @since 2024/08/07
	 */
	private String query;


	/**
	 * 路由名称
	 *
	 * @since 2024/08/07
	 */
	private String routeName;


	/**
	 * 是否为外链（0是 1否）
	 *
	 * @since 2024/08/07
	 */
	private String isFrame;


	/**
	 * 是否缓存（0缓存 1不缓存）
	 *
	 * @since 2024/08/07
	 */
	private String isCache;


	/**
	 * 菜单类型（M目录 C菜单 F按钮）
	 *
	 * @since 2024/08/07
	 */
	private String menuType;


	/**
	 * 菜单状态（0显示 1隐藏）
	 *
	 * @since 2024/08/07
	 */
	private String visible;


	/**
	 * 菜单状态（0正常 1停用）
	 *
	 * @since 2024/08/07
	 */
	private String status;


	/**
	 * 按钮权限标识
	 *
	 * @since 2024/08/07
	 */
	private String buttonPerms;

	/**
	 * 接口权限标识
	 * @since 2025/9/17
	 */

	private String apiPerms;


	/**
	 * 菜单图标
	 *
	 * @since 2024/08/07
	 */
	private String icon;


	/**
	 * 创建者
	 *
	 * @since 2024/08/07
	 */
	@JsonIgnore
	private Long createBy;


	/**
	 * 创建时间
	 *
	 * @since 2024/08/07
	 */
	private Date createTime;


	/**
	 * 更新者
	 *
	 * @since 2024/08/07
	 */
	@JsonIgnore
	private Long updateBy;


	/**
	 * 更新时间
	 *
	 * @since 2024/08/07
	 */
	@JsonIgnore
	private Date updateTime;


	/**
	 * 备注
	 *
	 * @since 2024/08/07
	 */
	private String remark;


	/**
	 * 子菜单
	 *
	 * @since 2024/8/14
	 */

	private List<SysMenuDo> children = new ArrayList<>();

}
