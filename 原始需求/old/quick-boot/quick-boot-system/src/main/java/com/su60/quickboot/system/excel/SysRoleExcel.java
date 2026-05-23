package com.su60.quickboot.system.excel;

import com.su60.quickboot.common.validation.AddGroup;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import jakarta.validation.constraints.NotBlank;
import java.io.Serial;
import java.io.Serializable;

/**
 * 角色excel导出
 *
 * @author luyanan
 * @since 2024/8/17
 */
@Data
@Accessors(chain = true)
public class SysRoleExcel implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;


	/**
	 * 角色名称
	 *
	 * @since 2024/08/07
	 */
	@ExcelProperty(value = "角色名称")
	@NotBlank(groups = AddGroup.class, message = "角色名称不能为空")
	private String roleName;


	/**
	 * 角色权限字符串
	 *
	 * @since 2024/08/07
	 */
	@ExcelProperty(value = "角色权限字符串")
	@NotBlank(groups = AddGroup.class, message = "角色权限字符串不能为空")
	private String roleKey;
}
