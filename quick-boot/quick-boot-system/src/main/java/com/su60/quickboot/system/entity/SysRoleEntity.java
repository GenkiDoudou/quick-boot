package com.su60.quickboot.system.entity;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.*;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 角色信息表
 * </p>
 *
 * @author luyanan
 * @since 2024/08/07
 */
@Data
@Accessors(chain = true)
@TableName("sys_role")
public class SysRoleEntity implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 角色ID
	 *
	 * @since 2024/08/07
	 */

	@TableId(value = "id", type = IdType.ASSIGN_ID)
	private Long id;


	/**
	 * 角色名称
	 *
	 * @since 2024/08/07
	 */
	@TableField("role_name")
	private String roleName;


	/**
	 * 角色权限字符串
	 *
	 * @since 2024/08/07
	 */
	@TableField("role_key")
	private String roleKey;


	/**
	 * 显示顺序
	 *
	 * @since 2024/08/07
	 */
	@TableField("role_sort")
	private Integer roleSort;


	/**
	 * 数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限 5:自己）
	 *
	 * @since 2024/08/07
	 */
	@TableField("data_scope")
	private String dataScope;


	/**
	 * 菜单树选择项是否关联显示
	 *
	 * @since 2024/08/07
	 */
	@TableField("menu_check_strictly")
	private Integer menuCheckStrictly;


	/**
	 * 部门树选择项是否关联显示
	 *
	 * @since 2024/08/07
	 */
	@TableField("dept_check_strictly")
	private Integer deptCheckStrictly;


	/**
	 * 角色状态（0正常 1停用）
	 *
	 * @since 2024/08/07
	 */
	@TableField("status")
	private String status;


	/**
	 * 删除标志（0代表存在 2代表删除）
	 *
	 * @since 2024/08/07
	 */
	@TableLogic
	private String delFlag;


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
