package com.su60.quickboot.system.dos;

import cn.hutool.core.date.DatePattern;
import com.su60.quickboot.common.validation.AddGroup;
import com.su60.quickboot.common.validation.UpdateGroup;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.experimental.Accessors;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

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
public class SysRoleDo implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;


	/**
	 * 角色ID
	 *
	 * @since 2024/08/07
	 */
	@NotNull(groups = UpdateGroup.class, message = "角色ID 不能为空")
	private Long id;


	/**
	 * 角色名称
	 *
	 * @since 2024/08/07
	 */
	@NotBlank(groups = AddGroup.class, message = "角色名称不能为空")
	private String roleName;


	/**
	 * 角色权限字符串
	 *
	 * @since 2024/08/07
	 */
	@NotBlank(groups = AddGroup.class, message = "角色权限字符串不能为空")
	private String roleKey;


	/**
	 * 显示顺序
	 *
	 * @since 2024/08/07
	 */
	private Integer roleSort;


	/**
	 * 数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）
	 *
	 * @since 2024/08/07
	 */
	private String dataScope;


	/**
	 * 自定义部门的时候部门id
	 * @since 2026/1/6
	 */

	private List<Long> deptIds;


	/**
	 * 菜单树选择项是否关联显示
	 *
	 * @since 2024/08/07
	 */
	private Integer menuCheckStrictly;


	/**
	 * 部门树选择项是否关联显示
	 *
	 * @since 2024/08/07
	 */
	private Integer deptCheckStrictly;


	/**
	 * 角色状态（0正常 1停用）
	 *
	 * @since 2024/08/07
	 */
	@NotBlank(groups = AddGroup.class, message = "角色状态（0正常 1停用）不能为空")
	private String status;


	/**
	 * 删除标志（0代表存在 2代表删除）
	 *
	 * @since 2024/08/07
	 */
	@JsonIgnore
	private String delFlag;


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
	@JsonFormat(pattern = DatePattern.NORM_DATETIME_MINUTE_PATTERN)
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
	 * 菜单id列表
	 *
	 * @since 2024/8/21
	 */

	private List<Long> menuIds;

}
